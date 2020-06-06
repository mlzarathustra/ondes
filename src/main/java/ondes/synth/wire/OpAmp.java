package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.List;
import java.util.Map;
import static java.lang.System.out;

import static java.lang.System.err;
import static ondes.mlz.Util.getList;

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
        // a manual loop is slightly faster than the lambda.
        double rs = 1;
        for (WiredIntSupplier input : inputs) rs *= input.getAsInt();
        //out.println("OpAmp.currentValue(): "+scale*rs);
        return (int)(scale * rs);
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

        // TODO - currently unused - remove?
        //
        dblInp= getDouble(config.get("input-amp"),
            "'input-amp' must be a number, typically " +
                "the same as the level-override of the sender.");
        if (dblInp != null) {
            inputAmp =  dblInp.floatValue();
            if (inputAmp != 0) inputAmpInv = 1.0f/inputAmp;
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

}
