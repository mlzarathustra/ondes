package ondes.synth.wave.lookup;


public class FloatWaveLookup {
    static final int TABLE_SIZE = 512_000;
    private final float[] lookupTable = new float[TABLE_SIZE];

    FloatUnaryOperator fn;

    //  todo - normalize level

    public FloatWaveLookup(FloatUnaryOperator fnInp) {
        fn = fnInp;
        refresh();
    }

    public void refresh() {
        for (int i = 0; i < TABLE_SIZE; ++i) {
            float phase = ((float)i)/ TABLE_SIZE;
            lookupTable[i] = fn.applyAsFloat(phase);
        }
    }

    public double valueAt(float phase) {
        int idx = (int)(phase * TABLE_SIZE) % TABLE_SIZE;
        return lookupTable[idx];
    }
}
