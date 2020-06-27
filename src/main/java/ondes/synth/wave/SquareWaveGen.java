package ondes.synth.wave;

import static java.lang.System.out;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
class SquareWaveGen extends WaveGen {

    private double dutyCycle = 0.5;

    @Override
    public int currentValue() {
        modFreq();
        if (signed) {
            return (phaseClock.getPhase() > dutyCycle) ?
                getAmp() : -getAmp();
        }
        else {
            return (phaseClock.getPhase() > dutyCycle) ?
                0 : 2 * getAmp();
        }

    }

}
