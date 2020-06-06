package ondes.synth.wave;

import ondes.mlz.WaveLookup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

import static java.util.stream.Collectors.joining;
import static ondes.mlz.SineLookup.sineLookup;


/**
 * <p>
 *      perform additive synthesis by constructing a wave composed of one
 *      or more harmonics of the base frequency (in sine waves).
 * </p>
 * <p>
 *      Some presets are available by using the 'preset:' keyword
 *      in yaml. Alternately, you may define a set of (sine) waves, each
 *      designated by a pair of numbers:
 *      <pre>
 *          Harmonic Divisor
 *      </pre>
 *
 *      So [2 3] would mean the octave (f*2) at a third the amplitude.
 *      The sets of waves are summed together.
 * </p>
 * <p>
 *     Note that, while you may specify non-integer frequencies for the
 *     harmonics, there is only one phase clock, so the resulting wave
 *     will be harmonic as the higher frequencies jump back to phase 0
 *     mid-wave. So far the only effect I've heard is that it gets kind
 *     of buzzy.
 *
 * </p>
 *
 */
@SuppressWarnings("FieldMayBeFinal")
class HarmonicWaveGen extends WaveGen {

    static double TAO=Math.PI*2.0;

    static HashMap<String,WaveLookup> waveLookups=new HashMap<>();

    WaveLookup waveLookup;

    @Override
    void setFreq(double freq) {
        super.setFreq(freq);
    }

    /** the keys of the "hash-map." The correspondingly indexed
     *  element of waves is the value.
     */
    private String[] presetTags ={ "mellow","odd","bell", "organ" };

    /**
     * the values, as a set of pairs. The pairs are each:
     *      frequency multiplier, divisor
     */
    private double[][] presets = {
            {1,1, 2,2, 3,3},
            {1,1, 2,2, 6,3, 14,3},
            {1,1, 2,2, 11,3, 14,3, 17,3},
            {1,1, 2,2, 3,3, 4,2, 8,2, 12,3}
    };

    private double[] waves = presets[0];

    double[] getWave(String preset) {
        for (int i = 0; i< presetTags.length; ++i) {
            if (presetTags[i].equals(preset)) return presets[i];
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes,unchecked")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Object preset = config.get("preset");
        if (preset != null) waves = getWave(preset.toString());

        Object waveConfig = config.get("waves");
        if (waveConfig instanceof List) {
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

            waves = new double[waveTokens.length];
            for (int i = 0; i< waves.length; ++i) {
                try { waves[i] = Double.parseDouble(waveTokens[i]); }
                catch (Exception ex) {
                    err.println("could not parse "+waveTokens[i]+" as float");
                }
                if (waves[i] <= 0) {
                    err.println("wave values must be greater than zero.\n" +
                        "falling back to default set.");
                    waves = this.presets[0];
                    break;
                }
            }
        }

        String waveKey = Arrays.toString(waves);
        waveLookup = waveLookups.get(waveKey);
        if (waveLookup == null) {
            //out.println("generating wave lookup.");
            waveLookup = new WaveLookup(this::currentValue);
            waveLookups.put(waveKey, waveLookup);
        }

        if (!(preset instanceof String) && presets == null) {
            err.println("harmonic wave form currently requires either a" +
                " preset string or a list of values.\n");
            err.println("current presets: "+String.join(" ", presetTags));
        }
    }

    private double currentValue(double phase) {
        double sum=0;
        for (int ov = 0; ov< waves.length-1; ov+=2) {
            sum += sineLookup(
                phase * TAO * waves[ov] )
                / waves[ov+1];
        }

        return sum;
    }


    /**
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        return (int)(
            waveLookup.valueAt(phaseClock.getPhase()) * getAmp()
        );

    }
}
