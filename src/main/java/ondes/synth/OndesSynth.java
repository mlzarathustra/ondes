package ondes.synth;

import javax.sound.midi.*;
import javax.sound.sampled.Mixer;

import ondes.midi.FreqTable;
import ondes.midi.MlzMidi;
import ondes.synth.mix.MonoMainMix;
import ondes.synth.voice.Voice;
import ondes.synth.voice.VoiceMaker;

import java.util.HashSet;
import java.util.function.Consumer;

import static java.lang.System.out;

@SuppressWarnings("FieldMayBeFinal")
public class OndesSynth extends Thread implements EndListener {

    boolean DB = false;

    class VoiceTracker {
        class VoiceSet extends HashSet<Voice> { }

        //  16 MIDI channels x 128 Notes
        private final Voice [][]voices = new Voice[16][128];
        // track playing voices per channel
        VoiceSet[] playing=new VoiceSet[16];
        {
            for (int i=0; i<16; ++i) playing[i] = new VoiceSet();
        }

        Voice getVoice(int chan, int note) {
            return voices[chan][note];
        }

        void forEach(Consumer<Voice> fn) {
            for (int chan=0; chan<16; ++chan) {
                for (Voice v : playing[chan]) fn.accept(v);
            }
        }

        void addVoice(Voice v, int chan, int note) {
            voices[chan][note]=v;
            playing[chan].add(v);
        }

        void delVoice(int chan, int note) {
            if (voices[chan][note] == null) return;
            playing[chan].remove(voices[chan][note]);
            voices[chan][note] = null;
        }

        /**
         * @param chan - origin 0
         * @return - the list of voices currently playing on this channel
         */
        VoiceSet getChannelPlaying(int chan) {
            return playing[chan];
        }
    }

    VoiceTracker voiceTracker = new VoiceTracker();

    private MonoMainMix monoMainMix;

    private final Instant instant;

    private final MidiDevice midiInDev;
    private Mixer outDev;
    private String[] progNames;

    final Object lock = new Object() {
        public String toString() { return "Ondes Lock"; }
    };
    boolean stop;

    /**
     * The constructor only sets
     *
     * @param sampleRate - cycles per second
     * @param in         - transmits MIDI messages that the synth responds to
     *                   e.g. note-ON, pitch bend, and so on.
     * @param od        - note that "source" is from the perspective of the
     *                   mixer. From our perspective, it is a target.
     * @param pn         - a list of 16 strings identifying the programs
     *                   for each channel. A loose "contains" match compares
     *                   the input with the 'name' property of the program.
     */
    public OndesSynth(
        int sampleRate,
        MidiDevice in,
        Mixer od,
        String[] pn
    ) {
        midiInDev = in;
        outDev = od;
        progNames = pn;

        //  TODO - allow the user to specify the sample rate,
        //            rather than only accepting the default.

        monoMainMix = new MonoMainMix(outDev);
        instant = new Instant(monoMainMix.getSampleRate());

    }

    /**
     * we're only supporting one voice on each note
     * for a given channel, so if they somehow hit it again
     * before the "off" we re-trigger.
     */
    void noteON(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];

        Voice playing = voiceTracker.getVoice(chan,note);

        if (playing != null) {
            playing.processMidiMessage(msg);
            return;
        }

        Voice v = VoiceMaker.getVoice(progNames[chan], this);
        if (v == null) return; // getVoiceMap() displays the warning

        voiceTracker.addVoice(v,chan,note);
        v.setEndListener(this);
        v.processMidiMessage(msg);
    }

    void noteOFF(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];

        Voice playing = voiceTracker.getVoice(chan,note);
        if (playing == null) return;
        playing.processMidiMessage(msg);

    }

    /**
     * Send this message to all voices currently playing
     * on the given channel.
     */
    void sendChannelMessage(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        for (Voice v : voiceTracker.getChannelPlaying(chan)) {
            v.processMidiMessage(msg);
        }
    }

    void routeMidiMessage(MidiMessage msg, long ts) {
        if (DB) out.println(ts+" : "+MlzMidi.toString(msg));

        //  Note-ON messes with the phase clocks list
        //  so don't do it while incrementing them
        synchronized(lock) {
            String status = "unknown";
            int s = msg.getStatus() >> 4;
            switch (s) {
                case 0x8:
                    status = "Note OFF";
                    noteOFF(msg);
                    break;
                case 0x9:
                    status = "Note ON";
                    noteON(msg);
                    break;

                default:
                    sendChannelMessage(msg);
            }
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
        FreqTable.getFreq(0); // preload the class

        for (;;) {

            //  Avoid colliding with constructor triggered by Note-ON
            //
            synchronized (lock){
                voiceTracker.forEach(Voice::resetWires);
                instant.next();
                monoMainMix.update();
            }

            try {

                //  TODO --  stub; implement  the above
                //  TODO - think about race conditions
                //   and need for synchronization with the above

                //sleep(1000);
                //out.print("breathe... ");
            }
            catch (Exception ignore) {}

            if (stop) return;
        }

    }


    @Override
    public void noteEnded(int chan, int note) {
        //  Assume that the voice has released all of its components
        voiceTracker.delVoice(chan,note);
    }

    public MonoMainMix getMonoMainMix() { return monoMainMix; }
    public Instant getInstant() { return instant; }
}
