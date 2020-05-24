package ondes.synth.wave;

import ondes.synth.Instant;
import ondes.synth.wire.WiredIntSupplier;

import java.util.Map;
import java.util.function.IntConsumer;

import static java.lang.System.out;

/**
 * Generate a plain square wave, with duty cycle of 0.5
 */
class SquareWaveGen extends WaveGen {

    Instant.PhaseClock phaseClock;

    private double dutyCycle = 0.5;

    int currentValue() {
        return  ((phaseClock.getPhase()>dutyCycle)?amp:-amp);
    }

    //  TODO - can we move configure up to the WaveGen level?
    //             It has to set up the phase clock(s).

    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        phaseClock = synth.getInstant().addPhaseClock();
        // Note ON will set frequency

//        out.println("configure() - midi: " + config.get("midi"));
//        out.println("config: "+config);
//        out.println("out type: "+config.get("out").getClass());
//        out.println("outList type: "+config.get("outList").getClass());




    }

    @Override
    void reset() {
    }

    @Override
    public void update(Instant now) {
    }

    @Override
    public WiredIntSupplier getMainOutput() {
        WiredIntSupplier wireOut =  new WiredIntSupplier() {
            public int updateInputs() { return currentValue(); }
        };
        return wireOut;
    }

    @Override
    public void setMainOutput() {

    }


}
