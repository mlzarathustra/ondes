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

    static void t4() {
        MidiInfo.main(new String[]{});
    }
    static void t5() {
        VoiceMaker.main(new String[]{});
    }

    static void t6() {
        int[] a= { 7,8,9 };


        List<Integer> al = Arrays.stream(a).boxed().collect(toList());



    }

    //////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        //for (int i=0; i<30; ++i) out.println(sineLookupTable[i]);
    }

}



















