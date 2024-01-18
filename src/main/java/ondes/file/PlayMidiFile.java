package ondes.file;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;
import static ondes.mlz.Util.getResourceAsString;

import ondes.midi.MlzMidi;
import ondes.synth.OndeSynth;
import ondes.synth.mix.WaveMonoMainMix;
import ondes.synth.voice.VoiceMaker;

import java.util.Comparator;


public class PlayMidiFile {

    static boolean showRawEvents = false;
    static boolean showSortedEvents = true;

    static boolean overwriteOutFile = false;

    File midiFile, waveFile;
    int sampleRate;
    String[] progNames;

    // to construct WaveMonoMainMix
    float samplesPerTick, ticksPerSample;

    PlayMidiFile(
        File midiFile, File waveFile, String[] progNames,
        int sampleRate
    ) {

        this.midiFile = midiFile;
        this.waveFile = waveFile;
        this.progNames = progNames;
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
        samplesPerTick = ((float)getSampleRate()) / (float)ticksPerSecond;
        ticksPerSample = ((float)ticksPerSecond / (float)getSampleRate());
        out.println("Samples per tick: "+ samplesPerTick);
        out.println("Ticks per sample: "+ ticksPerSample);

        Track[] tracks = seq.getTracks();
        showTrackInfo(tracks);
        List<MidiEvent>evtList = getEvtList(seq);

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
        WaveMonoMainMix mainMix =
            new WaveMonoMainMix(sampleRate, ticksPerSample, evtList,
                fadeAfter, fadeLength);

        OndeSynth synth = new OndeSynth(mainMix, progNames);

        mainMix.setSynth(synth);// It may not need this

        synth.start();
        try {
            synth.join();
        }
        catch (Exception ignore) { }
        out.println(); // skip status line

        List<Integer> sampleList = mainMix.getSamples();

        out.println(sampleList.size() + " samples");
        out.println("Synth halted.");

        int[] samples = new int[sampleList.size()];
        for (int i=0; i<samples.length; ++i) samples[i] = sampleList.get(i);

        out.println("Writing samples.");

        try {
            WavFileWriter.writeBuffer(samples, getSampleRate(), waveFile);
        }
        catch (IOException ex) {
            out.println("IO Exception writing "+waveFile+"\n"+ex);
        }
        out.println("Done.");

        // the grim reaper thread is still running.
        // we could stop it gracefully, or we could do this:
        System.exit(0);
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

        //
        List<String> looseVoices = new ArrayList<>();
        int sampleRate = 44100;

        for (int i=0; i<argList.size(); ++i) {

            switch(argList.get(i)) {
                case "-overwrite":
                    out.println("overwrite set to true.");
                    overwriteOutFile = true;
                    continue;
            }

            switch (argList.get(i)) {
                case "-sample-rate":
                    sampleRate = Integer.parseInt(argList.get(++i));
                    continue;

                    //  other args with parameters here
            }

            if (argList.get(i).startsWith("-ch")) {
                try {
                    progNames[ Integer.parseInt(argList.get(i).substring(3)) - 1 ]
                        = argList.get(++i);
                }
                catch (Exception ex) {
                    usage();
                }
            }
            else looseVoices.add(argList.get(i));
        }




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
            if (overwriteOutFile) {
                out.println("-overwrite was specified - overwriting "+waveFile);
            }
            else {
                out.println("WAVE file " + waveFile + " already exists!");
                out.println("   specify -overwrite to overwrite.");
                usage();
            }
        }

        if (argList.contains("-all") || argList.contains("-all-patches")) {
            out.println("load all patches");
            VoiceMaker.setRecurseSubdirs(true);
        }

        int lvp = 0, pnp = 0;
        while (lvp < looseVoices.size()) {
            while (pnp < progNames.length && !progNames[pnp].isEmpty()) ++pnp;
            if (pnp == progNames.length) {
                out.println("Too many loose program names. There are only 16 channels!");
                break;
            }
            progNames[pnp++] = looseVoices.get(lvp++);
        }

        out.println("progNames : "+Arrays.toString(progNames));

        VoiceMaker.loadPrograms();

        PlayMidiFile midiFilePlayer =
            new PlayMidiFile(midiFile, waveFile, progNames, sampleRate);

        midiFilePlayer.run();
    }

}
