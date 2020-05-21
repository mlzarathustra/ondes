package ondes.synth.program;

import java.io.*;
import java.util.*;

import ondes.synth.Component;
import org.yaml.snakeyaml.*;

import static java.lang.System.out;
import static java.lang.System.err;

public class Program {

    HashMap<String,Component> components=new HashMap<>();

    static String getResourceAsString(String fileName) {
        StringBuilder sb=new StringBuilder();
        try {
            InputStream is=Program.class
                .getClassLoader()
                .getResourceAsStream(fileName);
            if (is == null) {
                throw new Exception(
                    "can't get input stream for resource "+fileName);
            }
            BufferedReader in = new BufferedReader(
                new InputStreamReader(is));

            for (;;) {
                String line = in.readLine();
                if (line == null) break;
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

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
