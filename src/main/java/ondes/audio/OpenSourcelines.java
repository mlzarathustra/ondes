package ondes.audio;
import ondes.App;
import ondes.SynthSession;

import javax.sound.sampled.*;
import java.util.Arrays;

import static java.lang.Math.*;
import static java.lang.System.err;
import static java.lang.System.out;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;



// todo - separate out the MonoMainMix.openOutputLine function
//        into this class

class OpenLine {
    boolean DB = false;
    boolean DRY = false;
    SourceDataLine srcLine;
    int sampleRate;
    boolean signed, littleEndian;
    int channels, bytesPerSample;
    int bufferSize = 4096;
    private byte[] split;
    private int[] outputBuffer;
    private byte[] lineBuffer;

    int outPos = 0;

    /**
     *
     * @param s - Source of data, from the mixer's perspective.
     *          This object outputs data to the mixer.
     *
     * @throws Exception - the audio system throws exceptions
     */
    OpenLine(SourceDataLine s) throws Exception {
        srcLine = s;
        AudioFormat audFmt = srcLine.getFormat();

        //  it can be floating point...
        sampleRate = (int) audFmt.getSampleRate();

        AudioFormat.Encoding enc = audFmt.getEncoding();
        if (enc != PCM_SIGNED && enc != PCM_UNSIGNED) {
            throw new Exception("Unsupported Encoding:" + enc);
        }
        signed = (enc == PCM_SIGNED);

        littleEndian = !audFmt.isBigEndian();
        channels = audFmt.getChannels();

        //  bufSize must be divisible by the frame size.
        //  frameSize should be == bytesPerSample * channels
        int frameSize = audFmt.getFrameSize();
        if (frameSize != AudioSystem.NOT_SPECIFIED) {
            bufferSize = bufferSize - (bufferSize % frameSize);
        }
        int sb = audFmt.getSampleSizeInBits();
        bytesPerSample = (sb/8) + (sb%8 > 0 ? 1 : 0);
        split = new byte[bytesPerSample]; // avoid allocating one for each sample

        outputBuffer = new int[bufferSize]; // todo - this is mono
        lineBuffer = new byte[bufferSize * bytesPerSample * channels];

        if (DB) {
            out.println(audFmt);
            out.println(
                "frameSize=" + frameSize + "; bufSize=" + bufferSize +
                    "; bytesPerSample=" + bytesPerSample +
                    "; littleEndian=" + littleEndian +
                    "; # of channels=" + channels
            );
        }

        srcLine.open(audFmt,lineBuffer.length);
        srcLine.start(); // without this, you won't get any sound.
    }

    public void update(int n) {
        outputBuffer[outPos++] = n;
        srcLineWrite();
    }

    /**
     * <p>
     *     transform for endian-ness, multiple channels, unsigned samples.
     * </p>
     * @param outputBuffer - the buffer to copy into lineBuffer
     * @param count - how many bytes to copy
     * @return - how many bytes were copied into lineBuffer
     */
    private int toLineFmt(int[] outputBuffer, int count) {
        int lbIdx=0;
        long unsignedOffset = signed ? 0 : 1 << (8*bytesPerSample - 1);
        for (int obp=0; obp<count; ++obp) {
            int val = outputBuffer[obp];
            val += unsignedOffset;
            for (int i=0; i<bytesPerSample; ++i) {
                int si = littleEndian ? i : bytesPerSample-(i+1);
                split[si] = (byte)(val & 0xff);
                val >>>= 8;
            }

            // copy the same signal to both channels
            //  todo - implement multiple channels
            //
            for (int i=0; i<channels; ++i) {
                for (int j=0; j<bytesPerSample; ++j) {
                    lineBuffer[lbIdx++] = split[j];
                }
            }
        }
        return lbIdx;
    }

    void srcLineWrite() {
        if (srcLine.available() == 0 && outPos < outputBuffer.length) return;

        int lineFmtCount = toLineFmt(outputBuffer, outPos); // copies into lineBuffer
        outPos = 0;

        if (DRY) {
            //out.println("outputBuffer: "+Arrays.toString(outputBuffer));
            out.println("srcLine.write( disabled )");
        } else {
            long beforeWrite = System.nanoTime();

            //  Send the samples to the sound module.
            //  It blocks if needed.
            //
            int rs = srcLine.write(lineBuffer, 0, lineFmtCount);
//            out.print("wrote "+rs+" bytes.");

            long now = System.nanoTime();
//            if (now - beforeWrite > timingOverflow) {
//                // for a 2k buffer, the whole cycle needs to be under 46 ms.
//                out.println(String.format(" >> srcLine.write block: %,d <<",(now-beforeWrite)));
//            }
//
//            if (LOG_MAIN_OUT) {
//                // loops++; for limiting # of outputs
//
//                logWrite("srcLine.write() rs="+rs+" bytes; "+
//                    "   total >> "+ String.format("%,d ns",(now-lastWrite))+
//                    "    write "+ String.format("%,d ns",(now-beforeWrite))+
//                    "    process "+ String.format("%,d ns", (beforeWrite - lastWrite))
//                );
//            }
//            lastWrite=now;
        }
    }

}




/**
    Open two source lines from the same mixer and see if they're the same.

    ...turns out, they are not!

*/
public class OpenSourcelines {

    public static void main(String[] args) {

        if (args.length == 0) {
            out.println("please specify an output port.");
            System.exit(-1);
        }

        String outDevStr = String.join(" ",args);
        Mixer mixer = SynthSession.getMixer(outDevStr);

        SourceDataLine line1 = getDataLine(mixer);
        out.println("Got SourceDataLine 1: "+ line1);
        SourceDataLine line2 = getDataLine(mixer);
        out.println("Got SourceDataLine: 2"+ line2);

        playSine(220, line1);
        //playSine(660, line2);  // ugly noise if you add this.
    }


    // ///


    static void playSine(final double freq, final SourceDataLine line) {
        new Thread() {

            public void run() {
                try {
                    OpenLine openLine = new OpenLine(line);
                    final double delta = freq / openLine.sampleRate;
                    double phase = 0;

                    for (;;) {
                        phase += delta;
                        openLine.update( (int) (sin( 2 * PI * phase ) * 0xfff ));
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }.start();
    }


    public static SourceDataLine getDataLine(Mixer mixer) {

        try {
            Line.Info[] lineInfo = mixer.getSourceLineInfo();
            out.println(Arrays.toString(lineInfo));

            SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo[0]);
            out.println(line);
            // default format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo,
            // 4 bytes/frame, little-endian
            return line;

        } catch (Exception ex) {
            err.println("Can't open AUDIO output. Did you specify -out correctly?");
            err.println("Device is "+mixer);
            App.quitOnError();
            //err.println("Exception Caught: " + ex);
            //ex.printStackTrace();
        }
        return null;
    }





}
