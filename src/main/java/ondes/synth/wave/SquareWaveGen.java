package ondes.synth.wave;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SquareWaveGen extends WaveGen {

    private double dutyCycle = 0.5;

    @Override
    public int currentValue() {
        return  ((phaseClock.getPhase()>dutyCycle)?amp:-amp);
    }

}
