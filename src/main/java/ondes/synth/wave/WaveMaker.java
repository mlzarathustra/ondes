package ondes.synth.wave;

import ondes.synth.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static ondes.mlz.Util.keyStrip;

/**
 *  WaveMaker returns a WaveGen instance, given the key
 *  (given in the "shape" value of the config).
 *  It then passes along the configuration parameters
 *  and the Map of other components (to connect to)
 *  to the new instance.
 *  <br/><br/>
 *
 *  It does not set the frequency.
 *
 */
public class WaveMaker {
    //  Manage WaveGen subclasses (the concrete wave generators)
    static HashMap<String,Class<? extends WaveGen>> concreteClasses=new HashMap<>();
    private static void register(String key, Class<? extends WaveGen> c) {
        concreteClasses.put(keyStrip(key), c);
    }

    public static List<String> getKeys() {
        return concreteClasses.keySet()
            .stream()
            .sorted()
            .collect(toList());
    }

    /**
     * Wave Generator "factory" method
     * <br/><br/>
     *
     * Does NOT configure the wave generator, as we must wait
     * until all of the components are created, so that we can
     * connect to any one of them.
     *
     * @param shape - the key for finding this wave generator type
     * @return a wave generator as named by key
     */
    public static WaveGen getWaveGen(String shape) {
        Class<? extends WaveGen> wgClass = concreteClasses.get(keyStrip(shape));
        try {
            return wgClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception ex) {
            return new SineWaveGen();
        }
    }

    static {        //
        register("sine", SineWaveGen.class);
        register("square", SquareWaveGen.class);
        register("saw", SawWaveGen.class);
        register("pwm", PWMWaveGen.class);
        register("mellow", CompositeWaveGen.class);
        register("bell", CompositeWaveGen.class);
        register("organ", CompositeWaveGen.class);
        register("sample", SampleWaveGen.class);
    }

}
