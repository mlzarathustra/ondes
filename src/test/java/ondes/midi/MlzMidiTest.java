package ondes.midi;

import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static ondes.midi.MlzMidi.midiNumListToStr;
import static ondes.midi.MlzMidi.toMidiNumList;
import static org.testng.Assert.*;

public class MlzMidiTest {

    @Test
    public void testMidiNumToNote() {
        List<Integer> notes =
            IntStream.range(0,128)
                .boxed()
                .collect(toList());

        assertTrue(midiNumListToStr(notes)
            .startsWith("C-2 C#-2 D-2 D#-2 E-2 F-2 F#-2 G-2 G#-2 A-2 A#-2 B-2 C-1"),
            "conversion to sharps failed");

        assertTrue(midiNumListToStr(notes, true)
                .startsWith("C-2 Db-2 D-2 Eb-2 E-2 F-2 Gb-2 G-2 Ab-2 A-2 Bb-2 B-2 C-1"),
            "conversion to flats failed."
        );

        String noteStr = midiNumListToStr(notes);
        List<Integer> notesBack = toMidiNumList(noteStr);

        assertEquals(notes.size(), notesBack.size(),
            "Note lists are of different size.");

        boolean clash=false;
        for (int i=0; i<notes.size(); ++i) {
            if (notes.get(i) != notesBack.get(i)) {
                out.println("Element " + i + " doesn't match: "+
                    "orig="+notes.get(i)+"; returned="+notesBack.get(i));
                clash = true;
            }
        }

        if (!clash) {
            // out.println("Convert to str and back: all notes match.");
        }
        assertFalse(clash,"Convert to str and back failed!");


    }

}