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
        info.keySet().forEach(
            key -> {
                Object val=info.get(key);
                if (!(key instanceof Map)) return;
                Component c=Component.getComponent((Map)info.get(key));
                if (c == null) {
                    err.println("ERROR - could not load component "+key);
                    err.println("  --> "+info.get(key));
                    System.exit(-1);
                }
                components.put(key.toString(), c);
            });
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {

        String basicText = getResourceAsString("program/basic.yaml");
        Yaml yaml=new Yaml();
        Map basicMap=yaml.load(basicText);
        out.println(basicMap.keySet());

        Program prog = new Program(basicMap);





    }

}
