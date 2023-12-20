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

import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

public class SynthSession {
    private final String inDevStr, outDevStr;
    private final String[] progNames;
    private final int bufferSize;
    private MidiDevice midiDev;
    private Mixer mixer;
    private OndeSynth synth;

    public SynthSession(String inDevStr,
                 String outDevStr,
                 String[] progNames,
                 int bufferSize) {
        this.inDevStr = inDevStr;
        this.outDevStr = outDevStr;
        this.progNames = progNames;
        this.bufferSize = bufferSize;
    }

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

        mixer = getMixer(outDevStr);
        if (mixer == null) {
            out.println("Could not open audio mixer device for output: " + inDevStr);
            out.println("See READ.md for more information.");
            out.println("For audio output, you need one with a SOURCE line, " +
                "illogical as that seems.");
           return false;
        }
        out.println("Mixer (audio output): " + mixer.getMixerInfo());
        return true;
    }

    public void start() {
        synth = new OndeSynth(
            44100,      // sample rate
            midiDev,    // input device
            mixer,      // output device
            progNames,  // patch names
            bufferSize  // audio buffer size
        );

        synth.start();
    }

    public void close() {
        if (synth != null) synth.logFlush();

        if (midiDev != null) midiDev.close();
        if (mixer != null) mixer.close();
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
        out.println(list.size() + " items match "+outDevStr);

        // if more than one item matches, make sure we can
        // open the source data line.

        Mixer rs = null;
        for (Mixer.Info srcInfo : list) {
            try {
                rs = AudioSystem.getMixer(srcInfo);
                Line.Info[] lineInfo = rs.getSourceLineInfo();
                //out.println("lineInfo is "+lineInfo.length+" items.");
                //SourceDataLine sdl = (SourceDataLine)rs.getLine(lineInfo[0]);
                SourceDataLine sdl = (SourceDataLine)rs.getLine(lineInfo[lineInfo.length - 1]);
                sdl.open();
                sdl.start();
                break; // if no exception
            }
            catch (Exception ignore) { }
        }

        return rs;
    }



}
