package ondes.synth.program;

import java.util.*;

import ondes.synth.Component;
import org.yaml.snakeyaml.*;

import static java.lang.System.out;
import static java.lang.System.err;

import static ondes.mlz.Util.getResourceAsString;

public class Program {

    HashMap<String,Component> components=new HashMap<>();



    @SuppressWarnings("unchecked,rawtypes")
    public Program(Map info) {
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

    @SuppressWarnings("rawtypes")
    static Program load(String fileName) {
        String basicText = getResourceAsString(fileName);
        Yaml yaml=new Yaml();
        Map basicMap=yaml.load(basicText);
        // out.println(basicMap.keySet());
        return new Program(basicMap);
    }


    public static void main(String[] args) {
        Program p = load("program/basic.yaml");
    }
}
