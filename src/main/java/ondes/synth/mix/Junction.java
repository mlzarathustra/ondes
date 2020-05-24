package ondes.synth.mix;

import ondes.synth.Component;
import ondes.synth.wire.WiredIntSupplier;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Junction extends Component {

    ArrayList<IntSupplier> inputs;
    ArrayList<IntConsumer> outputs;


    IntConsumer output = new IntConsumer() {
        @Override
        public void accept(int value) {
            int level = inputs.stream()
                .mapToInt(IntSupplier::getAsInt)
                .sum();

            for (IntConsumer out : outputs) {
                out.accept(level);
            }

        }
    };

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

    //  If the wires + phase clocks drive all the data flow
    //  this won't be needed
    public void update() { }

}
