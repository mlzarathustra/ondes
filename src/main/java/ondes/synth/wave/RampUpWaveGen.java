package ondes.synth.wave;

public class RampUpWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        return (int)(
            (phaseClock.getPhase() * 2.0 - 1.0)
                * getAmp());
    }


}
