package ondes.synth.wave;

import ondes.synth.Instant;
import ondes.synth.wire.WiredIntSupplier;

import java.util.function.IntConsumer;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SquareWaveGen extends WaveGen {

    private double phase = 0; // range: 0-1


    private double dutyCycle = 0.5;

    int currentValue() {
        return  ((phase>dutyCycle)?amp:-amp);
    }



    @Override
    void reset() {
        phase = 0;
    }

    @Override
    public void update(Instant now) {

    }

    @Override
    public WiredIntSupplier getOutput() {
        WiredIntSupplier wireOut =  new WiredIntSupplier() {
            public int updateInputs() { return currentValue(); }
        };
        outputs.add(wireOut);
        return wireOut;
    }

    @Override
    public IntConsumer getInput() {

        //  TODO - should be able to add LFO or ENV

        return null;
    }


}
