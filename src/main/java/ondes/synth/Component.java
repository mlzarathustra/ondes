package ondes.synth;

import ondes.synth.envelope.EnvMaker;
import ondes.synth.mix.Mixer;
import ondes.synth.wave.WaveMaker;
import ondes.synth.wire.WiredIntSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

@SuppressWarnings("rawtypes")
public abstract class Component {

    //  Need to be public so Wires can see them (for release())
    public List<WiredIntSupplier> outputs = new ArrayList<>();
    public List<IntConsumer> inputs = new ArrayList<>();

    protected OndesSynth synth;

    /**
     * This should be the only place to acquire components from.
     * Do not use the constructors directly.
     * <br/><br/>
     *      *
     * @param specs - a map of specifications, otherwise known as a program
     *              typically from YAML
     *
     * @param synth - the synth which is using this component.
     *
     *      A component must know its synth so release() can figure out which
     *      wires to remove at the end (by looking them up in the tangle).
     *      <br/><br/>
     *
     *      But handing it to the component constructors is awkward
     *      because WaveMaker is using a reflected constructor.
     *      <br/><br/>
     * @return
     */
    public static Component getComponent(Map specs, OndesSynth synth) {
        Component rs;
        switch (specs.get("type").toString()) {

            case "wave":
                rs = WaveMaker.getWaveGen((String)specs.get("shape"));
                break;

            case "env":
                rs = EnvMaker.getEnv((String)specs.get("shape"));
                break;

            case "mix":
                rs = new Mixer();
                break;

            default:
                return null;
        }
        rs.setSynth(synth);
        return rs;
    }

    public abstract void configure(Map config, Map components);
    public abstract void update();

    void setSynth(OndesSynth s) { synth = s; }

    /**
     * removes all Wire connections to and from this component.
     */
    public void release() {
        outputs.forEach( o -> synth.getTangle().remove(o) );
        inputs.forEach( i -> synth.getTangle().remove(i) );

    }


    //  "output" from the perspective of this component
    //
    public abstract WiredIntSupplier getOutput();
    public abstract IntConsumer getInput();


}
