package ondes.synth.mix;

import ondes.App;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.sampled.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;

/**
 * The inputs to this class will direct output
 * to the JavaSound (stereo) audio system.
 * Copies the input channel to both L and R out
 *
 * TODO - implement a stereo input mix
 *
 */
@SuppressWarnings("FieldMayBeFinal")
public class MonoMainMix extends MainMix {

    private boolean DB=true;

    SourceDataLine srcLine;
    private int sampleRate;


    /**
     * Critical for response, as the system has to wait until
     * the next buffer for a note to start sounding. So we want
     * it as small as possible. However, any smaller than about
     * 1024, and it only makes funny clicking noises.
     *
     * See timing.md for more.
     *
     * IMPORTANT: Don't set it here. It's set from a command-line
     * argument.
     *
     */
    private int bufferSize;
    //

    private int bytesPerSample;

    private boolean signed, littleEndian;
    private int channels;  // 1=mono 2=stereo &c.

    private byte[] lineBuffer;
    private int[] outputBuffer;
    private byte[] split;
    //
    private int avgLatency, timingOverflow;


    //public MonoMainMix(Mixer mixer) { this(mixer,2048); }

    public MonoMainMix(Mixer mixer, int bufferSize) {
        this.bufferSize = bufferSize;

        // in nanoseconds. it's just for a message,
        // so it doesn't need to be precise
        avgLatency =  (int)( ((float)bufferSize * 1_000_000_000.0) / sampleRate );
        timingOverflow = avgLatency * 3 / 2;

        try {
            Line.Info[] lineInfo = mixer.getSourceLineInfo();
            out.println(Arrays.toString(lineInfo));

            SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo[0]);
            out.println(line);
            // format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo,
            // 4 bytes/frame, little-endian

            openOutputLine(line);

        } catch (Exception ex) {
            err.println("Can't open AUDIO output. Did you specify -out correctly?");
            err.println("Device is "+mixer);
            App.quitOnError();
            //err.println("Exception Caught: " + ex);
            //ex.printStackTrace();
        }
    }

    /**
     *
     * @param s - Source of data, from the mixer's perspective.
     *          This object outputs data to the mixer.
     *
     * @throws Exception - the audio system throws exceptions
     */
    public void openOutputLine(SourceDataLine s) throws Exception {
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

    /*
     *
     *
     * The "update()" function here is the center of timing, as it's
     * where we send the data to the speakers.
     *
     * By setting LOG_MAIN_OUT to true, you can watch the gory details
     * of the timing, i.e. how much time the synthesis is taking
     * and how long the system blocks before the next buffer.
     *
     * It proves that sometimes the gaps are the system's fault!
     *
     * See timing.md for how to interpret the output in "update.log."
     *
     */

    boolean DRY = false;

    boolean LOG_MAIN_OUT = App.LOG_MAIN_OUT;
    // WARNING - will overwrite update.log

    int outPos = 0;
    int loops = 0;
    long lastWrite = 0;
    File updateLog = new File("update.log");
    BufferedWriter log;
    {
        if (LOG_MAIN_OUT) {
            try {
                log = new BufferedWriter(new FileWriter(updateLog));
            } catch (Exception ex) {
                err.println("error opening update long " + ex);
            }
        }
    }
    void logWrite(String s) {
        try {
            log.write(s, 0, s.length());
            log.write("\n");
        }
        catch (Exception ex) { err.println("error writing to "+log); }
    }
    public void logFlush() {
        if (log == null) return;
        try { log.flush(); }
        catch (Exception ex) { err.println("Error flushing log."); }
    }

    public void update() {
        outputBuffer[outPos++] = inputSum();
        srcLineWrite();
    }

    void srcLineWrite() {
        if (srcLine.available() == 0 && outPos<outputBuffer.length) return;

        int lineFmtCount = toLineFmt(outputBuffer, outPos); // copies into lineBuffer
        outPos = 0;

        if (DRY) {
            //out.println("outputBuffer: "+Arrays.toString(outputBuffer));
            out.println("srcLine.write( disabled )");
        }
        else {
            long beforeWrite = System.nanoTime();

            //  Send the samples to the sound module.
            //  It blocks if needed.
            //
            int rs = srcLine.write(lineBuffer, 0, lineFmtCount);
            //out.print("wrote "+rs+" bytes.");

            long now=System.nanoTime();
            if (now - beforeWrite > timingOverflow) {
                // for a 2k buffer, the whole cycle needs to be under 46 ms.
                out.println(String.format(" >> srcLine.write block: %,d <<",(now-beforeWrite)));
            }

            if (LOG_MAIN_OUT) {
                // loops++; for limiting # of outputs

                logWrite("srcLine.write() rs="+rs+" bytes; "+
                    "   total >> "+ String.format("%,d ns",(now-lastWrite))+
                    "    write "+ String.format("%,d ns",(now-beforeWrite))+
                    "    process "+ String.format("%,d ns", (beforeWrite - lastWrite))
                );
            }
            lastWrite=now;
        }

    }



    @Override
    public void configure(Map config, Map components) {
        // as the final endpoint, this shouldn't need any connections....
        // outputs will connect TO here.
    }


    /**
     * As a singleton component that will always be
     * running when the synth is, we will not pause or resume.
     */
    @Override
    public void pause() { }

    /**
     * As a singleton component that will always be
     * running when the synth is, we will not pause or resume.
     */
    @Override
    public void resume() { }


    /**
     * This is the last stop on the route, and outputs directly
     * to the audio system, so we don't provide an output
     * other than that. It would be theoretically possible,
     * but easier to add a Junction between and take the output
     * from that.
     *
     * @return - null
     */
    @Override
    public WiredIntSupplier getMainOutput() { return null; }

    @Override
    public int currentValue() { return 0; }

    public int getSampleRate() {
        return sampleRate;
    }


}
