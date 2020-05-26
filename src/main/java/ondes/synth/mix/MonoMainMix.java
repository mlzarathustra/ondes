package ondes.synth.mix;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntSupplier;

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
public class MonoMainMix extends MonoComponent {

    private boolean DB=true;

    SourceDataLine srcLine;
    private int sampleRate;

    private int bufSize=2048;
    private int bytesPerSample;

    private boolean signed, littleEndian;
    private int channels;  // 1=mono 2=stereo &c.

    private byte[] lineBuffer;
    private int[] outputBuffer;
    //


    public MonoMainMix(Mixer mixer) {
        Line.Info[] lineInfo = mixer.getSourceLineInfo();
        out.println(Arrays.toString(lineInfo));

        try {
            SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo[0]);
            out.println(line);
            // format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo,
            // 4 bytes/frame, little-endian

            openOutputLine(line);

        } catch (Exception ex) {
            out.println("Exception Caught: " + ex);
            ex.printStackTrace();
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
            bufSize = bufSize - (bufSize % frameSize);
        }
        int sb = audFmt.getSampleSizeInBits();
        bytesPerSample = (sb/8) + (sb%8 > 0 ? 1 : 0);

        outputBuffer = new int[bufSize]; // todo - this is mono
        lineBuffer = new byte[bufSize * bytesPerSample * channels];

        if (DB) {
            out.println(audFmt);
            out.println(
                "frameSize=" + frameSize + "; bufSize=" + bufSize +
                    "; bytesPerSample=" + bytesPerSample +
                    "; littleEndian=" + littleEndian +
                    "; # of channels=" + channels
            );
        }

        srcLine.open(audFmt,lineBuffer.length);
    }

    //  transform for endian-ness, multiple channels,
    //  unsigned samples.
    //
    private synchronized void toLineFmt(int[] outputBuffer) {
        byte[] split = new byte[bytesPerSample];
        int lbIdx=0;
        long unsignedOffset = signed ? 0 : 1 << (8*bytesPerSample - 1);
        for (int val : outputBuffer) {
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
    }

    boolean DRY = true;

    int outPos = 0;
    int loops = 0;

    public void update() {
        outputBuffer[outPos++] = inputs.stream()
            .mapToInt(IntSupplier::getAsInt)
            .sum();

        if (outPos == outputBuffer.length) {
            toLineFmt(outputBuffer); // copies into lineBuffer
            outPos = 0;

            if (DRY) {
                out.println("outputBuffer: "+Arrays.toString(outputBuffer));
            }
            else {

                //  Send the samples to the sound module.
                //  It blocks if needed.
                //
                int rs = srcLine.write(lineBuffer, 0, lineBuffer.length);

                if (DB && loops<100) {
                    System.out.println("audTrack.write() rs="+rs); loops++;
                }
            }
        }
    }

    @Override
    public void configure(Map config, Map components) {
        // as the final endpoint, this shouldn't need any connections....
        // outputs will connect TO here.
    }

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
