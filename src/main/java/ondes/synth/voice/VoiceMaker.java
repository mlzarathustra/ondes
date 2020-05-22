package ondes.synth.voice;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.yaml.snakeyaml.*;

import static java.lang.System.out;
import static java.lang.System.err;

import static java.util.stream.Collectors.joining;
import static ondes.mlz.Util.getResourceAsString;
import static ondes.mlz.Util.listResourceFiles;

/**
 * A "program" is simply a Map.
 * <br/><br/>
 *
 * We load (on startup) from the resource directory 'program' and also
 * the filesystem directory from the local dir named 'program.'
 * The key "name" at the top level of the map is expected to be a String,
 * and it will be used to identify the voice.
 * <br/><br/>
 *
 * The matching will be the same as with the devices, i.e.
 * name.toLowerCase().contains(key.toLowerCase()).
 * If more than one match, it will pick the first it encounters.
 * Since we load the programs from the filesystem first, those should
 * supersede any that match of the internal (resource) definitions.
 * <br/><br/>
 *
 * Because we're loading from Yaml, the Map is using non-generic types,
 * hence the SuppressWarnings.
 * <br/><br/>
 *
 */
@SuppressWarnings("rawtypes")
public class VoiceMaker {

    static final List<Map> programs=new ArrayList<Map>();

    private static void addLabeledProg(Map prog, String fn) {
        if (prog == null) return; // file read error, reported elsewhere

        if (prog.get("name") == null) {
            err.println("Warning:  program/"+fn
                +" has no name label," +
                "\n    >>> so the program won't be accessible.");
        }
        else if (! (prog.get("name") instanceof String)) {
            err.println("Warning:  program/"+fn+
                ": 'name' key at the top level is not a String" +
                "\n    >>> so the program won't be accessible.");
        }
        else programs.add(prog);

    }

    static Map loadFile(Path path) {
        out.println("path: "+path);
        if (path.toFile().isDirectory()) return null;
        try {
            String text = Files.lines(path, StandardCharsets.UTF_8)
                .collect(joining("\n"));
            Yaml yaml=new Yaml();
            return yaml.load(text);
        }
        catch (Exception ex) {
            err.println("Exception reading file "+path+": "+ex);
            return null;
        }
    }

    static Map loadResource(String resName) {
        String text = getResourceAsString(resName);
        Yaml yaml=new Yaml();
        return yaml.load(text);
    }
    static void loadPrograms() {

        //  First load the .yaml files from the filesystem,
        //  (in the ./program/ directory) so those will supersede
        //  any with similar names internally
        //
        try {
            Files.walk(Path.of("program")).forEach( path -> {
                Map prog = loadFile(path);
                if (prog == null) return;
                addLabeledProg(prog, path.toString());

            });
        }
        catch (Exception ignore) {
            // this happens if the directory isn't there, which is
            // no big deal.
        }

        // Next, load the internal resources.
        //
        List<String> fileNames = listResourceFiles("program");
        out.println("resource files: "+fileNames);

        fileNames.forEach( fn -> {
            Map prog = loadResource("program/"+fn);
            addLabeledProg(prog, fn);

        });

    }

    public static Map findProg(String progName) {
        progName = progName.toLowerCase();
        for (Map m : programs) {
            if (m.get("name").toString()
                .toLowerCase()
                .contains(progName)) return m;
        }
        return null;
    }

    public static Voice getVoice(String progName) {
        Map m = findProg(progName);
        if (m == null) {
            err.println("Warning: Could not find program matching '"+
                progName+"'");
            System.exit(-1);  // load default program instead?
        }


            // TODO - implement


        return null;
    }


    public static void main(String[] args) {
        loadPrograms();
        programs.forEach( out::println );
    }
}
