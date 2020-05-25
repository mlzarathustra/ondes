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


    protected Instant.PhaseClock phaseClock;


    /**
     * reset the note to zero
     */
    abstract void reset();

    double freq = 440;
    int amp = 32767;  // assume 16-bits (signed) for now.

    void setFreq(double freq) { this.freq = freq; }


}
