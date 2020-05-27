package ondes.synth.wave;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

import static java.util.stream.Collectors.joining;
import static ondes.mlz.SineLookup.sineLookup;


/**
 * <p>
 *   perform additive synthesis by constructing a wave composed of one
 *   or more harmonics of the base frequency (in sine waves).
 * </p>
 * <p>
 *   The composition of the wave is currently determined by an ASCII key(word)
 *   which is mapped to a set of values giving the frequency and divisor
 *
 * </p>
 *
 */
@SuppressWarnings("FieldMayBeFinal")
class CompositeWaveGen extends WaveGen {

    static double TAO=Math.PI*2.0;
    private int scaledAmp;

    @Override
    void setFreq(double freq) {
        //out.println("composite.setFreq()=" + freq);
        super.setFreq(freq);

        //scaledAmp = amp;

        scaledAmp = (int)(getAmp() * (1.0 + 55.0/(2*freq) ));
        // pitch scaling, to help out the bass notes
    }

    /** the keys of the "hash-map." The correspondingly indexed
     *  element of waves is the value.
     */
    private String[] presets ={ "mellow","odd","bell", "organ" };
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

    double[] getWave(String preset) {
        for (int i = 0; i< presets.length; ++i) {
            if (presets[i].equals(preset)) return waves[i];
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes,unchecked")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Object preset = config.get("preset");
        if (preset != null) wave = getWave(preset.toString());

        Object waveConfig = config.get("waves");
        if (waveConfig instanceof List) {
//            out.println("Got list: "+waveConfig);

            if (preset != null) {
                err.println("warning: you have given both a preset and \n" +
                    "a set of pitches for a composite wave.\n" +
                    "The preset will be ignored.");
            }

            // it's just numeric pairs, so they can put them all on
            // one line, or split them.
            String waveString = ""+((List)waveConfig)
                .stream()
                .map(Object::toString)
                .collect(joining(" "));
            String[] waveTokens = waveString.split("[\\s,]+");

            wave = new double[waveTokens.length];
            for (int i=0; i<wave.length; ++i) {
                try { wave[i] = Double.parseDouble(waveTokens[i]); }
                catch (Exception ex) {
                    err.println("could not parse "+waveTokens[i]+" as float");
                }
                if (wave[i] <= 0) {
                    err.println("wave values must be greater than zero.\n" +
                        "falling back to default set.");
                    wave = this.waves[0];
                    break;
                }
            }

//            out.println("exiting configure.");
//            out.println(waveString);
//            out.println(Arrays.toString(wave));

            // .split("[\\s,]+")

        }


        if (!(preset instanceof String) && waves == null) {
            err.println("composite wave form currently requires either a" +
                " preset string or a list of values.\n");
            err.println("current presets: "+String.join(" ", presets));
        }

        //out.println("waves: "+ Arrays.toString(waves));
    }

    /**
     *
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        //out.println("currentValue() wave="+Arrays.toString(wave));
        double sum=0;
        for (int ov=0; ov<wave.length-1; ov+=2) {
            sum += sineLookup(
                phaseClock.getPhase() * TAO * wave[ov] )
                / wave[ov+1];
        }

        //out.println("v="+sum+"; amp="+getAmp());
        return (int) (sum * getAmp());  //   *scaledAmp?
    }
}
