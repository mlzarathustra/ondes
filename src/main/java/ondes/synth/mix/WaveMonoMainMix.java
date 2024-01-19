package ondes.synth.mix;

import ondes.synth.OndeSynth;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class WaveMonoMainMix extends MainMix {

    private final int sampleRate;
    private final float ticksPerSample;
    long sampleNum = 0;
    List<MidiEvent> evtList;
    int evtIdx = 0;


    /**
     * How many 0's means "I'm finished."
     */
    int endingZeros = 100;

    /**
     *  # of seconds after the last MIDI event at which to
     *  fade, if it hasn't gone down to all 0's yet.
     */
    float fadeAfter;

    /**
     How many seconds to fade for, if needed.
     */
    float fadeLength;

    Long endTime = null; // in seconds
    int zeroCount = 0;

    float fadeSampleCount;
    int fadeSample;
    boolean fading = false;

    /**
     * <p>
     *      Once the last MIDI event has played, we wait for "endingZeros"
     *      0 values to pass by, and then tell the synth to stop.
     * </p>
     * <p>
     *      TODO -
     *      If that doesn't happen, after "fadeAfter" seconds, we initiate
     *      a fade of "fadeLength" seconds.
     * </p>
     *
     */
    void endingLogic() {
        if (evtIdx >= evtList.size()) {
            // crude but effective
            if (endTime == null) {
                endTime = (long)
                    (((float) sampleNum / (float) sampleRate) + fadeAfter);

//                out.println("sampleNum / sampleRate is "+((float) sampleNum / (float) sampleRate) );
//                out.println("endTime set to "+endTime);
//                out.println("fadeAfter is "+fadeAfter);

            }

            if (!fading && ((float)sampleNum / (float)sampleRate) >= endTime) {
                fading = true;
                fadeSampleCount = fadeSample = (int)(fadeLength * sampleRate);
            }
            if (fading && fadeSample <= 0) synth.stop = true;

            if (currentValue() == 0) zeroCount++;
            else zeroCount = 0;

            if (zeroCount > endingZeros) synth.stop = true;
        }
    }



    @Override
    public void update() {
        int curMidiTick = (int) (sampleNum * ticksPerSample);

        while (evtIdx < evtList.size() && evtList.get(evtIdx).getTick() <= curMidiTick) {
            synth.routeMidiMessage(evtList.get(evtIdx).getMessage(), 0);
            evtIdx++;
        }

        if (fading) {
            samples.add( (int) (currentValue() * ((float)fadeSample--)/fadeSampleCount) );
        }
        else {
            samples.add(currentValue());
        }
        sampleNum++;
        if ((sampleNum % 100) == 0) {
            out.print("\rsample[" + sampleNum + "]    ");
        }

        endingLogic();
    }



    OndeSynth synth;

    public void setSynth(OndeSynth synth) { this.synth = synth; }

    private final List<Integer> samples = new ArrayList<>();

    public WaveMonoMainMix(
        int sampleRate, float ticksPerSample, List<MidiEvent> evtList,
        float fadeAfter, float fadeLength, int endingZeros) {

            this.sampleRate = sampleRate;
            this.ticksPerSample = ticksPerSample;
            this.fadeAfter = fadeAfter;
            this.fadeLength = fadeLength;
            this.endingZeros = endingZeros;

            this.evtList = evtList;
    }

    public List<Integer> getSamples() { return samples; }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public int currentValue() {
        return inputSum();
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }




}
