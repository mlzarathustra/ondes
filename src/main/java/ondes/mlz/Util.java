package ondes.mlz;

import ondes.synth.program.Program;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * a place to keep static utility functions
 *
 */
public class Util {
    public static String keyStrip(String s) {
        return s.toLowerCase().replaceAll("[^a-z]","");
    }

    public static String getResourceAsString(String fileName) {
        StringBuilder sb=new StringBuilder();
        try {
            InputStream is= Program.class
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
}
