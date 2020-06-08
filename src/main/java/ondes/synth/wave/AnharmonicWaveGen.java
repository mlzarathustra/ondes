package ondes.synth.wave;

import ondes.synth.Instant;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.err;
import static java.lang.System.out;

import static java.util.stream.Collectors.joining;
import static ondes.mlz.SineLookup.sineLookup;

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

    static float TAO=(float)(Math.PI*2.0);
    private int scaledAmp;


    /**
     * <p>
     *     We get a "setFreq" message that LFO's need because
     *     they don't get the Note-ON message. But we need to
     *     wait for the Note-ON so the phase clocks will have
     *     been restarted.
     *
     * </p>
     *
     * @param freq - frequency requested
     */
    @Override
    void setFreq(double freq) {
        super.setFreq(freq);
        if (clocks.size() < waves.length/2) return; // got the LFO msg

        for (int wp=0; wp<waves.length-1; wp+=2) {
            clocks.get(wp/2)
                .setFrequency( (float)(freq * waves[wp] * getFreqMultiplier()) );
        }
    }

    private final float[] defaultWave = { 1,1, 2,2, 3,3 };
    private float[] waves;

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

            waves = new float[waveTokens.length];
            for (int i = 0; i< waves.length; ++i) {
                try { waves[i] = (float)Double.parseDouble(waveTokens[i]); }
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

        if (waves == null || waves.length == 0) {
            err.println(
                "Anharmonic composite wave form requires \n" +
                    "a list of value pairs (frequency, divisor).\n");
        }
        else synth.getInstant().reservePhaseClocks(waves.length + 1);
    }

    // TODO - pool phase clocks
    //         the program named "almost" uses this, and overloads a lot
    //         I'm betting the allocation and garbage collect are why.

    @Override
    public void resume() {
        super.resume();
        for (int i=0; i<waves.length-1; i += 2) {
            clocks.add(synth.getInstant().addPhaseClock());
        }
    }

    @Override
    public void pause() {
        super.pause();
        clocks.forEach( synth.getInstant()::delPhaseClock );
        clocks.clear();
    }

    /**
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        float sum=0;
        for (int ov = 0; ov< waves.length-1; ov+=2) {
            sum += sineLookup( clocks.get(ov/2).getPhase() * TAO )
                / waves[ov+1];
        }

        return (int) (sum * getAmp());
    }

    // DEBUG HACK
    @Override
    public void noteON(MidiMessage msg) {
        super.noteON(msg);
    }




}
