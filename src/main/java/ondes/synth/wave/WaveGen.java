package ondes.synth.wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static ondes.mlz.Util.keyStrip;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 *
 *
 */
public abstract class WaveGen {
    static int DEFAULT_SAMPLE_RATE=44100; // per second
    static double DEFAULT_FREQ=440;       // hz

    long[] samples;
    double freq = 440;
    int amp = 10_000, sampleRate=44100;

    private String waveKey;

    void setBufSize(int bufSize) { // typically: 4104
        if (samples == null || samples.length != bufSize) {
            samples=new long[bufSize];
        }
    }
    void setSampleRate(int sr) { sampleRate = sr; }

    void setFreq(double freq) { this.freq = freq; }

    /** The most important override - when called by ToneGen, this method is responsible
     * for supplying the wave samples required by the Android's Audio Track
     *
     * @return - the next block of samples
     */
    abstract long[] nextBuf();

    /**
     * reset the note to zero
     */
    abstract void reset();

    //  Manage subclasses (the concrete wave generators)
    static HashMap<String,Class> concreteClasses=new HashMap<>();
    private static void register(String key, Class c) {
        concreteClasses.put(keyStrip(key), c);
    }

    public static ArrayList<String> getKeys() {  // should be CharSequence
        ArrayList<String> list=new ArrayList<>(concreteClasses.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * Wave Generator "factory" method
     *
     * @param key - text mnemonic for wave form
     * @return a wave generator as named by key
     */
    static WaveGen getWaveGen(String key) {
        Class wgClass = concreteClasses.get(keyStrip(key));
        try {
            WaveGen wg = (WaveGen) wgClass.newInstance();
            wg.setWaveKey(key);
            return wg;
        }
        catch (Exception ex) {
            return new SineWaveGen();
        }
    }
    protected void setWaveKey(String key) { waveKey =keyStrip(key); }
    String getWaveKey() { return waveKey; }

    static {
        //
        WaveGen.register("sine", SineWaveGen.class);
        WaveGen.register("square", SquareWaveGen.class);
        WaveGen.register("saw", SawWaveGen.class);
        WaveGen.register("pwm", PWMWaveGen.class);
        WaveGen.register("mellow", CompositeWaveGen.class);
        WaveGen.register("bell", CompositeWaveGen.class);
        WaveGen.register("organ", CompositeWaveGen.class);
        WaveGen.register("sample", SampleWaveGen.class);
    }



}
