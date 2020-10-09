package ondes.synth.wave;

import ondes.synth.Instant;
import ondes.synth.wave.lookup.CompositeWave;
import ondes.synth.wave.lookup.WaveLookup;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.err;
import static java.lang.Math.*;

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

    /**
     * Grouped in pairs: { f, d {, f, d, ...} }
     * where f is frequency multiplier
     * and d is amplitude divisor
     */
    private double[] anharmonicParams;

    /**
     * frequency multipliers from anharmonicParams
     */
    private double[] anharmonicMults;

    /**
     * amplitude divisors from anharmonicParams
     */
    private double[] anharmonicDivs;

    /**
     * frequencies for the current MIDI note
     */
    private double[] anharmonicBaseFreqs;

    /**
     * <p>
     *     We get a "setFreq" message that LFO's need because
     *     they don't get the Note-ON message. But we need to
     *     wait for the Note-ON (which calls setFreq() again)
     *     so the phase clocks will have been restarted.
     *
     * </p>
     *
     * @param midiFrequency - frequency requested
     */
    @Override
    public synchronized void setFreq(double midiFrequency) {
        super.setFreq(midiFrequency); // sets baseFrequency too

        if (clocks.size() < anharmonicMults.length) return; // LFO msg - see above

        for (int i = 0; i < anharmonicMults.length; i++) {
            double freq = baseFrequency * anharmonicMults[i];
            anharmonicBaseFreqs[i] = freq;
            clocks.get(i).setFrequency( (float) freq );
        }
    }

    @Override
    public void modFreq() {
        if (!modLinFrequency && !modLogFrequency) return;
        super.modFreq(); // sets linearInp and logInp

        for (int i=0; i<anharmonicBaseFreqs.length; ++i) {
            double freq = anharmonicBaseFreqs[i];
            if (modLinFrequency) freq += linearInp;
            if (modLogFrequency) freq *= pow(2,logInp);
            clocks.get(i).setFrequency( (float) freq);
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
            anharmonicParams = anList.stream().mapToDouble(v->v).toArray();
            harmonicParams = hrList.stream().mapToDouble(v->v).toArray();
        }

        if ( (anharmonicParams == null || anharmonicParams.length == 0) &&
            (harmonicParams == null || harmonicParams.length == 0)) {
            err.println(
                "Anharmonic composite wave form requires \n" +
                    "a list of value pairs (frequency, divisor).\n");
            return;
        }

        // Set up ANHARMONIC waves
        if (anharmonicParams != null) {
            int anharmonicCount = anharmonicParams.length / 2;
            synth.getInstant().reservePhaseClocks(anharmonicCount);
            anharmonicMults = new double[anharmonicCount];
            anharmonicDivs = new double[anharmonicCount];
            anharmonicBaseFreqs = new double[anharmonicCount];

            for (int i = 0; i < anharmonicMults.length; ++i) {
                anharmonicMults[i] = anharmonicParams[i * 2];
                anharmonicDivs[i] = anharmonicParams[i * 2 + 1];
            }
        }

        // Set up HARMONIC waves
        // find a matching wave table, or create one if needed
        // this::valueAtPhase depends on harmonicParams
        //
        String waveKey = Arrays.toString(harmonicParams);
        waveLookup = waveLookups.get(waveKey);
        if (waveLookup == null) {
            waveLookup = new WaveLookup(this::valueAtPhase);
            waveLookups.put(waveKey, waveLookup);
        }
    }

    @Override
    public synchronized void resume() {
        super.resume();
        for (int i = 0; i< anharmonicParams.length-1; i += 2) {
            clocks.add(synth.getInstant().addPhaseClock());
        }
    }

    @Override
    public synchronized void pause() {
        super.pause();
        clocks.forEach( synth.getInstant()::delPhaseClock );
        clocks.clear();
    }

    /**
     * @return component level at the instant of this sample.
     */
    @Override
    public synchronized int currentValue() {
        if (clocks.isEmpty()) return 0;
        modFreq();
        double sum=0;
        for (int ov = 0; ov< anharmonicParams.length-1; ov+=2) {
            sum += sineLookup( clocks.get(ov/2).getPhase() )
                / anharmonicParams[ov+1];
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
