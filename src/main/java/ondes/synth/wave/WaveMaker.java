package ondes.synth.wave;

import ondes.synth.noise.NoiseWaveGen;
import ondes.synth.noise.PinkNoiseGen;

import java.util.*;

import static java.lang.System.err;
import static java.util.stream.Collectors.toList;
import static ondes.mlz.Util.keyStrip;

/**
 * <p>
 *     WaveMaker returns a WaveGen instance, given the key
 *     (given in the "shape" value of the config).
 *     It then passes along the configuration parameters
 *     and the Map of other components (to connect to)
 *     to the new instance.
 * </p>
 * <p>
 *     It does not set the frequency.
 * </p>
 *
 */
public class WaveMaker {
    static HashMap<String,Class<? extends WaveGen>> waveGenClasses =new HashMap<>();
    private static void register(String key, Class<? extends WaveGen> c) {
        waveGenClasses.put(keyStrip(key), c);
    }

    public static List<String> getKeys() {
        return waveGenClasses.keySet()
            .stream()
            .sorted()
            .collect(toList());
    }

    /**
     * <p>
     *     Wave Generator "factory" method
     * </p>
     * <p>
     *     Does NOT configure the wave generator, as we must wait
     *     until all of the components are created, so that we
     *     can connect to any one of them.
     * </p>
     *
     * @param shape - the key for finding this wave generator type
     * @return a wave generator as named by key
     */
    public static WaveGen getWaveGen(String shape) {
        if (shape == null) {
            err.println("WaveMaker: shape is null! Falling back to saw.");
            shape = "saw";
        }
        Class<? extends WaveGen> wgClass = waveGenClasses.get(keyStrip(shape));
        try {
            return wgClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception ex) {
            return new SquareWaveGen();
        }
    }

    static {
        register("square", SquareWaveGen.class);
        register("pwm", PwmWaveGen.class);

        register("sine", SineWaveGen.class);
        register("harmonic", HarmonicWaveGen.class);
        register("anharmonic", AnharmonicWaveGen.class);

        register("saw", SawWaveGen.class);
        register("ramp-up", RampUpWaveGen.class);
        register("ramp-down", RampDownWaveGen.class);

        register("noise", NoiseWaveGen.class);
        register("pink", PinkNoiseGen.class);
    }

}
