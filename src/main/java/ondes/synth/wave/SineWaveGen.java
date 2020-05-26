package ondes.synth.wave;

import static java.lang.Math.sin;
import static java.lang.Math.PI;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SineWaveGen extends WaveGen {

    static double TAO = PI*2;

    int scale = 2; // sine waves are very quiet

    @Override
    public int currentValue() {
        return  (int)(
            sin(phaseClock.getPhase() * TAO) * amp * scale
        );
    }

}
