package ondes.synth.wave;

import static java.lang.System.out;

public class SawWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        modFreq();
        double phi = phaseClock.getPhase();
//        out.println("PWM phase = "+phi);
        int rs;

        if (phi < 0.5) {
            rs = (int) (
                (4.0 * phi - 1.0) * getAmp()
            );
        }
        else {
            rs = (int) (
                (4.0 * (1.0 - phi) - 1.0) * getAmp()
            );
        }

        if (!signed) rs += getAmp();
        return rs;
    }


}
