package ondes.synth.wave;

public class RampDownWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        return (int)(
            ( (1.0-phaseClock.getPhase()) * 2.0 - 1.0)
                * getAmp());
    }


}
