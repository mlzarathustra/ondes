package ondes.file;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;
import static ondes.mlz.Util.getResourceAsString;

import ondes.midi.MlzMidi;
import ondes.synth.OndeSynth;
import ondes.synth.voice.VoiceMaker;

import java.util.Comparator;


public class PlayMidiFile {

    static boolean showRawEvents = false;
    static boolean showSortedEvents = true;

    File midiFile, waveFile;
    int sampleRate;

    PlayMidiFile(File midiFile, File waveFile, int sampleRate) {
        this.midiFile = midiFile;
        this.waveFile = waveFile;
        this.sampleRate = sampleRate;
    }


    /**
     *  # of seconds after the last MIDI event at which to
     *  fade, if it hasn't gone down to all 0's yet.
     */
    float fadeAfter = 10;

    /**
        How long to fade for, if needed.
     */
    float fadeLength = 4;

    OndeSynth synth;

        //  TODO - assign

    int getSampleRate() {
        //return synth.getSampleRate();
        return sampleRate;
    }


    static void usage() {
        out.println(getResourceAsString("usage/PlayMidiFile.txt"));
        System.exit(0);
    }

    static void showTrackInfo(Track[] tracks) {
        out.println("\n# of tracks: "+tracks.length);

        for (int i=0; i<tracks.length; ++i) {
            Track t = tracks[i];
            out.println("Track #"+(i+1)+" "+t.size()+" events.");

            if (showRawEvents) {
                out.println();
                for (int j=0; j < t.size(); ++j) {
                    MidiEvent evt = t.get(j);
                    out.println(evt.getTick() + "   " +
                        MlzMidi.toString(evt.getMessage()));
                }
            }
        }
    }

    static List<MidiEvent> getEvtList(Sequence seq) {
        List<MidiEvent> rs = new ArrayList<>();

        Track[] tracks = seq.getTracks();
        List.of(tracks).forEach( t -> {
           for (int j=0; j<t.size(); ++j) rs.add(t.get(j));
        });
        rs.sort( Comparator.comparing( MidiEvent::getTick ));

        return rs;
    }

    List<MidiEvent> getEventList(File midiFile) {
        Sequence seq;

        try {
            MidiFileFormat mff = MidiSystem.getMidiFileFormat(midiFile);
            //out.println("Midi file format: "+mff);
            seq = MidiSystem.getSequence(midiFile);
            //out.println("Midi sequence: "+seq);
        }
        catch (Exception ex) {
            out.println("Exception!\n"+ex);
            return null;
        }

        float divType = seq.getDivisionType();
        out.println("Division type: "+
            MlzMidi.divTypeString(divType));

        out.println("Sequence Length: ");
        out.println("  microseconds: "+seq.getMicrosecondLength());
        out.println("         ticks: "+seq.getTickLength());

        double ticksPerSecond = ( 1000000.0D * (double)seq.getTickLength()) /
            (double)seq.getMicrosecondLength();
        out.println("Ticks per second: "+ticksPerSecond);
        out.println("Samples per tick: "+ ( (float)getSampleRate()) / ticksPerSecond );


        Track[] tracks = seq.getTracks();
        showTrackInfo(tracks);
        List<MidiEvent> evtList = getEvtList(seq);


        if (showSortedEvents) {
            evtList.forEach( evt->
                out.println(evt.getTick() + "   " +
                    MlzMidi.toString(evt.getMessage()))
            );

        }
        return evtList;
    }

    public void run() {


        List<MidiEvent> evtList = getEventList(midiFile);

        //  TODO - once we get the samples,
        //   WavFileWriter.writeBuffer(samples, getSampleRate, waveFileName)

    }

    /*
        OndeSynth does this:

        for (;;) {
            resetWires();
            instant.next();
            monoMainMix.update();
            if (stop) return;
        }
    */

    public static void main(String[] args) throws IOException {
        //  Parse command line args
        //
        if (args.length < 2) usage();

        // one for each channel
        String[] progNames = new String[16];
        for (int i=0; i<16; ++i) progNames[i]="";

        List<String>argList = new ArrayList<>(Arrays.asList(args));

        String midiFileName = argList.remove(0);
        String waveFileName = argList.remove(0);

        File midiFile = new File(midiFileName);
        if (!midiFile.isFile()) {
            midiFile = new File(midiFileName+".mid");
            if (!midiFile.isFile()) {
                out.println("Can't open file "+midiFileName+"!");
                usage();
            }
        }
        out.println("MIDI file: "+midiFile);

        File waveFile = new File(waveFileName);
        if (waveFile.exists()) {
            out.println("WAVE file "+waveFile+" already exists!");
            usage();
        }

        if (argList.contains("-all") || argList.contains("-all-patches")) {
            out.println("load all patches");
            VoiceMaker.setRecurseSubdirs(true);
        }

        List<String> looseVoices = new ArrayList<>();

        int sampleRate = 44100;

        for (int i=0; i<args.length; ++i) {

            //  options with no following args
            switch (args[i]) {
                case "-sample-rate":
                    sampleRate = Integer.parseInt(args[++i]);
                    continue;

                    //  other args with parameters here

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

        PlayMidiFile midiFilePlayer = new PlayMidiFile(midiFile, waveFile, sampleRate);
        midiFilePlayer.run();


    }

}
