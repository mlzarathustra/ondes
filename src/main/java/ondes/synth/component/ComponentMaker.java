package ondes.synth.component;

import ondes.synth.OndesSynth;
import ondes.synth.envelope.EnvMaker;
import ondes.synth.wave.WaveMaker;
import ondes.synth.wire.Junction;

import java.util.Map;

public class ComponentMaker {

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
     *
     * @return - a new Component as specified
     */
    public static MonoComponent getMonoComponent(Map specs, OndesSynth synth) {
        MonoComponent rs;
        switch (specs.get("type").toString()) {

            case "wave":
                rs = WaveMaker.getWaveGen((String)specs.get("shape"));
                break;

            case "env":
                rs = EnvMaker.getEnv((String)specs.get("shape"));
                break;

            case "mix":
                rs = new Junction();
                break;

            default:
                return null;
        }
        rs.setSynth(synth);
        return rs;
    }

}
