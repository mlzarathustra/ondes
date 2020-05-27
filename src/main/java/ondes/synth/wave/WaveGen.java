package ondes.synth.wave;

import ondes.synth.component.MonoComponent;
import ondes.synth.Instant;
import ondes.midi.FreqTable;

import javax.sound.midi.MidiMessage;
import java.util.List;
import java.util.Map;

import static ondes.mlz.Util.getList;
import static ondes.midi.MlzMidi.showBytes;

import static java.lang.Math.pow;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 *
 *
 */
public abstract class WaveGen extends MonoComponent {
    public static boolean VERBOSE = false;

    protected Instant.PhaseClock phaseClock;
    static final double oneStep = pow(2, 1.0/12);
    static final double oneCent = pow(2, 1.0/1200);

    /**
     * reset the note to zero
     */
    void reset() {
        //  phaseClock.align() or something like that
    }

    private double freq = 440;
    private int amp = 2048;  // assume 16-bits (signed) for now.

    public int getAmp() {
        return (int)(scale * amp);
    }

    float detune = 0;  // detune in cents
    int offset = 0;    // interval offset in minor seconds
    float scale = 1;

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
        this.freq = freq;
        phaseClock.setFrequency((float) (freq * getFreqMultiplier()));
    }

    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        phaseClock = synth.getInstant().addPhaseClock();
        // Note ON will set frequency

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
                    scale = 1;
                }
            }
            catch (Exception ex) {
                err.println("'scale' must be a floating number.");
            }
        }
    }

    public void release() {
        synth.getInstant().delPhaseClock(phaseClock);
        outputs.forEach( c -> c.delInput(this.getMainOutput()));
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
