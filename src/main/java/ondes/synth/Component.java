package ondes.synth;

import ondes.synth.envelope.EnvGen;
import ondes.synth.mix.Mixer;
import ondes.synth.wave.WaveGen;

import java.util.Map;

import static java.lang.System.out;

@SuppressWarnings("rawtypes")
public abstract class Component {

    public static Component getComponent(Map specs) {
        switch (specs.get("type").toString()) {

            case "wave":
                return WaveGen.getWaveGen((String)specs.get("shape"));

            case "env":
                return new EnvGen((String)specs.get("shape"));

            case "mix":
                return new Mixer();

            default:
                return null;
        }
    }

    public abstract void configure(Map config, Map components);
    public abstract void update();

}
