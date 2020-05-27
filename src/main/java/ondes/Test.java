package ondes;

import ondes.midi.MidiInfo;
import ondes.synth.voice.VoiceMaker;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static ondes.midi.MlzMidi.*;

import static java.lang.Math.*;

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

    static void f(double x) { // make sure it's using the value
    }

    // stress test sin function vs. lookup table
    // the lookup takes a little more than half the time.
    //
    static void t6(DoubleUnaryOperator fn) {
        long start = System.nanoTime();
        for (int rpt = 0; rpt < 1e5; ++rpt) {
            for (double theta = 0; theta < 2 * PI; theta += 0.001) {
                f(fn.applyAsDouble(theta));
            }
        }

        long end = System.nanoTime();
        out.println("Elapsed: "+((float)(end-start))/1e9 + " seconds");
    }

    // compare results: sin function with lookup table
    static void t7(DoubleUnaryOperator fn1, DoubleUnaryOperator fn2) {
        long start = System.nanoTime();
        for (double theta = 0; theta < 2 * PI; theta += 0.001) {
            double diff = fn1.applyAsDouble(theta) - fn2.applyAsDouble(theta);
            out.println(" diff: "+diff);
        }

        long end = System.nanoTime();
        out.println("Elapsed: "+((float)(end-start))/1e9 + " seconds");
    }


    //////////////////////////////////////////////////////////////////////////////////////
    //  below: should be a test case
    //  see t8() comment

    static int tableSize = 512_000;
    static double[] sineLookupTable = new double[tableSize];
    static double TAO=2*PI;

    static {
        for (int i = 0; i < tableSize; ++i) {
            double theta = TAO * ((double)i)/tableSize;
            sineLookupTable[i] = sin(theta);
        }
    }

    static double sineLookup(double theta) {
        double phase = (theta % TAO)/TAO;
        int idx = (int)(phase * tableSize);
        return sineLookupTable[idx];
    }

    //  Linear antialiasing doesn't seem to significantly
    //  improve accuracy.
    //
    static double sineLookupWithAlias(double theta) {
        double phase = (theta % TAO)/TAO;
        double idx = (int) (phase * tableSize);
        double left = idx % 1.0;

        double here = sineLookupTable[(int)idx];
        if (idx > sineLookupTable.length - 1) return here;
        double next = sineLookupTable[1+(int)idx];
        return here + (left * (next - here));
    }

    //  This and all its dependencies should be a test case.
    //  assert that the difference is never > 1e5 or so.
    static void t8() {
        out.println("using function");
        t6(Math::sin);
        out.println("using lookup");
        t6(Test::sineLookup);

        t7(Math::sin, Test::sineLookup);
    }

    //////////////////////////////////////////////////////////////////////////////////////



    public static void main(String[] args) {


        //for (int i=0; i<30; ++i) out.println(sineLookupTable[i]);
    }


}



















