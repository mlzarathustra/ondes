package ondes.synth.voice;

import java.nio.file.*;
import java.util.*;

import ondes.App;
import ondes.synth.OndeSynth;

import static java.lang.System.out;
import static java.lang.System.err;

import static java.util.stream.Collectors.*;
import static ondes.mlz.Util.listResourceFiles;
import static ondes.mlz.YamlLoader.*;

/**
 * <p>
 *     A "program" is a Map, defined by a YAML file.
 * </p>
 * <p>
 *     We load (on startup) from the resource directory 'program' and also
 *     the filesystem directory from the local dir named 'program.'
 *     The key "name" at the top level of the map is expected to be a String,
 *     and it will be used to identify the voice.
 * </p>
 * <p>
 *     The matching will be the same as with the devices, i.e.
 *     name.toLowerCase().contains(key.toLowerCase()).
 *     If more than one match, it will pick the first it encounters.
 *     Since we load the programs from the filesystem first,
 *     those should supersede any that match of the internal
 *     (resource) definitions.
 * </p>
 * <p>
 *     Because we're loading from Yaml, the Map is using non-generic types,
 *     hence the SuppressWarnings.
 * </p>
 *
 */
@SuppressWarnings("rawtypes")
public class VoiceMaker {

    static boolean DB=false;
    static int depth = 1;

    public static void setRecurseSubdirs(boolean b) {
        depth = b ? Integer.MAX_VALUE : 1;
    }

    static List<Map> programs=new ArrayList<>();

    /**
     * Add a program (Map) to the "programs" ArrayList.
     * It will be named (labeled) according to the filename it came from,
     * which is to say we add a "name" property to the program Map,
     * containing the filename minus the .yaml suffix.
     *
     * @param prog - the Map from YAML
     * @param fn - the filename of the file the YAML came from
     */
    @SuppressWarnings("unchecked")
    private static void addLabeledProg(Map prog, String fn) {
        if (prog == null) return; // file read error, reported elsewhere
        String name = fn.replaceAll("^.*\\\\","")  // one slash becomes 4!
            .replaceFirst("(?i).yaml$","");
        prog.put("name", name);
        programs.add(prog);
    }

    /**
     *  Populate the "programs" ArrayList with Maps
     *  defined by the YAML files in the "program" directory
     *
     */
    public static void loadPrograms() {

        // addLabeledProg() will add to here
        programs = new ArrayList<>();

        // First, load the internal resources.
        //
        List<String> fileNames = listResourceFiles("program");
        if (DB) out.println("resource files: "+fileNames);

        fileNames.forEach( fn -> {
            Map prog = loadResource("program/"+fn);
            addLabeledProg(prog, fn);

        });

        //  Then load the .yaml files from the filesystem,
        //  (in the ./program/ directory) so those will supersede
        //  any with similar names internally
        //
        try {
//            Path progPath = FileSystems.getDefault().getPath("program");  // JDK 8
//            Files.walk(progPath,depth).forEach( path -> {

            Files.walk(Path.of("program"),depth).
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
        programs.sort(Comparator.comparing(m->m.get("name").toString()));
    }
    
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////


    /**
     * Get program (Map) with the shortest name
     *          that contains the one given.
     *
     * Because: if you have "square" and "square plus,"
     * asking for "square" should give you the first one.
     *
     * @param progName - the name to search for
     * @return - a map representing the program.
     */
    public static Map findProg(String progName) {
        String lcProgName = progName.toLowerCase();

        List<Map> matches = programs.stream()
            .filter( m-> m.get("name").toString().toLowerCase()
                .contains(lcProgName) )
            .collect(toList());

        int minLen=Integer.MAX_VALUE;
        Map rs=null;
        for (Map m : matches) {
            int nameLen = m.get("name").toString().length();
            if (nameLen < minLen) {
                rs = m;
                minLen = nameLen;
            }
        }
        return rs;
    }

    /**
     * @param i - the index of the voice to load, origin 1
     *          (as displayed by showVoices())
     * @return - the Map of voice specifications
     */
    public static Map getVoiceMap(int i) {
        if (i < 1 || i > programs.size()) {
            err.println("Warning: cannot load voice #"+i+
                "; use -show-voices to display the list.");
        }
        return programs.get(i-1);
    }

    /**
     * Get the program (Map) indicated by progName, which may either
     * be a program "name" prefix, or an index to the programs listed
     * alphabetically
     *
     * @param progName - name or index
     * @return - Map of component specifications from YAML
     */
    public static Map getVoiceMap(String progName) {
        //  see if it's a plain index number first.
        try {
            int i = Integer.parseInt(progName);
            return getVoiceMap(i);
        }
        catch (NumberFormatException ignore) { }

        // if not, then load by name.
        progName = progName.replaceFirst("\\.yaml$","");
        Map m = findProg(progName);
        if (m == null) {
            err.println("Warning: Could not find program matching '"+
                progName+"'");
            App.quitOnError();  // load default program instead?
        }
        return m;
    }

    /**
     * @param progName - either a substring of the name or the index
     *       (origin 1) into the progNames list, as shown by showVoices()
     *
     * @param synth - the OndesSynth this voice will be associated with
     * @return - a new voice constructed from the specifications given
     */
    public static Voice getVoice(
        String progName,
        OndeSynth synth,
        ChannelVoicePool channelVoicePool) {
            Map m = getVoiceMap(progName);
            if (m == null) return null; // getVoiceMap will show an error
            return new Voice(m, synth, channelVoicePool);
    }

    public static void showPrograms() { showPrograms(false); }
    public static void showPrograms(boolean detail) {
        out.println("OndeSynth Programs\n" +
                    "------------------");
        int i=1;
        for (Map p : programs) {
            out.println(String.format("%5d: ",i++)+ p.get("name"));
            if (detail) showProgram(p.get("name").toString());
        }

    }

    @SuppressWarnings("unchecked")
    public static void showProgram(String progName) {
        Map m = getVoiceMap(progName);
        m.keySet().forEach( k-> {
            Object v = m.get(k);
            //out.println("  "+k+": "+v.getClass());
            out.println("  - "+k+": "+v);
        });
    }

    public static void main(String[] args) {
        loadPrograms();
        showPrograms(true);
    }
}
