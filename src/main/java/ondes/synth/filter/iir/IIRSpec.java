package ondes.synth.filter.iir;

/**
 * <p>
 *    With the wide difference in the scale of values,
 *    (e.g. 1e11 - 1e-11) the calculations need to be
 *    double in order to avoid instability.
 * </p>
 *  */
public class IIRSpec {
    String key;
    double[] a, b;
    IIRSpec(String key, double[][]ab) {
        this.key = key;
        a = ab[0];
        b = ab[1];

    }
}
