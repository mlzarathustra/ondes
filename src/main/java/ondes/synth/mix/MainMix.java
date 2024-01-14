package ondes.synth.mix;

import ondes.synth.component.ComponentContext;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

public abstract class MainMix extends MonoComponent {

    public void logFlush() {}

    public int getSampleRate() { return 0; }


    // where MonoMainMix writes to srcLine
    public void update() { }

}
