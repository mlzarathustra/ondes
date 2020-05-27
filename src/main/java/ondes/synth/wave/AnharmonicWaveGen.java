package ondes.synth.wave;

import ondes.synth.Instant;

import java.util.*;

import static java.lang.System.err;
import static java.util.stream.Collectors.joining;

/**
 * <p>
 *   Perform additive synthesis by constructing a wave composed
 *   of one or more frequencies above the base frequency (in sine waves).
 *   These frequencies may be harmonic, but it is not necessary.
 *
 * </p>
 * <p>
 *   Similar to CompositeWaveGen but for this one we will
 *   create a snapshot of a single cycle instead of generating
 *   the sine waves each time.
 * </p>
 * <p>
 *   The composition of the wave is currently determined by an ASCII key(word)
 *   which is mapped to a set of values giving the frequency and divisor
 *
 * </p>
 *
 */
class AnharmonicWaveGen extends WaveGen {
    /**
     * The fundamental phase clock remains in WaveGen.
     * Additional anharmonic waves each need their own.
     */
    List<Instant.PhaseClock> clocks = new ArrayList<>();

    static double TAO=Math.PI*2.0;
    private int scaledAmp;

    @Override
    void setFreq(double freq) {
        super.setFreq(freq);

        scaledAmp = (int)(getAmp() * (1.0 + 55.0/(2*freq) ));
        // TODO - pitch scaling, to help out the bass notes
        //        research: what is the right formula for it?
    }

    private final double[] defaultWave = { 1,1, 2,2, 3,3 };
    private double[] waves;

    @Override
    @SuppressWarnings("rawtypes,unchecked")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Object waveConfig = config.get("waves");
        if (waveConfig instanceof List) {

            // it's just numeric pairs, so you can put them all on
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
                    waves = defaultWave;
                    break;
                }
            }
        }

        if (waves == null) {
            err.println(
                "Anharmonic composite wave form currently requires \n" +
                    "a list of value pairs (frequency, divisor).\n");
        }
    }


    @Override
    public int currentValue() {
        return 0;
    }
}
