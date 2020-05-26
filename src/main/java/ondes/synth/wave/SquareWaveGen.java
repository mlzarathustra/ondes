package ondes.synth.wave;

import static java.lang.System.out;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SquareWaveGen extends WaveGen {

    private double dutyCycle = 0.5;

    @Override
    public int currentValue() {
//        out.print(phaseClock.getPhase()+" ");
//        out.println(" freq="+phaseClock.getFrequency());
//        out.println("sample number="+synth.getInstant().getSampleNumber());
        return  ((phaseClock.getPhase()>dutyCycle)?amp:-amp);
    }

}
