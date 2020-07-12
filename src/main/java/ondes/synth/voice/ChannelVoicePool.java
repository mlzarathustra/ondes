package ondes.synth.voice;

import ondes.synth.ComponentOwner;
import ondes.synth.OndeSynth;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.ChannelInput;
import ondes.synth.wire.WiredIntSupplier;
import ondes.synth.wire.WiredIntSupplierPool;

import javax.sound.midi.MidiMessage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

/**
 * <p>
 *     The Sirens were voices on an island.
 *     These are voices in a pool.
 * </p>
 * <p>
 *     We only support one program at a time.
 * </p>
 *
 */
public class ChannelVoicePool implements ComponentOwner {
    String progName;
    OndeSynth synth;
    int chan;
    private final Map<String, MonoComponent> components=new HashMap<>();

    public void addComponent(String key, MonoComponent comp) {
        components.put(key, comp);
    }
    public Map<String, MonoComponent> getComponents() { return components; }
    public MonoComponent getComponent(String key) { return components.get(key); }

    private final WiredIntSupplierPool wiredIntSupplierPool = new WiredIntSupplierPool();

    public WiredIntSupplierPool getWiredIntSupplierPool() {
        return wiredIntSupplierPool;
    }

    public void resetWires() {
        out.println("wiredIntSupplierPool: "+wiredIntSupplierPool);
        wiredIntSupplierPool.reset();
    }

                                                                                     /*
                  .      .       .                 .      .
           .. ... .. ... .. ...      .. ... .. ... .. ..      ... .. ..    // ///
         ** . *** . ** . ***    **   . . . . . . . .    . . .  . .   . . ** *** **
      //  ... // ...                                                   ///  //
                                 ComponentOwner functions
    */




    @Override
    public void setWaitForEnv(boolean exit) {

    }

    @Override
    public void addInput(WiredIntSupplier output) {

    }

    @Override
    public void addInput(WiredIntSupplier output, String select) {

    }

    @Override
    public void addChannelInput(ChannelInput ci) {

    }

    @Override
    public void addMidiListeners(MonoComponent comp, Map compSpec) {

    }

                                                                                     /*
                  .      .       .                 .      .     //
           .. ... .. ... .. ...      .. ... .. ... .. ..      ... .. ..    // ///
         ** . *** . ** . ***    **   . . . . . . . .    . . .  . .   . . ** *** **
      //  ... // ...                    *                              ///  //

    */



    ChannelState channelState;

    public void updateState(MidiMessage msg) {
        channelState.update(msg);

        //  todo - loop through listeners (components) and send msg
        //         to each that should get it.
        //             Look at how Voice handles midiListeners.

    }
    
    private final ArrayDeque<Voice> available=new ArrayDeque<>();
//    private final ArrayDeque<Voice> inUse = new ArrayDeque<>();

    /**
     * Ten voices seems like a good default.
     *
     * @param progName - which program to load
     * @param synth - what synth will be playing them
     */
    public ChannelVoicePool(String progName,
                     OndeSynth synth,
                     int chan,
                     int count) {
        
        this.progName = progName;
        this.synth = synth;
        this.chan = chan;
        channelState = new ChannelState(chan);
        
        while (count-- > 0) {
            Voice voice = VoiceMaker.getVoice(progName,synth, this);
            if (voice == null) return; // getVoice should give an error.
            voice.midiChan = chan;
            available.add(voice);
        }
        components.values().forEach( MonoComponent::resume );
    }

    /**
     * retrieve one without starting it (so we can get the name)
     * @return a voice from this channel.
     */
    public Voice peekVoice() {
        Voice voice;
        if (available.size() > 0) voice = available.peek();
        else voice = VoiceMaker.getVoice(progName,synth, this);

        return voice;
    }

    public Voice getVoice() {
        Voice voice;

        if (available.size() > 0) {
            synchronized (this) {
                voice = available.pop();
            }
        }
        else voice = VoiceMaker.getVoice(progName,synth, this);

        if (voice == null) return null;

        // propagate channel state
        channelState.getMessages()
            .forEach( voice::processMidiMessage );

        voice.resume();
        return voice;
    }

    public void releaseVoice(Voice voice) {
        voice.pause();

        synchronized (this) {
            available.add(voice);
        }
    }
    
    
}
