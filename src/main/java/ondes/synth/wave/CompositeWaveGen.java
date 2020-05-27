package ondes.synth.wave;

import java.util.Arrays;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

import static ondes.mlz.SineLookup.sineLookup;


/**
 * <p>
 *   perform additive synthesis by constructing a wave composed of one or more harmonics
 *   of the base frequency (in sine waves).
 * </p>
 * <p>
 *   The composition of the wave is currently determined by an ASCII key(word)
 *   which is mapped to a set of values giving the frequency and divisor
 *
 * </p>
 *
 */
class CompositeWaveGen extends WaveGen {

    static double TAO=Math.PI*2.0;
    private int scaledAmp;

    @Override
    void setFreq(double freq) {
        super.setFreq(freq);

        //scaledAmp = amp;

        scaledAmp = (int)(getAmp() * (1.0 + 55.0/(2*freq) ));
        // pitch scaling, to help out the bass notes
    }

    /** the keys of the "hash-map." The correspondingly indexed
     *  element of waves is the value.
     */
    private String[] labels={ "mellow","odd","bell", "organ" };
    /**
     * the values, as a set of pairs. The pairs are each:
     *      frequency multiplier, divisor
     */
    private double[][] waves= {
            {1,1, 2,2, 3,3},
            {1,1, 2,2, 6,3, 14,3},
            {1,1, 2,2, 11,3, 14,3, 17,3},
            {1,1, 2,2, 3,3, 4,2, 8,2, 12,3}
    };

    private double[] wave=waves[0];

    double[] getWaves(String preset) {
        for (int i=0; i<labels.length; ++i) {
            if (labels[i].equals(preset)) return waves[i];
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Object preset = config.get("preset");
        double[] waves = getWaves(preset.toString());
        if (!(preset instanceof String) || waves == null) {
            err.println("composite wave form currently requires preset string");
            err.println("current options: "+String.join(" ",labels));
            return;
            // TODO - or a list of freqs/divisors
        }

        //out.println("waves: "+ Arrays.toString(waves));
    }

    /**
     * TODO - transfer the above logic to here using the sampled sine waves
     *
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        double sum=0;
        for (int ov=0; ov<wave.length-1; ov+=2) {
            sum += sineLookup(
                phaseClock.getPhase() * wave[ov] )
                / wave[ov+1];
        }

        return (int) sum * getAmp();  //   *scaledAmp?
    }
}
