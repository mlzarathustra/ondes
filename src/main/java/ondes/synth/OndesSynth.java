package ondes.synth;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;

import ondes.synth.mix.MainMix;

import static java.lang.System.out;

public class OndesSynth extends Thread {
    Tangle tangle = new Tangle();
    Instant instant;
    MainMix mainMix=new MainMix();

    MidiDevice inDev;
    Mixer outDev;
    String[] progNames;

    boolean stop;

    /**
     * The constructor only sets
     *
     * @param sampleRate - cycles per second
     * @param in - transmits MIDI messages that the synth responds to
     *           e.g. note-ON, pitch bend, and so on.
     * @param out - note that "source" is from the perspective of the
     *            mixer. From our perspective, it is a target.
     * @param pn - a list of strings identifying the programs
     *            These may be file names or resource names (?)
     *                 TODO - clarify
     */
    public OndesSynth(
        int sampleRate,
        MidiDevice in,
        Mixer out,
        String[] pn
    ) {
        inDev = in;
        outDev = out;
        progNames = pn;

        //  TODO - where should the sample rate come from?
        instant = new Instant(sampleRate);

    }

    public void run() {
        for (;;) {
            instant.next();
            tangle.update();
            mainMix.update();
            try {

                //  TODO --  stub; implement  the above

                sleep(1000);
                out.print("breathe... ");
            }
            catch (Exception ignore) {}

            if (stop) return;
        }

    }




}
