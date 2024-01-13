package ondes.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.err;
import static java.lang.System.out;

import static ondes.mlz.Util.listResourceFiles;
import static ondes.mlz.YamlLoader.*;

/**
 *  The collection of waves in the filesystem.
 *  Modeled after ondes.synth.voice.VoiceMaker
 */
@SuppressWarnings("unchecked,rawtypes")
public class WaveFile {
    static boolean DB=false;
    static int depth = 1;
    static final String WAVE_PATH = "waves";

    static {
        loadWaves();
    }

    public static void setRecurseSubdirs(boolean b) {
        depth = b ? Integer.MAX_VALUE : 1;
    }

    private static List<Map> progList;
    private static String[] waveNames = {};
    private static Map<String,Map> progMap;

    public static String[] getWaveNames() {
        return waveNames;
    }
    public static Map getWaveProgram(String name) { return progMap.get(name); }

    private static void addLabeledProg(Map prog, String fn) {
        if (prog == null) return; // file read error, reported elsewhere
        String name = fn.replaceAll("^.*\\\\","")  // one slash becomes 4!
            .replaceFirst("(?i).yaml$","");
        prog.put("name", name);
        progList.add(prog);
        progMap.put(name,prog);
    }

    public static synchronized void loadWaves() {
        progList = new ArrayList<>();
        progMap = new HashMap<>();

        // First, load the internal resources.
        //
        List<String> fileNames = listResourceFiles(WAVE_PATH);
        if (DB) out.println("resource files: "+fileNames);

        fileNames.forEach( fn -> {
            Map prog = loadResource(WAVE_PATH+"/"+fn);
            addLabeledProg(prog, fn);

        });

        // Then load the waves from the filesystem.
        //
        try {
            // Path progPath = FileSystems.getDefault().getPath("program");  // JDK 8
            // Files.walk(progPath,depth).forEach( path -> {

            Files.walk(Path.of(WAVE_PATH),depth).
                sorted( Comparator.comparing(
                    p-> (""+p.getFileName()).replaceFirst("\\.yaml$","" ))
                )
                .forEach( path -> {
                    Map prog = loadFile(path);
                    if (prog == null) return;
                    addLabeledProg(prog, path.toString());
                });


        }
        catch (Exception ignore) {
            // this happens if the directory isn't there, which is
            // no big deal.
        }
        // Now create the required ARRAY of strings for the JComboBox
        waveNames = new String[progList.size()];
        for (int i=0; i<waveNames.length; ++i) {
            waveNames[i] = ""+ progList.get(i).get("name");
        }

    }

    static String writeNextFile(String text) {
        String format = "wave-%04d.yaml";
        File parentDir = new File(WAVE_PATH);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            err.println("Can't write to WAVE_PATH!");
            return "NOT WRITTEN";
        };
        File f;
        for (int i=0;; ++i) {
            f=new File(parentDir, String.format(format, i));
            if (!f.exists()) break;
        }
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(text);
            fw.flush();
            out.println("wrote to file "+f);
        }
        catch (IOException ex) {
            err.println(ex);
        }
        return ""+f;
    }

}
