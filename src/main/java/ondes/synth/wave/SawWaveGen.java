package ondes.synth.wave;

public class SawWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        double phi = phaseClock.getPhase();

        if (phi < 0.5) {
            return (int) (
                (4.0 * phi - 1.0) * getAmp()
            );
        }
        else {
            return (int) (
                (4.0 * (1.0 - phi) - 1.0) * getAmp()
            );
        }
    }


}
