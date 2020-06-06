package ondes.synth.filter.iir;

import java.util.HashMap;

import static java.lang.System.err;

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
        //  6 pole
        //"lp_6_440",  // completely unstable.
        "lp_6_1k",     // becomes unstable easily
        "lp_6_2k",
        "lp_6_5k",
        "lp_6_10k",
        "lp_6_15k",
        "lp_6_20k",

        //  4 pole
        "lp_4_440",
        "lp_4_1k",
    };

    /**
     * See notes/IIRCoefficients.groovy for the script that generated the below.
     * It includes the matlab calls that calculated the data.
     * For more, see https://www.mathworks.com/help/signal/ref/butter.html
     */
    public static final double[][][] specValues = {
        //
        //              6-pole
        //
        //
//        { // 440 - completely unstable
//            {1.00000, -5.75779, 13.81813, -17.69237, 12.74637, -4.89919, 0.78485},
//            {0.00000000084184, 0.00000000505105, 0.00000001262763, 0.00000001683684, 0.00000001262763, 0.00000000505105, 0.00000000084184}
//        },
        { // 1,000 - not recommended - becomes unstable easily
            {1.00000, -5.44961, 12.39746, -15.06844, 10.31945, -3.77519, 0.57633},
            {0.00000010023, 0.00000060141, 0.00000150351, 0.00000200469, 0.00000150351, 0.00000060141, 0.00000010023}
        },
        { // 2,000
            {1.00000, -4.89975, 10.08780, -11.15976, 6.99104, -2.34999, 0.33097},
            {0.0000050126, 0.0000300753, 0.0000751883, 0.0001002511, 0.0000751883, 0.0000300753, 0.0000050126}
        },
        { // 5,000
            {1.000000, -3.257599, 4.806147, -3.982222, 1.932197, -0.516327, 0.059061},
            {0.00064464, 0.00386786, 0.00966965, 0.01289287, 0.00966965, 0.00386786, 0.00064464}
        },
        { // 10,000
            {1.0000000, -0.5517678, 0.8913623, -0.2718709, 0.1442227, -0.0188095, 0.0023745},
            {0.018680, 0.112079, 0.280198, 0.373597, 0.280198, 0.112079, 0.018680}
        },
        { // 15,000
            {1.000000, 2.144005, 2.505855, 1.690254, 0.699779, 0.162362, 0.016504},
            {0.12842, 0.77051, 1.92627, 2.56836, 1.92627, 0.77051, 0.12842}
        },
        { // 20,000
            {1.00000, 4.87228, 9.97981, 10.98805, 6.85320, 2.29419, 0.32187},
            {0.56733, 3.40401, 8.51002, 11.34669, 8.51002, 3.40401, 0.56733}
        },

        //
        //              4-pole
        //
        //
        { // 440
            {1.00000, -3.83620, 5.52188, -3.53454, 0.84887},
            {0.00000089052, 0.00000356210, 0.00000534315, 0.00000356210, 0.00000089052}
        },

        { // 1,000
            {1.00000, -3.62784, 4.95123, -3.01192, 0.68889},
            {0.000021521, 0.000086084, 0.000129126, 0.000086084, 0.000021521}
        },

    };

    static {
        if (specKeys.length != specValues.length) {
            err.println("IIRSpecLib - keys and values are of different length!");
            System.exit(-1);
        }
        for (int i=0; i<specKeys.length; ++i) {
            specMap.put(specKeys[i], new IIRSpec(specKeys[i], specValues[i]));
        }
    }
    public static IIRSpec get(String key) { return specMap.get(key); }


}
