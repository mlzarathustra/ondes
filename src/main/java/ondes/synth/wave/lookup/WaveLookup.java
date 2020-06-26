package ondes.synth.wave.lookup;

import java.util.function.DoubleUnaryOperator;

public class WaveLookup {
    static int tableSize = 512_000;
    private final double[] lookupTable = new double[tableSize];

    public WaveLookup(DoubleUnaryOperator fn) {
        for (int i = 0; i < tableSize; ++i) {
            double phase = ((double)i)/tableSize;
            lookupTable[i] = fn.applyAsDouble(phase);
        }
    }

    public double valueAt(double phase) {
        int idx = (int)(phase * tableSize);
        return lookupTable[idx];
    }
}
