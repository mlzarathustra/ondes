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

    private double scale = 1;
    void setScale(double v) { scale = v; }

    public Junction() { super(); }

    @Override
    public int currentValue() {
        // a manual loop is slightly faster than the lambda.
        int rs=0;
        for (WiredIntSupplier input : inputs) rs += input.getAsInt();
        return (int)(scale * rs);
    }

    // // // //

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components); // set outputs

        // TODO - use config helper instead
        Object scaleStr = config.get("level-scale");
        if (scaleStr != null) {
            try { this.scale = Double.parseDouble(scaleStr.toString()); }
            catch (Exception ex) {
                err.println("'level-scale' must be a number to multiply the output value by.\n" +
                    "  '1' is default. Can be floating point.");
            }
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

}
