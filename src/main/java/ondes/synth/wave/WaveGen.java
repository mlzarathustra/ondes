package ondes.synth.wave;

import ondes.component.MonoComponent;
import ondes.synth.DeltaListener;
import ondes.synth.Instant;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 *
 *
 */
public abstract class WaveGen extends MonoComponent implements DeltaListener {
    /**
     * reset the note to zero
     */
    abstract void reset();

    double freq = 440;
    int amp = 32767;  // assume 16-bits (signed) for now.

    void setFreq(double freq) { this.freq = freq; }

//    @SuppressWarnings("rawtypes")
//    public void configure(Map config, Map components) {
//
//        //  TODO -  each individual class will probably need its own one of these.
//        //     this parent class should probably have IntConsumer and IntSupplier lists.
//
//    }

    public void update() {
        //  TODO -  each individual class will probably need its own one of these.

    }

    //  to implement DeltaListener
    abstract public void update(Instant now);


}
