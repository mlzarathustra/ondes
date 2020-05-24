package ondes.synth;

import javax.sound.midi.*;
import javax.sound.sampled.Mixer;

import ondes.midi.MlzMidi;
import ondes.synth.mix.MainMix;
import ondes.synth.voice.Voice;
import ondes.synth.voice.VoiceMaker;
import ondes.synth.wire.Tangle;

import static java.lang.System.out;

public class OndesSynth extends Thread implements EndListener {

    //  16 MIDI channels x 128 Notes
    private Voice [][]voices = new Voice[16][128];

    private Tangle tangle = new Tangle();
    private MainMix mainMix = new MainMix();

    private Instant instant;

    private MidiDevice midiInDev;
    private Mixer outDev;
    private String[] progNames;

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

    /**
     * we're only supporting one voice on each note
     * for a given channel, so if they somehow hit it again
     * before the "off" we re-trigger.
     */
    void noteON(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];

        if (voices[chan][note] != null) {
            voices[chan][note].noteON(msg);
            return;
        }

        Voice v = VoiceMaker.getVoice(progNames[chan], this);
        voices[chan][note]=v;
        v.setEndListener(this);
        v.noteON(msg);
    }

    void noteOFF(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];

        if (voices[chan][note] == null) return;
        voices[chan][note].noteOFF(msg);

    }


    void routeMidiMessage(MidiMessage msg, long ts) {
        out.println(ts+" : "+MlzMidi.toString(msg));

        String status="unknown";
        int s=msg.getStatus()>>4;
        switch (s) {
            case 0x8: status = "Note OFF";
                noteOFF(msg);
                break;
            case 0x9: status = "Note ON";
                noteON(msg);
                break;

            //  TODO - implement at least some of these
            //   (e.g. pitch bend + vol, mod, and sustain controllers)
            //
            case 0xa: status = "Aftertouch"; break;
            case 0xb: status = "Controller"; break;
            case 0xc: status = "Program Change"; break;
            case 0xd: status = "Channel Pressure"; break;
            case 0xe: status = "Pitch Bend"; break;
            case 0xf: status = "System"; break;
        }
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


    @Override
    public void noteEnded(int chan, int note) {
        //  Assume that the voice has released all of its components
        voices[chan][note] = null;
    }

    public Tangle getTangle() { return tangle; }
    public MainMix getMainMix() { return mainMix; }
}
