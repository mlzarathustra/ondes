package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;
import static java.lang.System.out;
import static ondes.synth.component.ConfigHelper.getDouble;
import static ondes.synth.component.ConfigHelper.getInAmpPair;

public class Balancer extends MonoComponent {

    double scale = 1;

    int ctrlInputAmp = 1000, ctrlInitialValue = 500;
    int ctrlValue = ctrlInitialValue;

    void reset() {
        ctrlValue = ctrlInitialValue;
    }


    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);
        Double dblInp;
        dblInp = getDouble( config.get("level-scale"),
            "'level-scale' must be a number to multiply the output value by.\n" +
                "  '1' is default. Can be floating point.");
        if (dblInp != null) scale = dblInp;

        Object[] prInp = getInAmpPair(config, "input-ctrl", "initial-value");
        if (prInp != null) {
            ctrlInputAmp = (int)prInp[0];
            ctrlInitialValue = ((Float) prInp[1]).intValue();
        }
        reset();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { reset(); }

    @Override
    public int currentValue() {
        float L = namedInputSum("left"), R = namedInputSum("right"),
            ctrl = namedInputSum("ctrl");

        //  if ctrlInputAmp is 1000, the value will be
        //   -1000 <= ctrl <= 1000
        //
        float lScale = ((ctrl / ctrlInputAmp)+1f)/2f;
        float val = lScale * L + (1.0f - lScale) * R;

        return (int)(val * scale);
    }
}
