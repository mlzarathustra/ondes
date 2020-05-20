package ondes.midi;

import javax.sound.midi.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


public class MlzMidi {

    // looks for the first midi device with name containing id
    // that has a receiver. The receiver is for playback
    public static MidiDevice.Info getReceiver(String id) {
        //println 'getReceiver'
        MidiDevice.Info[] infoList=MidiSystem.getMidiDeviceInfo();

        return Arrays.stream(infoList)
            .filter (
                (MidiDevice.Info it)-> {
                    if (!it.getName().toLowerCase().contains(id.toLowerCase())) return false;
                    //println "$it.name  - $id"

                    MidiDevice midiDev=null;
                    try {
                        midiDev = MidiSystem.getMidiDevice(it);
                        midiDev.open();
                        Receiver recv = midiDev.getReceiver();
                    } catch (Exception ex) {
                        midiDev.close();
                        //println 'no receiver'
                        return false;
                    }
                    midiDev.close();
                    return true;
                })
            .findFirst().orElse(null);
    }

    //  The transmitter is for recording.
    public static MidiDevice.Info getTransmitter(String id) {
        //println 'getReceiver'
        MidiDevice.Info[] infoList=MidiSystem.getMidiDeviceInfo();

        return Arrays.stream(infoList)
            .filter (
                (MidiDevice.Info it)-> {
                    if (!it.getName().toLowerCase().contains(id.toLowerCase())) return false;
                    //println "$it.name  - $id"

                    MidiDevice midiDev=null;
                    try {
                        midiDev = MidiSystem.getMidiDevice(it);
                        midiDev.open();
                        Transmitter trans = midiDev.getTransmitter();
                        //Receiver recv = midiDev.getReceiver();
                    } catch (Exception ex) {
                        midiDev.close();
                        //println 'no receiver'
                        return false;
                    }
                    midiDev.close();
                    return true;
                })
            .findFirst().orElse(null);
    }

    public static int toMidiNum(int note) { return note; } // for overloading

    //  TODO -- handle double flat (bb) 
    //
    public static int toMidiNum(String note) {
        int[] offsets={9,11,0,2,4,5,7}; // a b c d e f g
        note=note.trim();
        Matcher m = Pattern
            .compile("([a-gA-G])([#bx]?)(-?[0-8])?")
            .matcher(note);

        if (m.find()) {
            String noteName=m.group(1);
            String accidental=m.group(2);
            String octave=m.group(3);

            int rs = offsets[ noteName.toLowerCase().charAt(0) - 'a' ]+
                (accidental.equals("#")?1:0) +
                (accidental.equals("b")?-1:0) +
                (accidental.equals("x")?2:0) +
                (octave == null ? 0 : Integer.parseInt(octave) + 2) * 12;

            return rs<128? rs :-1;
        }
        else return -1;
    }

    // notes may be separated by spaces or commas
    // use lower-case b for flat 
    // samples: g#3 c0 Bb-1
    public static List<Integer> toMidiNumList(String noteStr) {
        return Arrays.stream(noteStr.split("[ ,]+"))
            .map(MlzMidi::toMidiNum)
            .collect(Collectors.toList());
    }

    public static String[][] noteNames= {
        {"C", "C#","D","D#","E","F","F#","G","G#","A","A#","B"},
        {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"}
    };

    public static String midiNumToStr(int n) {
        return midiNumToStr(n,false);
    }
    public static String midiNumToStr(int n, boolean isFlat) {
        int octave = n/12;
        return  noteNames[isFlat?1:0][n%12]+(octave-2);
    }

    public static String midiNumListToStr(List<Integer> n) {
        return midiNumListToStr(n,false);
    }
    public static String midiNumListToStr(List<Integer> n, boolean isFlat) {
        return n.stream().map( it -> midiNumToStr(it,isFlat))
            .collect(joining(" "));
    }

    // TODO - could be a lot more thorough!
    //
    public static String toString(MidiMessage msg) {
        String status="unknown";
        int s=msg.getStatus()>>4;
        switch (s) {
            case 0x8: status = "Note OFF"; break;
            case 0x9: status = "Note ON"; break;
            case 0xa: status = "Aftertouch"; break;
            case 0xb: status = "Controller"; break;
            case 0xc: status = "Program Change"; break;
            case 0xd: status = "Channel Pressure"; break;
            case 0xe: status = "Pitch Bend"; break;
            case 0xf: status = "System"; break;
        }
        StringBuilder sb=new StringBuilder(status);
        sb.append(" - ");
        if (s<0xa) {
            sb.append(midiNumToStr(msg.getMessage()[1]));
            sb.append(" vel="+msg.getMessage()[2]);
        }
        else {
            for (int i = 1; i < msg.getLength(); ++i) {
                sb.append(msg.getMessage()[i]);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    


}



