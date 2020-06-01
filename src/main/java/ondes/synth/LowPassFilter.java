package ondes.synth;

import ondes.synth.component.MonoComponent;

import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static ondes.mlz.Util.getList;

public class LowPassFilter extends MonoComponent {

    float freq = 0;

    /**
     * <p>
     *     A simple low pass filter with (so far) a fixed frequency
     * </p>
     * @param config - the configuration map from YAML
     * @param components - a map of all the components
     *                   in this voice, by name.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Float fltInp;
        fltInp = getFloat(config.get("freq"),
            "'freq' must be a number. can be floating.");
        if (fltInp != null) freq = (float) fltInp;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public int currentValue() {
        return 0;
    }
}
