package ondes.synth.filter;

import ondes.midi.FreqTable;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.lang.Math.*;

/**
 *  Output the running average over an array of size arraySize().
 *  It results in a 'sinc' filter. It's low pass up to the frequency
 *  specified, then has a series of nodes with zeros at the harmonics
 *  of the frequency.
 *
 *  https://www.dsprelated.com/freebooks/sasp/Running_Sum_Lowpass_Filter.html
 */
public class SweepingSincFilter extends Filter {

    float freq = 0;
    float levelScale = 1;
    float inputAmp = 0, inputAmpInv = 0;

    float sweepWidth; // in semitones

    int bufLen, bufIdx;
    int [] buf;
    boolean first;
    long sum;

    /**
     * <p>
     *     A simple low pass filter with (so far) a fixed frequency
     * </p>
     * @param config - the configuration map from YAML
     * @param components - a map of all the components
     *                   in this voice, by name.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Float fltInp;
        fltInp = getFloat(config.get("freq"),
            "'freq' must be a number. can be floating.");
        if (fltInp != null) freq = (float) fltInp;

        Double dblInp= getDouble(config.get("input-amp"),
            "'input-amp' must be a number, typically " +
                "the same as the level-override of the sender.");
        if (dblInp != null) {
            inputAmp = dblInp.floatValue();
            if (inputAmp != 0) inputAmpInv = 1.0f / inputAmp;
        }

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }

        String sweepWidthErr = "Sweep width must be a number. (semitones)";
        fltInp = getFloat(config.get("sweep-width"), sweepWidthErr);
        if (fltInp != null) { sweepWidth = fltInp; }
       //
        if (freq == 0 && config.get("midi") == null) {
            err.println("You need to specify a freq: setting or midi: note-on. " +
                "Otherwise, the filter won't do anything.");
        }
    }


    /**
     * @param freq the base frequency
     *
     * @return - the maximum array size required to sweep this frequency
     * as low as possible (i.e. to hold the longest wavelength)
     * We won't use the whole array for the higher frequencies.
     * Pad by 1 to be safe from arithmetic inaccuracies.
     */
    int arraySize(float freq) {
        if (freq == 0) return 0;
        float sweptFreq =
            getSweptFreq(-inputAmp);

        return  (int)(1.0 + bufLen(sweptFreq));
    }

    int bufLen(float freq) {
        return (int) (
            (1.0/freq) * synth.getSampleRate()
        );
    }

    void setFreq(double freq) {
        this.freq = (float) freq;
    }

    float getSweptFreq(float sweepAmt) {
        return (float) (
            freq *
            pow(2, (((inputAmpInv * sweepAmt)*sweepWidth)/12.0))
        );


    }

    void reset() {
        if (freq > 0) {
            buf = new int[arraySize(freq)];
            bufLen = bufLen(freq);

            //out.println("buf.length="+buf.length+"; bufLen="+bufLen);

            bufIdx = 0;
            sum = 0;
            first = true;
        }
        else buf = null;
    }

    int nextAverage(int n) {
        if (buf == null) return n;

        if (!first) {
            sum -= buf[bufIdx];
        }
        if (bufIdx == bufLen - 1) {
            first = false;
        }
        sum += n;
        buf[bufIdx] = n;
        bufIdx = (bufIdx+1) % bufLen;

        return (int)
            (first ? 0 : // it makes noise at the beginning otherwise
                ((float)sum)/((float)bufLen)
        );
    }


    @Override
    public void pause() {
        buf = null;

    }

    @Override
    public void resume() {
        reset();
    }

    @Override
    public void noteON(MidiMessage msg) {
        //out.println("sweeping sinc filter: note ON");
        setFreq(FreqTable.getFreq(msg.getMessage()[1]));
        reset();
    }

    float lastSweepAmt =0;
    private void modFreq(float sweepAmt) {
        // limit to inputAmp (signed) max
        sweepAmt = min(abs(sweepAmt),inputAmp) * signum(sweepAmt);

        if (sweepAmt != lastSweepAmt) {
            lastSweepAmt = sweepAmt;
            float sweptFreq = getSweptFreq(sweepAmt);


            int oldBufLen = bufLen;
            bufLen = bufLen(sweptFreq);

            if (bufLen < oldBufLen) {

                // buffer is shrinking
                // the rule is: always discard the oldest data

                if (bufLen > bufIdx) {
                    System.arraycopy(
                        buf, oldBufLen - (bufLen - bufIdx),
                        buf, bufIdx,
                        bufLen - bufIdx);
                } else {
                    System.arraycopy(
                        buf, bufLen,
                        buf, 0,
                        bufIdx - bufLen
                    );
                }

                bufIdx = bufIdx % bufLen;
            }
        }
        else {
            Arrays.fill(buf,bufLen,buf.length,0);
        }
    }



    @Override
    public int currentValue() {
        float sweepAmt=0;
        List<WiredIntSupplier> sweepInputs = namedInputs.get("sweep");
        if (sweepInputs != null) {
            for (WiredIntSupplier input : sweepInputs) {
                sweepAmt += input.getAsInt();
            }
        }
        modFreq(sweepAmt);

        int inputSum=0;
        for (WiredIntSupplier input : inputs) inputSum += input.getAsInt();

        return nextAverage((int)(levelScale * inputSum));
    }

}
