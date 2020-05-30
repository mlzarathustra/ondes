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

    //private double freq;
    private int amp = 1024;  // assume 16-bits (signed) for now.
    // it adds up fast for composite waves.

    public int getAmp() {
        return (int)(scale * amp);
    }

    float detune = 0;  // detune in cents
    int offset = 0;    // interval offset in minor seconds
    float scale = 1;

    double scaleFactor = 10; // see ondes.mlz.PitchScaling

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
        phaseClock.setFrequency((float) (freq * getFreqMultiplier()));
        scale = (float)getScaling(scaleFactor, freq);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        //out.println("WaveGen.configure: "+config);

        Object compOut = config.get("out");
        if (compOut == null) {
            err.println("Missing out: key in "+this.getClass());
            err.println("Voice will not sound without output!");
            return;
        }
        List compOutList = getList(compOut);
        for (Object oneOut : compOutList) {
            setOutput((MonoComponent) components.get(oneOut));
        }

        Object detune = config.get("detune");
        if (detune != null) {
            try { this.detune = Float.parseFloat(detune.toString()); }
            catch (Exception ex) {
                err.println("'detune' must be a number. can be floating.");
            }
        }
        Object offset = config.get("offset");
        if (offset != null) {
            try { this.offset = Integer.parseInt(offset.toString()); }
            catch (Exception ex) {
                err.println("'offset' must be an integer.");
            }
        }
        Object scale = config.get("scale");
        if (scale != null) {
            try {
                this.scale = Float.parseFloat(scale.toString());
                if (this.scale < 0 || this.scale > 1) {
                    err.println("'scale' must (floating) be between 0 and 1.");
                    this.scale = 1;
                }
            }
            catch (Exception ex) {
                err.println("'scale' must be a floating number.");
            }
        }
    }

    @Override
    public void pause() {
        synth.getInstant().delPhaseClock(phaseClock);
    }

    @Override
    public void resume() {
        phaseClock = synth.getInstant().addPhaseClock(); // note-ON sets freq
    }

    void setOutput(MonoComponent comp) {
        comp.addInput(this.getMainOutput());
        outputs.add(comp); // so we can remove it later
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
