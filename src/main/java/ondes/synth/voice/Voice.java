package ondes.synth.voice;

import ondes.synth.Component;
import ondes.synth.EndListener;
import ondes.synth.OndesSynth;
import ondes.synth.mix.MainMix;

import javax.sound.midi.MidiMessage;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;

public class Voice {
    private HashMap<String,Component> components=new HashMap<>();
    private EndListener endListener;
    private OndesSynth synth;

    public void setEndListener(EndListener el) { endListener=el; }

    @SuppressWarnings("unchecked,rawtypes")
    Voice(Map info, OndesSynth synth) {
        this.synth = synth;
        // step 1 : construct components
        info.keySet().forEach(
            key -> {
                Object value=info.get(key);
                if (!(value instanceof Map)) return;
                Map valMap=(Map)info.get(key);
                Component c=Component.getComponent(valMap, synth);
                if (c == null) {
                    err.println("ERROR - could not load component "+key);
                    err.println("  --> "+info.get(key));
                    System.exit(-1);
                }
                components.put(key.toString(), c);
            });

        // step 1a: add the main mixer to the components.
        //    currently it is the only global component.
        components.put("main", synth.getMainMix());

        // step 2 : configure
        //          (including: connect to other components)
        components.keySet().forEach( c->{
                Map valMap=(Map)info.get(c);
                components.get(c).configure(valMap,components);
            }
        );
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
