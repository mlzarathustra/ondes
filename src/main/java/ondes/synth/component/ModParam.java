package ondes.synth.component;

import ondes.mlz.FloatConsumer;

import java.util.Map;
import java.util.function.*;

import static ondes.synth.component.ConfigHelper.getFloat;
import static ondes.synth.component.ConfigHelper.getInAmpPair;

@SuppressWarnings("rawtypes")
public class ModParam {

    float base, range, current;
    int amp;
    boolean mod;
    FloatConsumer post;

    public ModParam(Map config,
             String label,
             String measure,
             float defaultVal
             ) {

        this(config,label,measure,defaultVal, null);
    }

    public ModParam(Map config,
             String label,
             String measure,
             float defaultVal,
             FloatConsumer post) {

        Float fltInp = getFloat(config.get(label),
            label + " must be a decimal number");
        if (fltInp != null) base = fltInp;
        else base = defaultVal;

        Object[] modInp = getInAmpPair(config, "input-"+label, measure);
        if (modInp != null) {
            amp = (int) modInp[0];
            range = (float) modInp[1];
            mod = true;
        }

        this.post = post;
    }

    // inp was namedInputSum("time")
    boolean mod(int inp) {
        if (!mod) return false;
        float delta = range * inp / amp;
        if (delta + base == current) return false;
        current = delta + base;

        if (post != null) post.accept(current);
        return true;

    }

    float currentValue() { return current; }

}
