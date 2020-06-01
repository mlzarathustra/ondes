package ondes.synth.wave;

import ondes.midi.MlzMidi;
import ondes.synth.component.MonoComponent;
import ondes.synth.Instant;
import ondes.midi.FreqTable;

import javax.sound.midi.MidiMessage;
import java.util.List;
import java.util.Map;

import static ondes.mlz.Util.getList;
import static ondes.midi.MlzMidi.showBytes;
import static ondes.mlz.PitchScaling.*;

import static java.lang.Math.pow;
import static java.lang.Math.min;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 */
public abstract class WaveGen extends MonoComponent {
    public static boolean VERBOSE = true;

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
     *     (invoked by the "output-amp" property)
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
     *     (invoked by the "output-amp" property)
     *     scale will be ignored.
     * </p>
     */
    float levelScale = 1;

    /**
     *  Here is where we preserve the original frequency
     *  when modulating.
     */
    protected double baseFrequency = -1;

    /**
     *  if positive, overrides MIDI
     */
    protected double freqOverride = -1;

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
     * if they specify an amplitude with the "output-amp" configuration key,
     * it overrides the calculation.
     *
     * @see #getAmp()
     * @see #configure(Map, Map)
     */
    private int ampOverride = -1;

    /**
     * <p>
     *     range: 0-1; the scaling when velocity is zero.
     *     fault: 0.
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
     *     added to the base. default: 1
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
     * @param vel MIDI velocity, 1-128
     * @return - the corresponding multiplier, from 0 to 1
     *      based on velocityBase and velocityAmount.
     */
    float velocityMultiplier(int vel) {
        return (float) min(1.0,
            velocityBase + velocityAmount * ((float)vel)/128.0 );
    }

    protected boolean signed = true;

    public int getAmp() {
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

    double freqMultiplier = 1;
    double getFreqMultiplier() {
        if (detune == 0 && offset == 0) {
            freqMultiplier=1;
            return 1;
        }
        freqMultiplier =
            pow(oneStep,offset) * pow(oneCent,detune);
        return freqMultiplier;
    }

    /**
     * <p>
     *    When modulating the frequency, we don't re-adjust the amplitude
     *    scaling as we do with a note-ON. Nor do we set touch the 'baseFrequency.'
     *    We just set the phase clock.
     * </p>
     * <p>
     *     Waves with other phase clocks will need to override (calling this
     *     one first).
     * </p>
     * <p>
     *     Not to be confused with the "rocker freq." :^)
     * </p>
     *
     * @param freq - the frequency to set the phaseClock to.
     */
    void modFreq(double freq) {
        // todo - review freq logic (for future modulation)
        phaseClock.setFrequency((float) (freq * getFreqMultiplier()));
    }

    /**
     * <p>
     * Set the phase clock frequency. If freqOverride has been set (>0)
     * we use that frequency rather than the one coming in. Also, we
     * set the amplitude scaling from this pitch (lower notes need to be
     * boosted to sound as loud) and set the `baseFrequency`
     * </p>
     * @param freq - the desired frequency
     */
    void setFreq(double freq) {
        if (freqOverride > 0) {
            freq = freqOverride;
            // noise has no frequency, but should not be scaled at infinity!
            pitchScale = (float)getScaling(pitchScaleFactor, freq);
        }
        phaseClock.setFrequency((float) (freq * getFreqMultiplier()));
        baseFrequency = freq;
    }

    @Override
    public void noteON(MidiMessage msg) {
        setFreq(FreqTable.getFreq(msg.getMessage()[1]));
        amplitude = (int)(ampBase * pitchScale * velocityMultiplier(msg.getMessage()[2]) * levelScale);
        if (VERBOSE) {
            out.print("WaveGen.noteON(): "+ MlzMidi.toString(msg)+
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

        Object compOut = config.get("out");
        if (compOut == null) {
            err.println("Missing out: key in "+this.getClass());
            err.println("WaveGen will not do much without output!");
            return;
        }
        List compOutList = getList(compOut);
        for (Object oneOut : compOutList) {
            setOutput((MonoComponent) components.get(oneOut));
        }

        //  Do we give negative output? Doesn't work so well for LFO's.
        Object blInp = config.get("signed");
        if (blInp != null) signed = (boolean)blInp;

        Float fltInp;
        fltInp = getFloat(config.get("detune"),
            "'detune' must be a number. can be floating.");
        if (fltInp != null) detune = (float)fltInp;

        Integer intInp;
        intInp = getInt(config.get("offset"),
            "'offset' must be an integer.");
        if (intInp != null) offset = intInp;

        String levelScaleErr = "'level-scale' must be between 0 and 10. (floating)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >10) err.println(levelScaleErr);
            else levelScale = fltInp;
        }

        intInp = getInt(config.get("output-amp"),
            "'output-amp' must be an integer.");
        if (intInp != null)  ampOverride = intInp;

        fltInp = getFloat(config.get("freq"),
            "freq must be a number. can be floating.");
        if (fltInp != null) freqOverride = fltInp;
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
