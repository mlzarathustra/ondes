package ondes.synth.wave;

import ondes.synth.Instant;
import ondes.synth.wave.lookup.CompositeWave;
import ondes.synth.wave.lookup.WaveLookup;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.err;
import static java.lang.System.out;

import static java.util.stream.Collectors.*;
import static ondes.synth.wave.lookup.SineLookup.sineLookup;

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
class AnharmonicWaveGen extends CompositeWave {
    /**
     * The fundamental phase clock remains in WaveGen.
     * Additional anharmonic waves each need their own.
     */
    private final List<Instant.PhaseClock> clocks = new ArrayList<>();
    private double[] anharmonicWaves;

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
    public void setFreq(double freq) {
        super.setFreq(freq);
        if (clocks.size() < anharmonicWaves.length/2) return; // got the LFO msg

        for (int wp = 0; wp< anharmonicWaves.length-1; wp+=2) {
            clocks.get(wp/2)
                .setFrequency( (float)(
                    freq * anharmonicWaves[wp] * getFreqMultiplier())
                );
        }
    }


    @Override
    @SuppressWarnings("rawtypes,unchecked")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Object waveConfig = config.get("waves");
        if (waveConfig instanceof List) {

            // it's just numeric pairs, so you can put them all on
            // one line, or split them.
            String[] waveTokenAry = listToTokenAry((List)waveConfig);

            //  The inputs are in pairs: { freq, divisor }
            List<Double>
                hrList = new ArrayList<>(),
                anList = new ArrayList<>();
            double freqInp, divInp;
            for (int i=0; i<waveTokenAry.length; i += 2) {
                String freqToken = waveTokenAry[i], divToken = waveTokenAry[i+1];
                try {
                    freqInp = Double.parseDouble(freqToken);
                    divInp = Double.parseDouble(divToken);
                    if (freqInp < 0 || divInp < 0) {
                        err.println("ERROR! Anharmonic wave values " +
                            "must be greater than zero.");
                        continue;
                    }
                    if (freqInp % 1.0 == 0) {
                        hrList.add(freqInp); hrList.add(divInp);
                    }
                    else {
                        anList.add(freqInp); anList.add(divInp);
                    }
                }
                catch (Exception ex) {
                    err.println("ERROR! Could not parse one of {"+
                        freqToken+", "+divToken+"} as float");
                }
            }
            anharmonicWaves = anList.stream().mapToDouble(v->v).toArray();
            harmonicWaves = hrList.stream().mapToDouble(v->v).toArray();
        }

        if (( anharmonicWaves == null || anharmonicWaves.length == 0) &&
            (harmonicWaves == null || harmonicWaves.length == 0)) {
            err.println(
                "Anharmonic composite wave form requires \n" +
                    "a list of value pairs (frequency, divisor).\n");
            return;
        }

        // Set up ANHARMONIC waves
        synth.getInstant().reservePhaseClocks(anharmonicWaves.length + 1);

        // Set up HARMONIC waves
        String waveKey = Arrays.toString(harmonicWaves);
        waveLookup = waveLookups.get(waveKey);
        if (waveLookup == null) {
            waveLookup = new WaveLookup(this::currentValue);
            waveLookups.put(waveKey, waveLookup);
        }
    }

    // TODO - pool phase clocks
    //         the program named "almost" uses this, and overloads a lot
    //         I'm betting the allocation and garbage collect are why.

    @Override
    public void resume() {
        super.resume();
        for (int i = 0; i< anharmonicWaves.length-1; i += 2) {
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
        double sum=0;
        for (int ov = 0; ov< anharmonicWaves.length-1; ov+=2) {
            sum += sineLookup( clocks.get(ov/2).getPhase() * TAO )
                / anharmonicWaves[ov+1];
        }
        sum +=  waveLookup.valueAt(phaseClock.getPhase()) ;
        //out.println(sum);

        return (int) (sum * getAmp());
    }

    // DEBUG HACK
    @Override
    public void noteON(MidiMessage msg) {
        super.noteON(msg);
    }




}
