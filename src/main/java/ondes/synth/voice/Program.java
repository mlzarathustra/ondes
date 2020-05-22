package ondes.synth.voice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.*;

import static java.lang.System.out;
import static java.lang.System.err;

import static ondes.mlz.Util.getResourceAsString;
import static ondes.mlz.Util.listResourceFiles;

public class Program {

    static final List<Map> programs=new ArrayList<Map>();



    @SuppressWarnings("rawtypes")
    static Map loadResource(String fileName) {
        String basicText = getResourceAsString(fileName);
        Yaml yaml=new Yaml();
        return yaml.load(basicText);
    }
    @SuppressWarnings("rawtypes")


    static void loadPrograms() {
        List<String> fileNames = listResourceFiles("program");
        out.println("resource files: "+fileNames);

        fileNames.forEach( fn -> {
            Map prog = loadResource("program/"+fn);
            if (prog.get("name") == null) {
                err.println("Warning: resource file program/"+fn
                    +" has no name label, \n    >>> so it won't be accessible.");
            }
            else programs.add(prog);
        });

        try {
            Files.walk(Path.of("program")).forEach( path -> {
                out.println("path: "+path);
            });
        }
        catch (Exception ignore) {
            // this happens if the directory isn't there, which is
            // no big deal.
        }
    }


    public static void main(String[] args) {
        loadPrograms();
        programs.forEach( out::println );
    }
}
