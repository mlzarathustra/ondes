package ondes.synth.wave;

import static ondes.synth.wave.lookup.SineLookup.sineLookup;
import static java.lang.Math.PI;

/**
 * Generate a sine wave
 */
class SineWaveGen extends WaveGen {

    @Override
    public int currentValue() {
        modFreq();
        int rs = (int)(
            sineLookup(phaseClock.getPhase() ) * getAmp()
        );
        if (!signed) rs += getAmp();
        return rs;
    }

}
