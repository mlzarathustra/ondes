package ondes.synth.filter.iir;

import java.util.HashMap;

public class IIRSpecLib {

    public static final HashMap<String, IIRSpec>specMap = new HashMap<>();

    /**
     * <p>
     *     Naming convention "{type}_{order}_{freq}"
     *     So, lp_6_15k is a 6th-order low-pass filter
     *     at 15 kilohertz.
     * </p>
     * <p>
     *     The hz labels are based on a sample rate of 44100.
     *     Used with a different sample rate, the frequency will
     *     shift accordingly.
     * </p>
     * <p>
     *     Below: the output of SampleRateConversion.main()
     *
     *     <pre>
     *        The MatLab convention: fs = sample rate. fc = cutoff frequency
     *          fs=44100.0 fc=[10,000.000 , 15,000.000 , 20,000.000 ]
     *          fs=48000.0 fc=[10,884.354 , 16,326.531 , 21,768.707 ]
     *          fs=96000.0 fc=[21,768.707 , 32,653.061 , 43,537.415 ]
     *     </pre>
     * </p>
     */
    public static final String[] specKeys = {
        "lp_6_10k",
        "lp_6_15k",
        "lp_6,20k"
    };

}
