package ondes.synth.wave;

import ondes.midi.MlzMidi;
import ondes.synth.component.MonoComponent;
import ondes.synth.Instant;
import ondes.midi.FreqTable;

import javax.sound.midi.MidiMessage;
import java.util.Map;

import static java.lang.Math.*;
import static ondes.midi.MlzMidi.showBytes;
import static ondes.mlz.PitchScaling.*;
import static ondes.synth.component.ConfigHelper.*;


import static java.lang.System.err;
import static java.lang.System.out;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 */
public abstract class WaveGen extends MonoComponent {
    public static final boolean VERBOSE = false;

    protected Instant.PhaseClock phaseClock;
    static final double oneStep = pow(2, 1.0/12);
    static final double oneCent = pow(2, 1.0/1200);


    /**
     * <p>
     *     Scale the output level of this wave gen
     *     by pitch.
     * </p>
     * <p>
     *     Note that if we're using ampOverride,
     *     (invoked by the "level-override" property)
     *     scale will be ignored.
     * </p>
     */
    float pitchScale = 1;


    /**
     * <p>
     *     Scale the output level of this wave gen
     *     as directed by the "level-scale"
     *     configuration property.
     * </p>
     * <p>
     *     Note that if we're using ampOverride,
     *     (invoked by the "level-override" property)
     *     scale will be ignored.
     * </p>
     */
    float levelScale = 1;

    /**
     *  the frequency from the MIDI note number
     */
    protected float midiFrequency = -1;

    /**
     *  the MIDI frequency plus the specified
     *  offset + detune
     */
    protected float baseFrequency = -1;


    /**
     *  if positive, overrides MIDI
     */
    protected float freqOverride = -1;

    /**
     * <p>
     *     Base amplitude. 1024 works well so far. The output is 16 bits wide
     *     but the intermediate signals travel over integers, which are 32.
     * </p>
     * <p>
     *     This will be multiplied by the pitchScale, levelScale, and
     *     velocityScale to arrive at the actual amplitude.
     * </p>
     */
    private final int ampBase = 1024;

    /**
     * <p>
     *     The actual amplitude. We calculate it when processing the note-ON, so
     *     we can multiply by pitchScale and velocityScale.
     * </p>
     * <p>
     *     ampOverride will always override all of these.
     * </p>
     *
     */
    private int amplitude = ampBase;

    /**
     * if they specify an amplitude with the "level-override" configuration key,
     * it overrides the calculation.
     *
     * @see #getAmp()
     * @see #configure(Map, Map)
     */
    private int ampOverride = -1;

    //  LOG FM

    private int logInputAmp = 0;
    private float logModMaxExp = 0;


    //  Linear FM

    private int linearInputAmp = 0;

    private float linearInputRatio = 0;
    private float linearInputFreq = 0;

    // FM in general

    protected boolean modLinFrequency=false;
    protected boolean modLogFrequency=false;


    /**
     * <p>
     *     range: 0-1; the scaling when velocity is zero.
     *     default: 0. Set using "velocity-base"
     * </p>
     * <p>
     *     Given as a percentage, so this will be the number
     *     in the configuration file / 100.0
     * </p>
     * <p>
     *     @see #velocityMultiplier(int)
     * </p>
     */
    float velocityBase = 0;
    /**
     * <p>
     *     range 0-1; the multiplier on the velocity to be
     *     added to the base. default: 1; set using "velocity-amount"
     * </p>
     * <p>
     *     The incoming velocity is 0-127, so we divide by 128.0
     *     to get a number from 0 to 1. (because 128 is a power
     *     of 2, it should be fast).
     *
     *     Then multiply again by velocityAmount (which is
     *     the percentage given in the config / 100.0
     * </p>
     * <p>
     *     @see #velocityMultiplier(int)
     * </p>
     */
    float velocityAmount = 1;

    /**
     * <p>
     *     Nyquist uses a "rule" for velocity
     *     "that maps -60 dB to 1 and 0 dB to 127"
     *     If 6dB is twice as loud, and if we also interpret amplitude
     *     as volume, that would mean ampl = 2^(vel / 12.7)
     * </p>
     *
     * @param vel MIDI velocity, 1-128
     * @return - the corresponding multiplier, from 0 to 1
     *      based on velocityBase and velocityAmount.
     */
    float velocityMultiplier(int vel) {
        return (float) min(1.0,
            velocityBase + velocityAmount * ((float)vel)/128.0 );

    }

    protected boolean signed = true;

    /**
     * The amplitude on either side of the X axis.
     * So if the amplitude is 10, the values range from -10 to 10
     *
     * Can be overridden by the "level-override" value. Otherwise,
     * it's an equation based on the MIDI key, velocity, and
     * "level-scale" value.
     *
     * @return
     */
    public int getAmp() {
        //out.println("getAmp(): ampOverride is "+ampOverride);
        if (ampOverride >= 0) return ampOverride;
        return amplitude;
    }

    float detune = 0;  // detune in cents
    int offset = 0;    // interval offset in minor seconds

    /**
     * The amount of pitch scaling to do.
     * @see ondes.mlz.PitchScaling
     */
    double pitchScaleFactor = 10;

    Double freqMultiplier = null;
    public double getFreqMultiplier() {
        if (freqMultiplier != null) return freqMultiplier;
        if (detune == 0 && offset == 0) {
            freqMultiplier=1.0;
        }
        else {
            freqMultiplier =
                pow(oneStep,offset) * pow(oneCent,detune);
        }
        return freqMultiplier;
    }

    /**
     *    If set, we will display the frequency span of the
     *    linear modulation. The percentage is what we want
     *    to capture.
     *
     *    todo - to be clean, this should get reset on note-on
     */
    boolean TRACE_LINEAR_MOD = false;
    double lastPercent=0;

    class ModTracker {
        final int MT_SAMPLES = 44100; // 1 trace/second
        int ltIdx = 0;
        double curMin, curMax;

        public void trackLinMod(double freq) {
            if (ltIdx % MT_SAMPLES == 0 && (curMin != 0 || curMax != 0)) {
                double delta = curMax - midiFrequency; // should be symmetrical
                double percent = 100.0 * delta / midiFrequency;
                if (lastPercent != 0 && abs(percent - lastPercent) > 0.0001) {
                    out.printf(
                        "["+getName() +"] "+
                        "   %10.4f %% LINEAR MODULATION   | " + //" min: %10.4f  max: %10.4f " +
                            " base: %10.4f  delta: %10.4f%n",
                        percent, curMin, curMax, midiFrequency, delta);
                }
                lastPercent = percent;
                curMin = Double.POSITIVE_INFINITY;
                curMax = Double.NEGATIVE_INFINITY;
                ltIdx = 0;
            }
            if (freq > curMax) curMax = freq;
            if (freq < curMin) curMin = freq;
            ltIdx = (ltIdx + 1) % MT_SAMPLES;
        }
    }
    ModTracker mt = new ModTracker();
    static final double LOG2 = log(2);

    /**
     *    the label of another WaveGen to track.
     *    if trace-relative is set to a WaveGen component,
     *    we will display the relative frequency in semitones
     *    as it changes. To use for introspecting FM sounds.
     *
     *    The "trace-relative" property needs to go into the
     *    oscillator being modulated, as we look a the
     *    BASE frequency of this oscillator and the
     *    CURRENT frequency of the other oscillator
     *    (i.e. the phaseClock.getFrequency())
     *
     */
    class SemitoneDiffTracker {
        final int MT_SAMPLES = 44100; // 1 trace/second
        int ltIdx = 0;

        WaveGen waveGen;
        SemitoneDiffTracker(WaveGen wg) { waveGen = wg; }
        public void trackSemitones() {
            double otherFreq = waveGen.phaseClock.getFrequency();
            if (baseFrequency == 0 || otherFreq == 0) return;
            double diff = 12.0 *
                abs(log(baseFrequency / otherFreq)) / LOG2;
            if (ltIdx == 0) {  // once per second
                out.printf(
                    "["+getName()+":"+waveGen.getName()+"] "+
                    "  FREQ DIFF: %10.4f SEMITONES  | ", diff);
                out.printf(" %s: %10.4f %s: %10.4f %n",
                    getName(), baseFrequency, waveGen.getName(), waveGen.phaseClock.getFrequency());
            }
            ltIdx = (ltIdx + 1) % MT_SAMPLES;
        }
    }
    SemitoneDiffTracker dt = null;

    // to be used by overriding modFreq()
    protected double linearInp, logInp;

    /**
     * <p>
     *    When modulating the frequency, we don't re-adjust the amplitude
     *    scaling as we do with a note-ON. Nor do we set touch the 'baseFrequency.'
     *    We just set the phase clock.
     * </p>
     * <p>
     *     Waves with other phase clocks will need to override (calling this
     *     one first).
     *
     *     TODO - anharmonic, in particular
     * </p>
     * <p>
     *     Not to be confused with the "rocker freq." :^)
     * </p>
     */
    protected void modFreq() {
        // todo - create WaveGen.currentValue() and require the child classes
        //      to call THAT, instead of calling modFreq() directly
        //      since the below trace isn't modulating frequency
        //      hence "modFreq" is the wrong place for it.
        //
        if (dt != null) dt.trackSemitones();

        if (!modLinFrequency && !modLogFrequency) return;

        double freq= baseFrequency;

        if (modLinFrequency) {
            linearInp=((double)namedInputSum("linear"))/linearInputAmp
                * linearInputFreq;
            freq += linearInp;
            if (TRACE_LINEAR_MOD) mt.trackLinMod(freq);
        }
        if (modLogFrequency) {
            logInp=((double)namedInputSum("log"))/logInputAmp
                * logModMaxExp;
            freq = freq * pow(2,logInp);
        }

//        out.print("...linearInp is "+linearInp);

        phaseClock.setFrequency((float)freq);
    }

    /**
     * <p>
     * Set the phase clock frequency. If freqOverride has been set (>0)
     * we use that frequency rather than the one coming in. Also, we
     * set the amplitude scaling from this pitch (lower notes need to be
     * boosted to sound as loud) and set the `baseFrequency`
     * </p>
     * @param midiFrequency - the desired frequency
     */
    public void setFreq(float midiFrequency) {
        if (freqOverride > 0) {
            midiFrequency = freqOverride;
        }
        // noise has no frequency, but should not be scaled at infinity!
        // Note that the lowest note available in MIDI is 8.175 hz
        if (midiFrequency > 8) pitchScale = (float)getScaling(pitchScaleFactor, midiFrequency);

        baseFrequency = midiFrequency * (float)getFreqMultiplier();
        phaseClock.setFrequency((float) baseFrequency);
        this.midiFrequency = midiFrequency;
    }

    /**
     * @param msg - a Note-ON message for this channel
     */
    @Override
    public void noteON(MidiMessage msg) {
        float midiFreq = (float)FreqTable.getFreq(msg.getMessage()[1]);
        setFreq(midiFreq);
        amplitude = (int)(ampBase
            * pitchScale
            * velocityMultiplier(msg.getMessage()[2])
            * levelScale);

        linearInputFreq = (float)(midiFreq * linearInputRatio);

        if (VERBOSE) {
            out.print("WaveGen: "+ MlzMidi.toString(msg)+
                " << ["+ showBytes(msg)+"]");
            out.println("; amplitude: "+amplitude);
        }
    }

    /**
     * <p>
     *     Note that at this point the phase clock does not yet exist.
     *     Not until we get a note-ON.
     * </p>
     * @param config - the configuration map from YAML
     * @param components - a map of all the components
     *                   in this voice, by name.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        //  signed = ring mod  unsigned = op-amp
        Object objInp = config.get("signed");
        if (objInp != null) signed = (boolean)objInp;

        Float fltInp;
        fltInp = getFloat(config.get("detune"),
            "'detune' must be a number. can be floating.");
        if (fltInp != null) detune = (float)fltInp;

        Integer intInp;
        intInp = getInt(config.get("offset"),
            "'offset' must be an integer.");
        if (intInp != null) offset = intInp;

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else
                levelScale = fltInp;
        }

        //  It may be desirable to allow a base lower than zero (which
        //  would necessitate a limit when combining in velocityMultiplier()
        //  the lowest the QS puts out is about 11.
        //
        String velocityBaseErr = "'velocity-base' must be a percentage " +
            "between 0 and 100. The default is 0.";
        fltInp = getFloat(config.get("velocity-base"), velocityBaseErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >100) err.println(velocityBaseErr);
            else velocityBase = fltInp / 100.0f;
        }
        //  The QS (my test synth) never gets to a velocity of 127
        //  so the only way to achieve it is by boosting the value here.
        //  hence, the limit higher than 100%
        String velocityAmountErr = "'velocity-amount' must be a percentage " +
            "between 0 and 200. The default is 100.";
        fltInp = getFloat(config.get("velocity-amount"), velocityAmountErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >200) err.println(velocityAmountErr);
            else velocityAmount = fltInp / 100.0f;
        }

        intInp = getInt(config.get("level-override"),
            "'level-override' must be an integer.");
        if (intInp != null)  ampOverride = intInp;

        //  LOG input
        Object[] prInp = getInAmpPair(config, "input-log", "semitones");
        if (prInp != null) {
            logInputAmp = (int) prInp[0];
            logModMaxExp = ((float) prInp[1])/12f;
            modLogFrequency = true;
        }

        // LINEAR input

        prInp = getInAmpPair(config, "input-linear", "percent");
        if (prInp != null) {
            linearInputAmp = (int) prInp[0];
            linearInputRatio = (float) prInp[1] / 100f;
            if (linearInputRatio > 1 || linearInputRatio < 0) {
                err.println("linear input percent must be between 0 and 100.");
                linearInputRatio = max(0,min(linearInputRatio,1));
            }
            // it's going to vary between f+f*ratio to f-f*ratio, so
            // if ratio>1 the frequency would go negative.

            modLinFrequency = true;
        }
        Object inp;
        inp = config.get("trace-linear");  // trace linear modulation
        if (inp instanceof Boolean) {
            TRACE_LINEAR_MOD = (Boolean)inp;
        }
        else if (inp != null) {
            err.println("Envelope: trace-linear property was specified but is not boolean.");
        }

        inp = config.get("trace-relative");
        if (inp != null) {
            try {
                dt = new SemitoneDiffTracker((WaveGen) components.get(inp.toString()));
            }
            catch (ClassCastException ex) {
                err.println("trace-relative: component given is not a WaveGen.");
                err.println("  >> "+inp+" << ");
            }

        }

        //   Fixed frequency

        fltInp = getFloat(config.get("freq"),
            "freq must be a number. can be floating.");
        if (fltInp != null) freqOverride = fltInp;

        synth.getInstant().reservePhaseClocks(1);
    }

    @Override
    public void pause() {
        synth.getInstant().delPhaseClock(phaseClock);
    }

    @Override
    public void resume() {
        phaseClock = synth.getInstant().addPhaseClock();
        // If the 'freq' variable is set, it overrides the 0.
        // Because an LFO may not receive the note-ON
        setFreq(0);
    }

}
