package ondes.synth.wave;

public class RampUpWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        int rs = (int)(
            phaseClock.getPhase() * getAmp());

        if (signed) rs = rs * 2 - getAmp();
        return rs;
    }


}
