package ondes.synth.mix;

import ondes.synth.OndeSynth;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class WaveMonoMainMix extends MainMix {

    private final int sampleRate;
    private final float ticksPerSample;
    int sampleNum = 0;
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

    Integer endTime = null;
    int zeroCount = 0;

    @Override
    public void update() {
        int curMidiTick = (int) (sampleNum * ticksPerSample);

        while (evtIdx < evtList.size() && evtList.get(evtIdx).getTick() <= curMidiTick) {
//            out.println("curMidiTick = " + curMidiTick + "; evtList.get(evtIdx).getTick()="+
//                evtList.get(evtIdx).getTick());

            synth.routeMidiMessage(evtList.get(evtIdx).getMessage(), 0);
            evtIdx++;
        }

        samples.add(currentValue());
        sampleNum++;
        if ((sampleNum % 100) == 0) {
            out.print("\rsample[" + sampleNum + "]    ");
        }

        //  TODO - implement fadeAfter and fadeLength
        //
        if (evtIdx >= evtList.size()) {
            // crude but effective
            if (endTime == null) endTime = (int)
                ( ((float)sampleNum/(float)sampleRate) + fadeAfter );

            if (((float)sampleNum / (float)sampleRate) >= endTime) {
                synth.stop = true;
            }

            if (currentValue() == 0) zeroCount++;
            else zeroCount = 0;

            if (zeroCount > endingZeros) synth.stop = true;


        }
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
