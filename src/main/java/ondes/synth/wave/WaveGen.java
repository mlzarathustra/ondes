package ondes.synth.wave;

import ondes.synth.component.MonoComponent;
import ondes.synth.Instant;
import ondes.midi.FreqTable;

import javax.sound.midi.MidiMessage;
import java.util.List;
import java.util.Map;

import static ondes.mlz.Util.getList;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * The parent abstract class that defines the interface of the wave generators.
 * All wave generators must extend this class.
 *
 *
 */
public abstract class WaveGen extends MonoComponent {
    protected Instant.PhaseClock phaseClock;

    /**
     * reset the note to zero
     */
    void reset() {
        //  phaseClock.align() or something like that
    }

    double freq = 440;
    int amp = 32767;  // assume 16-bits (signed) for now.

    void setFreq(double freq) { this.freq = freq; }

    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        phaseClock = synth.getInstant().addPhaseClock();
        // Note ON will set frequency

        out.println("WaveGen.configure: "+config);
        //out.println("out type: "+config.get("out").getClass());

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
    }

    public void release() {
        synth.getInstant().delPhaseClock(phaseClock);
        outputs.forEach( c -> c.delInput(this.getMainOutput()));
    }

    void setOutput(MonoComponent comp) {
        out.println("setOutput("+comp+")");
        comp.addInput(this.getMainOutput());
        outputs.add(comp); // so we can remove it later
    }

    // there is no Arrays.stream(byte[])
    //
    void showBytes(MidiMessage msg) {
        for (byte b : msg.getMessage()) {
            out.print(Integer.toHexString(b&0x00ff)+" ");
        }
        out.println();
    }

    @Override
    public void noteON(MidiMessage msg) {
        out.print("WaveGen.noteON(): ");
        showBytes(msg);
        double freq = FreqTable.getFreq(msg.getMessage()[1]);
        phaseClock.setFrequency((float)freq);
    }
}
