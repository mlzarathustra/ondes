package ondes.synth.filter;

import ondes.synth.wave.WaveGen;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.Junction;

import java.util.HashMap;

import static ondes.mlz.Util.keyStrip;
import static java.lang.System.err;

public class FilterMaker {
    static HashMap<String, Class<? extends Filter>> filterClasses = new HashMap<>();

    private static void register(String key, Class<? extends Filter> f) {
        filterClasses.put(keyStrip(key), f);
    }

    /**
     * <p>
     *     Factory" method to create Filters
     * </p>
     * <p>
     *     Does NOT configure the filter, as we must wait
     *     until all of the components are created, so that we
     *     can connect to any one of them.
     * </p>
     *
     * @param shape - the key for finding this wave generator type
     * @return a filter as named by key. If no filter can be found,
     * it returns a junction (hence the MonoComponent return type)
     * so at least you'll hear something.
     */
    public static MonoComponent getFilter(String shape) {
        if (shape == null) shape = "sinc";
        Class<? extends Filter> wgClass = filterClasses.get(keyStrip(shape));
        try {
            return wgClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception ex) {
            err.println("Can't find the filter class specified: "+shape);
            return new Junction();
        }
    }

    static {
        register("sinc", SincFilter.class);
        register("sweep-sinc", SweepingSincFilter.class);
    }

}
