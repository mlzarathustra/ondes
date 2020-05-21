package ondes.synth.program;

import java.io.*;
import java.util.Map;

import org.yaml.snakeyaml.*;

import static java.lang.System.out;

public class Program {

    String getResourceAsString(String fileName) {
        StringBuilder sb=new StringBuilder();
        try {
            InputStream is=getClass()
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

    public static void main(String[] args) {

        String basicText = new Program().getResourceAsString("program/basic.yaml");
        Yaml yaml=new Yaml();
        Map basicMap=yaml.load(basicText);
        out.println(basicMap.keySet());





    }

}
