package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;

/**
 *
 * <p>
 *     Basically a mixer with a single volume (scale) control.
 *     It multiplies the sum of its input by "scale."
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
    public void configure(Map config, Map components) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void release() { }


}
