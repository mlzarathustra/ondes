package ondes.synth.mix;

import ondes.synth.Component;
import ondes.synth.wire.WiredIntSupplier;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * The inputs to this class will direct output
 * to the Javasound audio system.
 *
 */
public class MainMix extends Component {

    ArrayList<WiredIntSupplier> inputs;


    @Override
    public void configure(Map config, Map components) {
        // as the final endpoint, this shouldn't need any connections....
        // outputs will connect TO here.
    }

    @Override
    public void update() {


    }

    @Override
    public WiredIntSupplier getOutput() {
        return null;
    }

    @Override
    public IntConsumer getInput() {
        return null;
    }
}
