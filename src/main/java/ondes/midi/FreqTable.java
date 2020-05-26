package ondes.midi;

import java.util.Arrays;

import static java.lang.Math.*;
import static java.lang.System.out;
import static ondes.midi.MlzMidi.midiNumToStr;

/**

 * <p>
 *    Generate a table from MIDI note number to frequency
 *    rather than trying to serialize it as data, it's easy
 *    enough to just regenerate each time.
 * </p>
 * <p>
 *    The math isn't too hairy, otherwise it would be the
 *    Furry Freq Brothers. :^)
 * </p>
 *
 */
public class FreqTable {

    /**
     * freqs[midi-note-num] = the frequency of the note.
     */
    private static final double[] freqs = new double[128];

    public static double getFreq(int midiNum) {
        if (midiNum<0 || midiNum > 127) return -1;
        return freqs[midiNum];
    }

    /**
     * You may choose a different base if you prefer!
     */
    public static final double A3=440;
    public static final int A3MidiNum = 69;

    /**
     * The increment of a minor second.
     * if (f = freq(C)) then f * m2 = freq(C#)
     */
    public static final double m2=pow( 2, 1.0/12 );

    static {
        //  To avoid a minuscule loss of precision,
        //  multiply or divide the the base A frequency
        //  to get the exact octave value, then step up
        //  from each for that octave.
        //
        //  We hope the listener will appreciate this
        //  attention to accuracy.
        //
        double a=A3;
        for (int i = A3MidiNum; i<128; i += 12) {
            double step = a;
            for (int j=0; j<12 && i+j<128; ++j) {
                freqs[i+j] = step;
                step *= m2;
            }
            a *= 2;
        }

        a=A3;
        for (int i = A3MidiNum; i>0; i-= 12) {
            double step = a;
            for (int j=0; j<12 && i-j>=0; ++j) {
                freqs[i-j] = step;
                step /= m2;
            }
            a /= 2;
        }
    }

    public static void main(String[] args) {
        out.println();
        out.println("The first column is the note A in all octaves.");
        out.println("The bottom note is "+midiNumToStr(0));
        out.println("The top note is "+midiNumToStr(127));
        out.println("");

        for (int i=0; i<3; ++i) out.print(String.format("%12s"," "));
        for (int i=0; i<128; ++i) {
            if ((i-A3MidiNum)%12 == 0) out.println();
            out.print(
                String.format("%12s",
                    String.format("%5.5f ",getFreq(i))));
        }
        out.println();


    }
}
