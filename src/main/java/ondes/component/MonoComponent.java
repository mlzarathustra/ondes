package ondes.component;

import ondes.synth.OndesSynth;
import ondes.synth.wire.WiredIntSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

@SuppressWarnings("rawtypes")
public abstract class MonoComponent {

    /**
     * "mainOutput" is the IntSupplier that a target component
     * can use as input.
     *
     * In some cases there may be more than one output
     * but all components will have at least one.
     *
     */
    public WiredIntSupplier mainOutput = null;

    /**
     * Multiple inputs are common - e.g. ENV and LFO
     * The below implies they will be summed together.
     *
     * Components with alternate inputs will have to define
     * their own. For example, a delay could intake a signal
     * and be modulated by a separate LFO.
     *
     * The output value at each sample is cached by WiredIntSupplier,
     * to avoid infinite looping (e.g. if a component uses its
     * output as input, common in FM synthesis). So the "visited"
     * flag needs to be reset for every sample.
     *
     * Getting the output value is basically a depth-first walk.
     */
    public List<WiredIntSupplier> inputs = new ArrayList<>();

    protected OndesSynth synth;

    public abstract void configure(Map config, Map components);

    void setSynth(OndesSynth s) { synth = s; }

    /**
     * Provide the output of this component to another component.
     * <br/><br/>
     *
     * The subclass generally MUST define "mainOutput," as it's the
     * whole point of various components that the functionality of
     * the output depends on the component.
     * <br/><br/>
     *
     * The exception is MainMix, which outputs by a whole other
     * means to the audio system.
     *
     * @return - the supplier of output data
     */
    public WiredIntSupplier getMainOutput() {
        return mainOutput;
    }

    /**
     * By default, we just add whatever inputs we are requested to.
     * If a component wants to get fussy about rejecting them, it
     * can override this method.
     *
     * @param input - the input to add.
     */
    public void addInput(WiredIntSupplier input) {
        inputs.add(input);
    }


}
