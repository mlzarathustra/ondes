package ondes.synth.component;

import ondes.function.FloatConsumer;

import java.util.Map;
import java.util.function.Function;
import static java.lang.System.out;

import static ondes.synth.component.ConfigHelper.getFloat;
import static ondes.synth.component.ConfigHelper.getInAmpPair;

@SuppressWarnings("rawtypes")
public class ModParam {

    private float base;
    private float range;
    private float current;
    private float scale = 1;
    int amp;
    boolean mod;
    FloatConsumer post;
    String label;
    Function<String,Integer> getInput;

    public ModParam(Map config,
                    String label,
                    String measure,
                    float defaultVal,
                    Function<String,Integer> getInput
    ) {

        this(config,label,measure,defaultVal, getInput, null);
    }

    public ModParam(Map config,
             String label,
             String measure,
             float defaultVal,
             Function<String,Integer> getInput,
             FloatConsumer post) {

        Float fltInp = getFloat(config.get(label),
            label + " must be a decimal number");
        if (fltInp != null) base = fltInp;
        else base = defaultVal;

        current = base * scale;

        Object[] modInp = getInAmpPair(config, "input-"+label, measure);
        if (modInp != null) {
            amp = (int) modInp[0];
            range = (float) modInp[1];
            mod = true;
        }

        this.label = label;
        this.getInput = getInput;
        this.post = post;
    }

    // inp was namedInputSum("time")
    public boolean mod() {
        int inp = getInput.apply(label);
        if (!mod) return false;
        float delta = range * inp / amp;
        if ((delta + base) * scale == current) return false;
        current = (delta + base) * scale;
        //out.println(label+".mod(): setting current to "+current);

        if (post != null) post.accept(current);
        return true;
    }

    public float getBase() { return base; }
    public float getRange() { return range; }
    public float getCurrent() { return current; }

    public void setScale( float val) {
        scale = val;
        current = base * scale;
    }
}
