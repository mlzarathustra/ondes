package ondes.mlz;


import java.util.stream.IntStream;

import static java.lang.Math.log;
import static ondes.midi.FreqTable.getFreq;
import static ondes.midi.MlzMidi.toMidiNum;

import static java.lang.System.out;


/**
 *
 * experiment with the math of amplitude scaling by pitch
 *
 */
public class PitchScaling {

    static double c3 = getFreq(toMidiNum("c3"));

    /**
     * <pre>
     *   log(freq / c3) / log(2) -
     *          is the number of octaves from middle c. will be
     *              negative when freq < c3
     *              0 when freq = c3
     *              positive when freq > c3
     *
     *    div - the divisor. as div increases, the amount of scaling
     *              decreases.
     *
     *    we add 1 so that at c3 the scaling will always be 1.
     *
     *    div = 10 results in a possible solution. It increases by .1
     *    as the octave decreases (c2=1.1 c1=1.2 &c.)
     * </pre>
     */
    public static double getScaling(double div, double freq) {
        if (div == 0) return 1;
        double val = log(freq / c3) / log(2);
        return (-val / div) + 1;
    }

    static void showScaling(double div) {
        out.println("Pitch scaling with coefficient "+div);
        IntStream.range(-2, 9).forEach(n -> {
            double freq = getFreq(toMidiNum("c" + n));
            out.println("C" + n + ": " +
                getScaling(div,freq));
        });
        out.println();
    }


    public static void main(String[] args) {
        showScaling(10);
        showScaling(5);
    }
}



