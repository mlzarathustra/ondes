package ondes;

import ondes.midi.MidiInfo;
import ondes.synth.voice.VoiceMaker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static ondes.midi.MlzMidi.*;

public class Test {

    static void t0() {
        out.println(getReceiver("828"));

    }
    static void t1() {
        out.println(toMidiNumList("c#2 d#2 e2"));
        out.println(midiNumListToStr((List<Integer>) Arrays.asList(49,51,52)));
    }

    //  TODO - make this a test assertion
    static void t2() {
        List<Integer> notes =
            IntStream.range(0,128)
                .boxed()
                .collect(toList());

        out.println(midiNumListToStr(notes));

        out.println(midiNumListToStr(notes, true));

        String noteStr = midiNumListToStr(notes);
        List<Integer> notesBack = toMidiNumList(noteStr);
        if (notes.size() != notesBack.size())
            out.println("lists are of different size.");

        boolean clash=false;
        for (int i=0; i<notes.size(); ++i) {
            if (notes.get(i) != notesBack.get(i)) {
                out.println("Element " + i + " doesn't match: "+
                    "orig="+notes.get(i)+"; returned="+notesBack.get(i));
                clash = true;
            }
        }
        if (!clash) { out.println("Convert to str and back: all notes match."); }
    }

    static void t4() {
        MidiInfo.main(new String[]{});
    }
    static void t5() {
        VoiceMaker.main(new String[]{});
    }


    public static void main(String[] args) {
        t5();
    }


}
