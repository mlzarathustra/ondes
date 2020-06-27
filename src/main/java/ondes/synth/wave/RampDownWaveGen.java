package ondes.synth.wave;

public class RampDownWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        modFreq();
        int rs = (int)(
             (1.0-phaseClock.getPhase()) * getAmp());

        if (signed) rs = rs * 2 - getAmp();
        return rs;
    }


}
