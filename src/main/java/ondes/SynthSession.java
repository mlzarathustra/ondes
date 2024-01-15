package ondes;

import ondes.midi.MlzMidi;
import ondes.synth.OndeSynth;
import ondes.synth.voice.VoiceMaker;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.*;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

/**
 * Both the main Ondes "App" and WaveEditor use this class
 *
 */
public class SynthSession {
    private final String inDevStr, outDevStr;
    private final String[] progNames;
    private final int bufferSize;
    private MidiDevice midiDev;
    private Mixer outDev;
    private OndeSynth synth;

    /**
     * Called from
     * <ul>
     *   <li>App (for main playback mode)</li>,
     *   <li>SynthConnection (for Wave Editor)</li>
     * </ul>

     * @param inDevStr - string identifying MIDI input device
     * @param outDevStr - string identifying Audio output device
     * @param progNames - the names of programs to load
     * @param bufferSize - size of audio buffer to use
     */
    public SynthSession(
                String inDevStr,
                String outDevStr,
                String[] progNames,
                int bufferSize) {

        this.inDevStr = inDevStr;
        this.outDevStr = outDevStr;
        this.progNames = progNames;
        this.bufferSize = bufferSize;
    }

    /**
     *  <ul>
     *  <li>
     *      Loads the programs into the static VoiceMaker arrays
     *  </li>
     *  <li>
     *  From the identifying labels passed in: inDevStr, outDevStr
     *  gets the MIDI input device (midiDev) and the
     *      Audio (output) Mixer (outDev)
     *  </li>
     *  </ul>

     *
     * @return - success
     */

    public boolean open() {
        VoiceMaker.loadPrograms();

        //  Connect MIDI input and Audio output
        //
        out.println("Input device : " + inDevStr);
        out.println("Output device: " + outDevStr);

        //  it displays the ones it has loaded later on.
        //out.println("Program Names: "+Arrays.toString(progNames));

        midiDev = getMidiDev(inDevStr);
        if (midiDev == null) {
            out.println("Could not open MIDI input device " + inDevStr);
            out.println("See READ.md for more information.");
            return false;
        }
        out.println("Midi Input device   : " + midiDev.getDeviceInfo());

        outDev = getMixer(outDevStr);
        if (outDev == null) {
            out.println("Could not open audio mixer device for output: " + outDevStr);
            out.println("See READ.md for more information.");
            out.println("For audio output, you need one with a SOURCE line, " +
                "illogical as that seems.");
           return false;
        }
        out.println("Mixer (audio output): " + outDev.getMixerInfo());
        return true;
    }

    public void start() {
        synth = new OndeSynth(
            //  TODO DBG0115
            48000,      // sample rate
            midiDev,    // input device
            outDev,      // output device
            progNames,  // patch names
            bufferSize  // audio buffer size
        );

        synth.start();
    }

    public void close() {
        if (synth != null) synth.logFlush();

        if (midiDev != null) midiDev.close();
        if (outDev != null) outDev.close();
    }

    public OndeSynth getSynth() { return synth; }

    // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///
             // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///
       // /// // ///   // /// // ///   // /// // ///   // /// // ///   // /// // ///



    static MidiDevice getMidiDev(String inDevStr) {
        MidiDevice.Info info = MlzMidi.getTransmitter(inDevStr);
        if (info == null) {
            out.println(
                "could not find midi transmitting device to match "
                    +inDevStr);
            return null;
        }
        try {
            return MidiSystem.getMidiDevice(info);
        }
        catch (Exception ex) {
            out.println("attempting to open midi device "+inDevStr);
            out.println(ex);
            return null;
        }
    }

    public static Mixer getMixer(String outDevStr) {
        out.println("SynthSession.getMixer("+outDevStr+")");
        Mixer.Info[] info= AudioSystem.getMixerInfo();
        List<Mixer.Info> list = Arrays.stream(info)
            .filter(i -> {
                String id=i.toString().toLowerCase();
                if (!id.contains(outDevStr))
                    return false;
                Mixer mixer=AudioSystem.getMixer(i);
                return mixer.getSourceLineInfo().length > 0;
            })
            .collect(toList());

        //out.println(list);
        if (list.isEmpty()) return null;
        out.println( list.size() +
            ( list.size()==1 ? " item matches ": " items match ") + outDevStr);
        if (list.size() > 1) out.println("  ...Using the first one.");

        return AudioSystem.getMixer(list.get(0));
    }


}
