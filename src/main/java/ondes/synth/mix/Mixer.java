package ondes.synth.mix;

import ondes.synth.Component;
import ondes.synth.WiredIntSupplier;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Mixer extends Component {

    ArrayList<IntSupplier> inputs;
    ArrayList<IntConsumer> outputs;

    //  TODO - this won't quite work maybe?
    //
    public void update() {
        int level = inputs.stream()
            .mapToInt(IntSupplier::getAsInt)
            .sum();

        for (IntConsumer out : outputs) {
            out.accept(level);
        }
    }

    @Override
    public WiredIntSupplier getOutput() {
        return null;
    }

    @Override
    public IntConsumer getInput() {
        return null;
    }

    public void configure(Map config, Map components) {

        //  TODO - implement

    }

}
