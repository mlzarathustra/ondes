package ondes.synth.filter;

import javax.sound.midi.MidiMessage;
import java.util.Map;
import static java.lang.System.err;
import static java.lang.System.out;

import ondes.midi.FreqTable;
import static ondes.synth.component.ConfigHelper.*;


/**
 *  Output the running average over an array of size arraySize().
 *  It results in a 'sinc' filter. It's low pass up to the frequency
 *  specified, then has a series of nodes with zeros at the harmonics
 *  of the frequency.
 *
 *  https://www.dsprelated.com/freebooks/sasp/Running_Sum_Lowpass_Filter.html
 */
public class SincFilter extends Filter {

    float freq = 0;
    float levelScale = 1;

    int arraySize() {
        if (freq == 0) return 0;
        return  (int)(
            (1.0/freq) * synth.getSampleRate()
        );
    }

    void setFreq(double freq) {
        this.freq = (float) freq;
    }


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

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }

        //
        if (freq == 0 && config.get("midi") == null) {
            err.println("You need to specify a freq: setting or midi: note-on. " +
                "Otherwise, the filter won't do anything.");
        }
    }

    int bufLen, bufIdx;
    int [] buf;
    boolean first;
    long sum;

    // todo - figure out how to manage buf for varying frequencies
    //        and avoid re-allocation.
    void reset() {
        if (freq > 0) {
            bufLen = arraySize();
            buf = new int[arraySize()];
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
                //((float)sum)/((float)bufIdx) :
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
        setFreq(FreqTable.getFreq(msg.getMessage()[1]));
        reset();
    }

    // the simplest filter: y[n] = x[n] + x[n-1]
    //
    // "moving average" implements a cutoff up to the
    // sampling frequency.

    int xn_1 = 0;

    @Override
    public int currentValue() {
        return nextAverage( (int)(levelScale * inputSum() ));
    }

}
