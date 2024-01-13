package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;
import static ondes.synth.component.ConfigHelper.*;

import static java.lang.System.out;
import static java.lang.System.err;

/**
 *
 * <p>
 *     An "Operational Amplifier"
 * </p>
 * <p>
 *     It multiplies its inputs together, then
 *     multiplies the result by "scale."
 * </p>
 * <p>
 *     Since the input must be integer, scale can be used
 *     to implement fractional ranges.
 *     For example, for an EGA you probably want a range of
 *     0 to 1 in increments of 0.001.
 *     So the EGA should multiply by 1000 before returning
 *     currentValue() and set here the scale to .001.
 *     It might be slightly faster to use powers of two.
 *
 *
 * </p>
 * <p>
 *      For more control over the input levels,
 *      @see ScalingWiredIntSupplier
 * </p>
 */
public class OpAmp extends MonoComponent {

    private double scale = 1;
    private float inputAmp, inputAmpInv;
    void setScale(double v) { scale = v; }

    public OpAmp() { super(); }

    @Override
    public int currentValue() {
        //out.println("OpAmp.currentValue(): "+scale*rs);
        return (int)(scale * inputProd());
    }

    // // // //

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Double dblInp;
        dblInp = getDouble( config.get("level-scale"),
            "'level-scale' must be a number to multiply the output value by.\n" +
                "  '1' is default. Can be floating point.");
        if (dblInp != null) scale = dblInp;

    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

}
