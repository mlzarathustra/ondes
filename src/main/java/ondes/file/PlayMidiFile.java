package ondes.file;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import ondes.midi.MlzMidi;
import ondes.synth.OndeSynth;

import java.util.Comparator;


public class PlayMidiFile {

    static boolean showRawEvents = false;
    static boolean showSortedEvents = true;

    static OndeSynth synth;

        //  TODO - assign

    static int getSampleRate() {
        //return synth.getSampleRate();
        return 44100;
    }

    static void usage() {
        out.println("Syntax: java java.ondes.file.MixMidiFile <filename>.mid");
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

    public static void main(String[] args) throws IOException {
        if (args.length == 0) usage();

        File midiFile = new File(args[0]);
        if (!midiFile.isFile()) usage();

        out.println("MIDI file: "+midiFile);
        Sequence seq;

        try {
            MidiFileFormat mff = MidiSystem.getMidiFileFormat(midiFile);
            //out.println("Midi file format: "+mff);
            seq = MidiSystem.getSequence(midiFile);
            //out.println("Midi sequence: "+seq);
        }
        catch (Exception ex) {
            out.println("Exception!\n"+ex);
            return;
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
    }

}
