package ondes.synth.voice;

import ondes.component.MonoComponent;
import ondes.component.ComponentMaker;
import ondes.synth.EndListener;
import ondes.synth.OndesSynth;

import javax.sound.midi.MidiMessage;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;

@SuppressWarnings("FieldMayBeFinal")
public class Voice {
    private HashMap<String, MonoComponent> components=new HashMap<>();
    private EndListener endListener;
    private OndesSynth synth;

    public void setEndListener(EndListener el) { endListener=el; }

    @SuppressWarnings("unchecked,rawtypes")
    Voice(Map voiceSpec, OndesSynth synth) {
        this.synth = synth;
        // step 1 : construct components
        for (Object key : voiceSpec.keySet()) {
            Object value=voiceSpec.get(key);
            if (!(value instanceof Map)) continue;
            Map valMap=(Map)voiceSpec.get(key);
            MonoComponent c= ComponentMaker.getMonoComponent(valMap, synth);
            if (c == null) {
                err.println("ERROR - could not load component "+key);
                err.println("  --> "+voiceSpec.get(key));
                System.exit(-1);
            }
            components.put(key.toString(), c);
        }

        // step 1a: add the main mixer to the components.
        //    currently it is the only global component.
        components.put("main", synth.getMonoMainMix());

        // step 2 : configure
        //          (including: connect to other components)
        for (String compKey : components.keySet()) {
            Map compSpec=(Map)voiceSpec.get(compKey);
            components.get(compKey).configure(compSpec,components);
        }
    }

    public void noteON(MidiMessage msg) {

    }

    public void noteOFF(MidiMessage msg) {

        //  TODO - below is the default behavior
        //           but if there is an env generator, let IT trigger the end.
        //
        endListener.noteEnded(msg);
    }


    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
