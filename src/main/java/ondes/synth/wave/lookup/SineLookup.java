package ondes.synth.wave.lookup;

import static java.lang.Math.*;

public class SineLookup {
    static int tableSize = 512_000;
    private final static double[] sineLookupTable = new double[tableSize];
    final static double TAO=2*PI;

    static {
        for (int i = 0; i < tableSize; ++i) {
            double theta = TAO * ((double)i)/tableSize;
            sineLookupTable[i] = sin(theta);
        }
    }

    public static double sineLookup(double theta) {
        double phase = (theta % TAO)/TAO;
        phase = phase - floor(phase); // for negative phase
        int idx = (int)(phase * tableSize);
        return sineLookupTable[idx];
    }
}
