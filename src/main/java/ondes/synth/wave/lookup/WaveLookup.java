package ondes.synth.wave.lookup;

import java.util.function.DoubleUnaryOperator;

public class WaveLookup {
    static int TABLE_SIZE = 512_000;
    private final double[] lookupTable = new double[TABLE_SIZE];

    //  todo - normalize level

    public WaveLookup(DoubleUnaryOperator fn) {
        for (int i = 0; i < TABLE_SIZE; ++i) {
            double phase = ((double)i)/TABLE_SIZE;
            lookupTable[i] = fn.applyAsDouble(phase);
        }
    }

    public double valueAt(double phase) {
        int idx = (int)(phase * TABLE_SIZE) % TABLE_SIZE;
        return lookupTable[idx];
    }
}
