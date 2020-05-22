package ondes.synth;

import javax.sound.midi.*;
import javax.sound.sampled.Mixer;

import ondes.midi.MlzMidi;
import ondes.synth.mix.MainMix;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.System.out;

public class OndesSynth extends Thread {
    Tangle tangle = new Tangle();
    Instant instant;
    MainMix mainMix = new MainMix();

    MidiDevice midiInDev;
    Mixer outDev;
    String[] progNames;

    boolean stop;

    /**
     * The constructor only sets
     *
     * @param sampleRate - cycles per second
     * @param in         - transmits MIDI messages that the synth responds to
     *                   e.g. note-ON, pitch bend, and so on.
     * @param out        - note that "source" is from the perspective of the
     *                   mixer. From our perspective, it is a target.
     * @param pn         - a list of strings identifying the programs
     *                   These may be file names or resource names (?)
     *                   TODO - clarify
     */
    public OndesSynth(
        int sampleRate,
        MidiDevice in,
        Mixer out,
        String[] pn
    ) {
        midiInDev = in;
        outDev = out;
        progNames = pn;

        //  TODO - where should the sample rate come from?
        instant = new Instant(sampleRate);

    }

    void routeMidiMessage(MidiMessage msg, long ts) {
        out.println(ts+" : "+MlzMidi.toString(msg));
    }

    void listen() {
        Receiver recv = new Receiver() {
            public void close() {};
            public void send(MidiMessage msg, long ts) {
                routeMidiMessage(msg,ts);
            }
        };

        Transmitter trans;
        try {
            trans = midiInDev.getTransmitter();

            out.println("Opened device: " + trans);
            out.println("Listening for MIDI messages.");

            trans.setReceiver(recv);
            midiInDev.open();

        }
        catch (Exception ex) {
            out.println("attempting to open midi device "+midiInDev);
            out.println(ex);
            System.exit(-1);
        }
    }

    public void run() {
        listen();

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
