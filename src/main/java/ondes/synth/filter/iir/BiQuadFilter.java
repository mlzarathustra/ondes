package ondes.synth.filter.iir;

import ondes.synth.filter.Filter;

import java.util.Arrays;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.System.err;
import static java.lang.System.out;
import static ondes.synth.component.ConfigHelper.*;


public class BiQuadFilter extends Filter {

    boolean DB = true;

    double freq, Q; // for display. not needed for the calculation
    Double sampleRate=null;

    double[] a = new double[3], b = new double[3];
    double [] x=new double[3], y=new double[3];

    //
    //

    int freqInputAmp, QInputAmp;
    float freqInputRange, freqOffset=0;
    float QInputRange, QOffset=0;


    boolean modFreq, modQ;


    float levelScale = 1;

    double getSampleRate() {
        if (sampleRate == null) {
            sampleRate = (double)synth.getSampleRate();
        }
        return sampleRate;
    }

    /*
            b[0] and b[2] match the butterworth values,
            but the middle term does not.
     */
    void setCoefficients(double freq, double Q) {
        double omega = 2 * PI * (freq / getSampleRate());
        double alpha = sin(omega) * sinh(0.5 / Q);

        a[0] = 1.0 + alpha;
        a[1] = -2.0 * cos(omega);
        a[2] = 1.0 - alpha;

        b[1] = 1 - cos(omega);
        b[0] = b[2] = 0.5 * b[1];

        normalize();
    }

    /**
     * <p>
     *     ported from nyq:biquad in dspprims.lsp
     * </p>
     * <p>
     *     Normalize so that a[0]=1. However, a[0]
     *     is never used, so we don't need to set it.
     * </p>
     * <p>
     *     At this point biquad-m also negates the A parameters
     *     because the Nyquist biquad function in biquadfilt.c
     *     adds them. We are subtracting them, so we don't
     *     do the negation here.
     * </p>
     */
    void normalize() {
        if (a[0] < 0) {
            err.println("Unstable parameter a0 in biquad.");
            setDefaults(); return;
        }
        double a0r = 1.0 / a[0];
        a[0] = 1;
        a[1] = a0r * a[1];
        a[2] = a0r * a[2];

        if (a[2] >= 1.0 || (1 + a[2]) <= abs(a[1])) {
            err.println("Unstable parameter a2 in biquad.");
            setDefaults(); return;
        }

        for (int i=0; i<b.length; ++i) b[i] *= a0r;
    }

    void setDefaults() {
        // if everything is 1, there is no filtering.
        Arrays.fill(a,1);
        Arrays.fill(b,1);
    }


    void showCoefficients() {
        out.println("freq: "+freq+" Q:"+Q);
        //out.println("omega: "+omega+" alpha: "+alpha);
        out.println("a="+ Arrays.toString(a));
        out.println("b="+ Arrays.toString(b));
    }

    /**
     * update modulated frequency (but not coefficients)
     * @return - did modulated frequency change?
     */
    private boolean adjustFreq() {
        if (!modFreq) return false;
        float inp = namedInputSum("freq");
        float newOffset = freqInputRange * inp / freqInputAmp;
        if (freqOffset == newOffset) return false;
        freqOffset = newOffset;
        return true;
    }

    /**
     * update modulated Q (but not coefficients)
     * @return - did modulated Q change?
     */
    private boolean adjustQ() {
        if (!modQ) return false;
        float inp = namedInputSum("Q");
        float newOffset = QInputRange * inp / QInputAmp;
        if (QOffset == newOffset) return false;
        QOffset = newOffset;
        return true;
    }

    /**
     * <p>
     *     Stolen from Nyquist (in the Audacity project) Only works for two
     *     poles. The generalized one for "n" poles is in IIRFilter.java
     * </p>
     * <p>
     *     /lib-src/libnyquist/nyquist/nyqstk/src/BiQuad.cpp
     *     Direct Form 1, as opposed to the version in biquadfilt.c
     *     which uses Direct Form 2 (and opposite-signed "a")
     * </p>
     */
    @Override
    public int currentValue() {
        if (adjustFreq() || adjustQ()) {
            setCoefficients(freq + freqOffset, Q + QOffset);
        }

        x[0] =  inputSum();
        y[0] = b[0]*x[0] + b[1]*x[1] + b[2]*x[2];
        y[0] -= a[2]*y[2] + a[1]*y[1];

        x[2] = x[1];
        x[1] = x[0];

        y[2] = y[1];
        y[1] = y[0];

        return (int) (y[0] * levelScale);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);
        Double dblInp;
        dblInp = getDouble(config.get("freq"),
            "biquad freq needs to be a decimal number.");
        if (dblInp != null) freq = dblInp;

        dblInp = getDouble(config.get("Q"),
            "biquad Q needs to be a decimal number.");
        if (dblInp != null) Q = dblInp;

        Object[] modInp = getAmpPair(config, "input-freq", "range");
        if (modInp != null) {
            freqInputAmp = (int) modInp[0];
            freqInputRange = (float) modInp[1];
            modFreq = true;
        }

        modInp = getAmpPair(config, "input-Q", "range");
        if (modInp != null) {
            QInputAmp = (int) modInp[0];
            QInputRange = (float) modInp[1];
            modQ = true;
        }



        //  Should levelScale be in MonoComponent?
        Float fltInp;
        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }

    }


    @Override
    public void pause() { }

    @Override
    public void resume() {
        setCoefficients(freq, Q);
//        a = new double[] {1,1,1};
//        b = new double[] {1,1,1};
        //
        Arrays.fill(x,0);
        Arrays.fill(y,0);
    }

    public static void main(String[] args) {

        /*  seems to max out at about a 25dB boost.
                Q=... (all are estimates) at 1000hz
                    .5 - no resonance
                     1 -   2dB
                     2 -   5
                     3 -   8
                     20 -  25
                     40 -  27
                     100 - 30
                     1000 - about the same
         */

        BiQuadFilter bq = new BiQuadFilter();
        bq.sampleRate = 44100.0;

        bq.setCoefficients(1000, 10000);
        bq.showCoefficients();
    }
}


