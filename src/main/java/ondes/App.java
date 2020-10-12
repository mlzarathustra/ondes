package ondes;

import ondes.midi.MlzMidi;
import ondes.synth.OndeSynth;
import ondes.synth.voice.VoiceMaker;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.err;
import static java.lang.System.out;

import static java.util.stream.Collectors.toList;
import static ondes.mlz.Util.getResourceAsString;

/**
 *  This "main" class is responsible for the following:
 *
 * <ol>
 *     <li>
 *         Parsing command line arguments and
 *         giving usage information
 *     </li>
 *     <li>
 *         Getting the Audio connection for output,
 *         given a string to match its label
 *     </li>
 *     <li>
 *         Getting the MIDI connection for input,
 *         given a string to match its label
 *     </li>
 *     <li>
 *         Gathering patch names from the command line
 *         for sixteen channels
 *         (i.e. a single MIDI input stream)
 *     </li>
 *     <li>
 *         Starting up the Synth and waiting for
 *         [Enter] in the computer keyboard,
 *         at which point it shuts down.
 *     </li>
 * </ol>
 *
 */
public class App {

    public static boolean LOG_MAIN_OUT = false;


    static boolean hold = false; // kludge - suppress note offs for drone!
    public static boolean holdValue() { return hold; }

    static void showPrograms() {
        VoiceMaker.showPrograms();
        System.exit(0);
    }

    static void showProgram(String progName) {
        VoiceMaker.showProgram(progName);
        System.exit(0);
    }

    static void usage() {
        out.println(getResourceAsString("usage/App.txt"));
        System.exit(0);
    }

    public static void quitOnError() {
        out.println(
            "\n\n" +
            "For help use the -h option\n" +
            "For full instructions see README.md");

        System.exit(-1);
    }

    public static void main(String[] args) {

        // contains("") will match any
        String inDevStr = "", outDevStr = "";

        // Works for most voices. May need to be longer for some.
        int bufferSize=2048;

        //  Parse command line args
        //

        // one for each channel
        String[] progNames = new String[16];
        for (int i=0; i<16; ++i) progNames[i]="";

        List<String>argList = Arrays.asList(args);

        if (argList.contains("-all") || argList.contains("-all-patches")) {
            out.println("load all patches");
            VoiceMaker.setRecurseSubdirs(true);
        }

        List<String> looseVoices = new ArrayList<>();

        for (int i=0; i<args.length; ++i) {

            //  options with no following args
            switch(args[i]) {
                case "-list":
                case "-list-patches":
                case "-list-programs":
                    VoiceMaker.loadPrograms();
                    showPrograms(); // exits

                case "-help": case "--help": case "-h": case "?": case "-?":
                    usage(); //exits

                case "-log-main-out": LOG_MAIN_OUT=true;
                continue;

                case "-hold": hold=true; continue;
                case "-all": continue;
            }

            // options with following args - if we get here
            // and there are no args following, it's an error.

//            if (i+1 > args.length-1) {
//                err.println("Expected argument following "+args[i]);
//                usage();
//            }

            switch(args[i]) {
                case "-in": inDevStr = args[++i]; continue;
                case "-out": outDevStr = args[++i]; continue;

                case "-show":
                case "-show-patch":
                case "-show-program":
                    VoiceMaker.loadPrograms();
                    showProgram(args[++i]);

                case "-buffer-size":
                    bufferSize = Integer.parseInt(args[++i]);
                    continue;

            }
            if (args[i].startsWith("-ch")) {
                try {
                    progNames[ Integer.parseInt(args[i].substring(3)) - 1 ]
                        = args[++i];
                }
                catch (Exception ex) {
                    usage();
                }
            }
            else looseVoices.add(args[i]);
        }

        int lvp = 0, pnp = 0;
        while (lvp < looseVoices.size()) {
            while (!progNames[pnp].equals("") && pnp < progNames.length) ++pnp;
            if (pnp == progNames.length) {
                out.println("Too many loose program names. There are only 16 channels!");
                break;
            }
            progNames[pnp++] = looseVoices.get(lvp++);
        }

        //  Create, open, and start new Synth Session

        SynthSession session = new SynthSession(
            inDevStr, outDevStr, progNames, bufferSize);

        if (!session.open()) quitOnError();
        session.start();

        //  Quit when user hits Enter
        //
        BufferedReader in=new BufferedReader(
            new InputStreamReader(System.in));
        out.println("Press [Enter] to stop.");

        try { in.readLine(); }
        catch (Exception ignore) { }

        session.close();
        System.exit(0);
    }
}
