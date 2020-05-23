package ondes.synth;

import ondes.synth.envelope.EnvMaker;
import ondes.synth.mix.Mixer;
import ondes.synth.wave.WaveMaker;

import java.util.Map;
import java.util.function.IntConsumer;

@SuppressWarnings("rawtypes")
public abstract class Component {

    public static Component getComponent(Map specs) {
        switch (specs.get("type").toString()) {

            case "wave":
                return WaveMaker.getWaveGen((String)specs.get("shape"));

            case "env":
                return EnvMaker.getEnv((String)specs.get("shape"));

            case "mix":
                return new Mixer();

            default:
                return null;
        }
    }

    public abstract void configure(Map config, Map components);
    public abstract void update();


    //  Naming is from the perspective of this component
    //
    public abstract WiredIntSupplier getOutput();
    public abstract IntConsumer getInput();


}
