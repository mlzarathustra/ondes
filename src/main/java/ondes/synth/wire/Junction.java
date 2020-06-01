package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;

import static java.lang.System.err;

/**
 *
 * <p>
 *     Basically a mixer with a single volume (scale) control.
 *     It multiplies the sum of its input by "scale."
 *     About the simplest component you could have!
 * </p>
 * <p>
 *      For more control over the input levels,
 *      @see ondes.synth.wire.ScalingWiredIntSupplier
 * </p>
 */
public class Junction extends MonoComponent {

    private double levelScale = 1;
    void setLevelScale(double v) { levelScale = v; }

    public Junction() { super(); }

    @Override
    public int currentValue() {
        // a manual loop is slightly faster than the lambda.
        int rs=0;
        for (WiredIntSupplier input : inputs) rs += input.getAsInt();
        return (int)(levelScale * rs);
    }

    // // // //

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components); // set outputs

        Float fltInp;
        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

}
