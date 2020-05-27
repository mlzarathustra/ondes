package ondes.synth.wave;

import static ondes.mlz.SineLookup.sineLookup;
import static java.lang.Math.PI;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SineWaveGen extends WaveGen {

    static double TAO = PI*2;

    int sinScale = 2; // sine waves are very quiet
    // we may want to boost more for the lower freqs

    @Override
    public int currentValue() {
        return  (int)(
            sineLookup(phaseClock.getPhase() * TAO) *
                getAmp() * sinScale
        );
    }

}
