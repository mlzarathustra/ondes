package ondes.synth;

import ondes.synth.envelope.EnvGen;
import ondes.synth.mix.Mixer;
import ondes.synth.wave.WaveGen;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

public abstract class Component {

    //static HashMap<String,Class> componentMap;

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

}
