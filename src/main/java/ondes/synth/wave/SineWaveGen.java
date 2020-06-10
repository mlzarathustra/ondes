package ondes.synth.wave;

import static ondes.synth.wave.lookup.SineLookup.sineLookup;
import static java.lang.Math.PI;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SineWaveGen extends WaveGen {

    static double TAO = PI*2;

    @Override
    public int currentValue() {
        int rs = (int)(
            sineLookup(phaseClock.getPhase() * TAO) * getAmp()
        );
        if (!signed) rs += getAmp();
        return rs;
    }

}
