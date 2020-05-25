package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import java.util.Map;

/**
 * Basically a mixer with a single volume (scale) control.
 * It multiplies the sum of its input by "scale."
 * <br/><br/>
 *
 * For more control over the input levels,
 * @see ondes.synth.wire.ScalingWiredIntSupplier
 * <br/><br/>
 *
 */
public class Junction extends MonoComponent {

    private double scale = 1;
    void setScale(double v) { scale = v; }

    public Junction() { super(); }

    public Junction(WiredIntSupplier in) {
        super();
        addInput(in);
//        mainOutput = new WiredIntSupplier() {
//            public int updateInputs() {
//                return getValue();
//            }
//        };

    }

    @Override
    public int currentValue() {
        // a manual loop is slightly faster than the lambda.
        int rs=0;
        for (WiredIntSupplier input : inputs) rs += input.getAsInt();
        return (int)(scale * rs);
    }


    // // // //

    @Override
    public void configure(Map config, Map components) {

        // todo - implement

    }


}
