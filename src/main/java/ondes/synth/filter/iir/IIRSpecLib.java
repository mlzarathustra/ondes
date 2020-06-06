package ondes.synth.filter.iir;

import java.util.HashMap;

public class IIRSpecLib {

    public static final HashMap<String, IIRSpec>specMap = new HashMap<>();

    /**
     * <p>
     *     The current naming convention is
     *     "{type}_{order}_{freq}"
     *     Assuming a sample rate of 44100.
     * </p>
     * <p>
     *     So, "lp_6_15k" is a 6th-order low-pass filter
     *     at 15 kilohertz, at a sample rate of 44100.
     * </p>
     * <p>
     *     Used with a different sample rate, the frequency will
     *     shift accordingly, as shown in the below output from
     *     SampleRateConversion.main()
     * </p>
     * <pre>
     *     The MatLab convention: fs = sample rate. fc = cutoff frequency
     *     fs=44100.0 fc=[10,000.000 , 15,000.000 , 20,000.000 ]
     *     fs=48000.0 fc=[10,884.354 , 16,326.531 , 21,768.707 ]
     *     fs=96000.0 fc=[21,768.707 , 32,653.061 , 43,537.415 ]
     * </pre>
     * <p>
     *     It might be better to label them by the angular frequency
     *     (i.e. fc/(fs/2)) but then it's more difficult to tell
     *     what the continuous frequency would be.
     * </p>
     * <p>
     *     Of course, the best would be to just accept whatever
     *     values of (fc, fs) and do the Z-transform, but it will
     *     take some more research and study before I am up to
     *     coding that! :^)
     * </p>
     *
     *
     */
    public static final String[] specKeys = {
        "lp_6_10k",
        "lp_6_15k",
        "lp_6,20k"
    };

    /**
     * See notes/IIRCoefficients.groovy for the script that generated the below.
     * It includes the matlab calls that calculated the data.
     * For more, see https://www.mathworks.com/help/signal/ref/butter.html
     */
    public static final double[][][] specValues = {
        {
            {1.0000000, -0.5517678, 0.8913623, -0.2718709, 0.1442227, -0.0188095, 0.0023745},
            {0.018680, 0.112079, 0.280198, 0.373597, 0.280198, 0.112079, 0.018680}
        },
        {
            {1.000000, 2.144005, 2.505855, 1.690254, 0.699779, 0.162362, 0.016504},
            {0.12842, 0.77051, 1.92627, 2.56836, 1.92627, 0.77051, 0.12842}
        },
        {
            {1.00000, 4.87228, 9.97981, 10.98805, 6.85320, 2.29419, 0.32187},
            {0.56733, 3.40401, 8.51002, 11.34669, 8.51002, 3.40401, 0.56733}
        },
    };

    static {
        for (int i=0; i<specKeys.length; ++i) {
            specMap.put(specKeys[i], new IIRSpec(specKeys[i], specValues[i]));
        }
    }
    public static IIRSpec get(String key) { return specMap.get(key); }


}
