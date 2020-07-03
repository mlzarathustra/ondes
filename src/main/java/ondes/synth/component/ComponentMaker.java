package ondes.synth.component;

import ondes.synth.OndeSynth;
import ondes.synth.envelope.Envelope;
import ondes.synth.envelope.Limiter;
import ondes.synth.filter.FilterMaker;
import ondes.synth.wave.WaveMaker;
import ondes.synth.wire.Controller;
import ondes.synth.wire.Junction;
import ondes.synth.wire.MidiNoteNum;
import ondes.synth.wire.OpAmp;
import ondes.synth.filter.Smooth;

import java.util.Map;

import static java.lang.System.err;

public class ComponentMaker {

    /**
     * <p>
     *      This should be the only place to acquire components from.
     *      Do not use the constructors directly.
     * </p>
     * <p>
     *      A component must know its synth, but handing it to the
     *      component constructors is awkward because WaveMaker is
     *      using a reflected constructor.
     * </p>
     *
     * @param specs - a map of specifications, otherwise known as a program
     *              typically from YAML
     *
     * @param synth - the synth which is using this component.
     * @return - a new Component as specified
     */
    public static MonoComponent getMonoComponent(Map specs, OndeSynth synth) {
        MonoComponent rs;
        if (specs.get("type") == null) {
            err.println("Missing 'type' property for component "+
                specs.get("name"));
            return null;
        }
        switch (specs.get("type").toString()) {

            case "wave":
                rs = WaveMaker.getWaveGen((String)specs.get("shape"));
                break;

            case "env": rs = new Envelope(); break;
            case "mix": rs = new Junction(); break;
            case "limiter": rs = new Limiter(); break;
            case "op-amp": rs = new OpAmp(); break;

            case "filter":
                rs = FilterMaker.getFilter((String)specs.get("shape"));
                break;

            case "controller": rs = new Controller(); break;
            case "smooth": rs = new Smooth(); break;
            case "midi-note": rs = new MidiNoteNum(); break;

            default:
                return null;
        }
        rs.setSynth(synth);
        return rs;
    }

}
