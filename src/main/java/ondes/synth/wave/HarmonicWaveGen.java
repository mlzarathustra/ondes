package ondes.synth.wave;

import ondes.synth.wave.lookup.CompositeWave;
import ondes.synth.wave.lookup.FloatWaveLookup;
import ondes.synth.wave.lookup.WaveLookup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;

import static java.util.stream.Collectors.joining;


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
class HarmonicWaveGen extends CompositeWave {

    @Override
    public void setFreq(float midiFrequency) {
        super.setFreq(midiFrequency);
    }

    /** the keys of the "hash-map." The correspondingly indexed
     *  element of waves is the value.
     */
    private String[] presetTags ={ "mellow","odd","bell", "organ" };

    /**
     * the values, as a set of pairs. The pairs are each:
     *      frequency multiplier, divisor
     */
    private float[][] presets = {
            {1,1, 2,2, 3,3},
            {1,1, 2,2, 6,3, 14,3},
            {1,1, 2,2, 11,3, 14,3, 17,3},
            {1,1, 2,2, 3,3, 4,2, 8,2, 12,3}
    };

    private float[] getWave(String preset) {
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
        if (preset != null) harmonicParams = getWave(preset.toString());

        Object waveConfig = config.get("waves");
        if (waveConfig instanceof List) {
            if (preset != null) {
                err.println("warning: you have given both a preset and \n" +
                    "a set of pitches for a composite wave.\n" +
                    "The preset will be ignored.");
            }

            // it's just numeric pairs, so they can put them all on
            // one line, or split them.
            // todo - permit wave name after (e.g. ramp-up, square)
            String[] waveTokens = listToTokenAry((List)waveConfig);

            harmonicParams = new float[waveTokens.length];
            for (int i = 0; i< harmonicParams.length; ++i) {
                try { harmonicParams[i] = Float.parseFloat(waveTokens[i]); }
                catch (Exception ex) {
                    err.println("could not parse "+waveTokens[i]+" as float");
                }
//                if (harmonicParams[i] <= 0) {
//                    err.println("wave values must be greater than zero.\n" +
//                        "falling back to default set.");
//                    harmonicParams = this.presets[0];
//                    break;
//                }
            }
        }

        String waveKey = Arrays.toString(harmonicParams);
        waveLookup = waveLookups.get(waveKey);
        if (waveLookup == null) {
            waveLookup = new FloatWaveLookup(this::valueAtPhase);
            waveLookups.put(waveKey, waveLookup);
        }

        if (!(preset instanceof String) && presets == null) {
            err.println("harmonic wave form currently requires either a" +
                " preset string or a list of values.\n");
            err.println("current presets: "+String.join(" ", presetTags));
        }
    }

    /**
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        modFreq();
        return (int)(
            waveLookup.valueAt((float)phaseClock.getPhase()) * getAmp()
        );

    }
}
