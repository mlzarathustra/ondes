package ondes.synth;

import javax.sound.midi.*;
import javax.sound.sampled.Mixer;

import ondes.App;
import ondes.midi.FreqTable;
import ondes.midi.MlzMidi;
import ondes.mlz.SineLookup;
import ondes.synth.component.ComponentMaker;
import ondes.synth.component.MonoComponent;
import ondes.synth.envelope.Limiter;
import ondes.synth.voice.ChannelVoicePool;
import ondes.synth.voice.Voice;
import ondes.synth.wire.WiredIntSupplierMaker;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.System.err;
import static java.lang.System.out;
import static ondes.mlz.YamlLoader.*;

@SuppressWarnings("FieldMayBeFinal")
public class OndesSynth extends Thread implements EndListener {

    boolean DB = false;

    /**
     * Tracks which voices are playing on each channel.
     *
     */
    static class VoiceTracker {
        static class VoiceSet extends HashSet<Voice> { }

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

    /**
     * Each MIDI channel has its own voice pool.
     * Rather than creating the voice each time, we retrieve one
     * that has already been created.
     *
     */
    private ChannelVoicePool[] channelVoicePool = new ChannelVoicePool[16];
    private void fillChannelVoicePool(String[] progNames) {
        for (int chan=0; chan<16; ++chan) {
            if (progNames[chan] != null) {
                channelVoicePool[chan]=
                    new ChannelVoicePool(progNames[chan],this,10);

                out.println(String.format("Channel %4s: ", "["+(1+chan)+"]")+
                    channelVoicePool[chan].peekVoice()
                        .getVoiceSpec().get("name"));
            }
        }
    }

//    private MonoMainMix monoMainMix;
    private MonoMainMix monoMainMix;
    //

    private Limiter mainLimiter;

    private final Instant instant;
    private int sampleRate;

    private final MidiDevice midiInDev;
    private final MidiListenerThread midiListener;

    /**
     * <p>
     *     The Main Limiter is a bit unusual, being a component that
     *     doesn't belong to a voice. The other is monoMainMix. But
     *     unlike monoMainMix, the limiter has an output latch that
     *     needs to be reset on every sample.
     * </p>
     * <p>
     *     That means we throw away the WiredIntSupplierMaker right away
     *     as we're not tracking WiredIntSuppliers in a Voice. Here, there's
     *     just one which gets reset manually in the resetWires() function.
     * </p>
     * <p>
     * @see #resetWires
     * </p>
     *
     * @return - the Main Limiter. Creates and configures one if it doesn't
     * already exist.
     */
    @SuppressWarnings("rawtypes")
    Limiter getMainLimiter() {
        if (mainLimiter == null) {
            Map config = loadResource("config/main-limiter-config.yaml");
            mainLimiter = (Limiter) ComponentMaker.getMonoComponent(config, this);
            if (mainLimiter == null) {
                err.println("Cannot open Main Limiter!");
                System.exit(-1);
            }
            mainLimiter.mainOutput = new WiredIntSupplierMaker()
                .getWiredIntSupplier(mainLimiter::currentValue);
            mainLimiter.configure(config, null);
        }
        return mainLimiter;
    }

    /**
     * For main loop concurrency
     */
    final Object lock = new Object() {
        public String toString() { return "Ondes Lock"; }
    };

    /**
     * To signal the main loop to stop
     */
    boolean stop;

    boolean USE_LIMITER=true;

    /**
     * The constructor only sets
     *
     * @param sampleRate - cycles per second. Currently ignored here
     *                   see TODO below
     * @param midiInDev         - transmits MIDI messages that the synth responds to
     *                   e.g. note-ON, pitch bend, and so on.
     * @param outDev     - note that "source" is from the perspective of the
     *                   mixer. From our perspective, it is a target.
     * @param progNames         - a list of 16 strings identifying the programs
     *                   for each channel. A loose "contains" match compares
     *                   the input with the 'name' property of the program.
     */
    public OndesSynth(
        int         sampleRate,
        MidiDevice  midiInDev,
        Mixer       outDev,
        String[]    progNames,
        int         bufSize
    ) {
        super("OndesSynth - main thread");
        this.midiInDev = midiInDev;
        midiListener = new MidiListenerThread(this);

        //  TODO - allow the user to specify the sample rate,
        //            rather than only accepting the default.
//        monoMainMix = new MonoMainMix(outDev, bufSize);
        monoMainMix = new MonoMainMix(outDev, bufSize);
        this.sampleRate = monoMainMix.getSampleRate();
        mainLimiter  = getMainLimiter();
        instant = new Instant(sampleRate);
        fillChannelVoicePool(progNames);

        //
        //
        if (USE_LIMITER) {
            monoMainMix.addInput(mainLimiter.getMainOutput());
        }
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

        Voice v = channelVoicePool[chan].getVoice();
        if (v == null) return; // getVoiceMap() displays the warning

        voiceTracker.addVoice(v,chan,note);
        v.setEndListener(this);
        //v.resume(); // getVoice above calls resume.
        v.processMidiMessage(msg);
    }

    void noteOFF(MidiMessage msg) {
        if (App.holdValue()) return;
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];

        Voice playing = voiceTracker.getVoice(chan,note);
        if (playing == null) return;
        playing.processMidiMessage(msg);
    }

    @Override
    public void noteEnded(int chan, int note) {
        Voice voice = voiceTracker.getVoice(chan,note);
        if (voice != null) {
            channelVoicePool[chan].releaseVoice(voice);
        }
        voiceTracker.delVoice(chan,note);
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
        if (DB) {
            out.println(" OndesSynth.routeMidiMessage : " +
                "[" + ts + "] " + MlzMidi.toString(msg));
        }

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
                //routeMidiMessage(msg,ts);
                out.println("OndesSynth.listen("+MlzMidi.toString(msg)+")");
                midiListener.routeMidiMessage(msg);
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

        midiListener.start();
    }

    /**
     * <p>
     *     The "wires" are output objects of type WiredIntProvider
     *     (they're also input objects - the outputting component
     *     provides one for the inputting component to call.
     * </p>
     * <p>
     *     It is perfectly valid for a component to use its output as
     *     input, for FM. So to avoid infinite looping, it latches the
     *     current value on the first visit of this sample, then it must
     *     reset before the next sample. Below is where that happens.
     * </p>
     */
    void resetWires() {
        //  resetWires sets the "visited" flag "false" for each output
        //  in each Voice.
        voiceTracker.forEach(Voice::resetWires);

        //  only one output "wire" is not in the voices: the Main Limiter.
        //  (the Main Mix output is not a WiredIntSupplier, but instead
        //  writes to a buffer eventually written to the audio system.
        getMainLimiter().getMainOutput().setVisited(false);

    }

    //  The core is synchronized to avoid colliding with
    //  a constructor triggered by Note-ON
    //
    public void run() {

        // preload class data
        double
            u1 = FreqTable.getFreq(0),
            u2 =SineLookup.sineLookup(0);

        listen();

        // TODO - does this even help fix the gaps? 
        // attempting to give the audio thread greater priority...
        // "almost" still gets gaps in the sound.
        //
        Thread[] ta=new Thread[activeCount()+10];
        Thread.enumerate(ta);
        for (Thread t : ta) {
            if (t != null) {
                if (t.toString().contains("Dispatcher")) t.setPriority(10);
                else if (t.toString().contains("Ondes")) t.setPriority(1);
                out.println(t);
            }
        }
        //
        //

        for (;;) {
            synchronized (lock){
                resetWires();
                instant.next();
                monoMainMix.update();
            }

            if (stop) return;
        }
    }

    public void logFlush() {
        if (monoMainMix != null) monoMainMix.logFlush();
    }


    public MonoComponent getMainOutput() {
        if (USE_LIMITER) {
            return getMainLimiter();
        }
        return monoMainMix;

    }
    public Instant getInstant() { return instant; }

    public int getSampleRate() { return sampleRate; }


}
