package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;

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
    void setScale(double v) { scale = v; }

    public OpAmp() { super(); }

    @Override
    public int currentValue() {
        // a manual loop is slightly faster than the lambda.
        double rs = 1;
        for (WiredIntSupplier input : inputs) rs *= input.getAsInt();
        return (int)(scale * rs);
    }

    // // // //

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        Object scaleStr = config.get("scale");
        if (scaleStr != null) {
            try { this.scale = Double.parseDouble(scaleStr.toString()); }
            catch (Exception ex) {
                err.println("'scale' must be a number to mutiply the output value by.\n" +
                    "  '1' is default. Can be floating point.");
            }
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

}
