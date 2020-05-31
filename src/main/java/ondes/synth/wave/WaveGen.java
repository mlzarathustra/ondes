package ondes.synth.wave;

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

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 */
public abstract class WaveGen extends MonoComponent {
    public static boolean VERBOSE = false;

    protected Instant.PhaseClock phaseClock;
    static final double oneStep = pow(2, 1.0/12);
    static final double oneCent = pow(2, 1.0/1200);

    /**
     * if they specify an amplitude with the "output-amp" configuration key,
     * it overrides the calculation.
     *
     * @see #getAmp()
     * @see #configure(Map, Map)
    */
    private int ampOverride = -1;

    // should be private.
    protected double freq = -1; // if positive, overrides MIDI
    private int amp = 1024;  // assume 16-bits (signed) for now.
    // it adds up fast for composite waves.

    protected boolean signed = true;

    public int getAmp() {
        if (ampOverride >= 0) return ampOverride;
        return (int)(scale * amp);
    }

    float detune = 0;  // detune in cents
    int offset = 0;    // interval offset in minor seconds
    float scale = 1;

    double pitchScaleFactor = 10; // see ondes.mlz.PitchScaling

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

    void setFreq(double freq) {
        if (this.freq > 0) freq = this.freq;
        phaseClock.setFrequency((float) (freq * getFreqMultiplier()));
        scale = (float)getScaling(pitchScaleFactor, freq);
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

        String scaleErr = "'scale' must (floating) be between 0 and 1.";
        fltInp = getFloat(config.get("scale"), scaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >1) err.println(scaleErr);
            else scale = fltInp;
        }

        intInp = getInt(config.get("output-amp"),
            "'output-amp' must be an integer.");
        if (intInp != null)  ampOverride = intInp;

        fltInp = getFloat(config.get("freq"),
            "freq must be a number. can be floating.");
        if (fltInp != null) freq = fltInp;
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

    @Override
    public void noteON(MidiMessage msg) {
        if (VERBOSE) {
            out.print("WaveGen.noteON(): ");
            showBytes(msg);
        }
        setFreq(FreqTable.getFreq(msg.getMessage()[1]));
    }
}
