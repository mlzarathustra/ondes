package ondes.synth.voice;

import ondes.synth.Component;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;

public class Voice {
    HashMap<String,Component> components=new HashMap<>();

    @SuppressWarnings("unchecked,rawtypes")
    public Voice(Map info) {
        // step 1 : construct
        info.keySet().forEach(
            key -> {
                Object value=info.get(key);
                if (!(value instanceof Map)) return;
                Map valMap=(Map)info.get(key);
                Component c=Component.getComponent(valMap);
                if (c == null) {
                    err.println("ERROR - could not load component "+key);
                    err.println("  --> "+info.get(key));
                    System.exit(-1);
                }
                components.put(key.toString(), c);
            });
        // step 2 : configure
        //          (including: connect to other components)
        components.keySet().forEach( c->{
                Map valMap=(Map)info.get(c);
                components.get(c).configure(valMap,components);
            }
        );
    }

    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
