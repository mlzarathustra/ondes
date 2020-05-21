package ondes.synth.mix;

import ondes.synth.Component;
import ondes.synth.WiredIntSupplier;

import java.util.Map;

/**
 * The inputs to this class will direct output
 * to the Javasound audio system.
 *
 */
public class MainMix extends Component {

    WiredIntSupplier in;

    @Override
    public void configure(Map config, Map components) {

    }

    @Override
    public void update() {


    }
}
