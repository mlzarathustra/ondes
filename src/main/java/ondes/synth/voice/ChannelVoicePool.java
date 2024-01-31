package ondes.synth.voice;

import ondes.synth.ComponentOwner;
import ondes.synth.OndeSynth;
import ondes.synth.component.ComponentMaker;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.ChannelInput;
import ondes.synth.wire.DynamicJunction;
import ondes.synth.wire.WiredIntSupplierPool;

import javax.sound.midi.MidiMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.System.err;
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
public class ChannelVoicePool extends ComponentOwner {
    String progName;
    OndeSynth synth;
    int chan;

    //  Voice-level outputs (to 'main') plug in to here, to be regulated
    //  by the volume pedal (controller 7)
    DynamicJunction channelMix;
    {
        Map<String,String> compSpec=new HashMap<>();
        compSpec.put("type","dynamic-mix");
        compSpec.put("midi", "volume");
        channelMix = (DynamicJunction) ComponentMaker.getMonoComponent(compSpec, synth);
        if (channelMix == null) {
            err.println("Could not get a Junction for channel!");
        }
        else {
            channelMix.setOwner(this);
            addMidiListeners(channelMix, compSpec);
            addComponent("main", channelMix);
        }
    }

    /**
     * In this class, components inherited from ComponentOwner are all
     * Channel-level components, shared by all Voices on this channel
     */
//    private final Map<String, MonoComponent> components=new HashMap<>();

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
        wiredIntSupplierPool.reset();
    }

                                                                                     /*
                  .      .       .                 .      .
           .. ... .. ... .. ...      .. ... .. ... .. ..      ... .. ..    // ///
         ** . *** . ** . ***    **   . . . . . . . .    . . .  . .   . . ** *** **
      //  ... // ...                                                   ///  //
                                 ComponentOwner functions
    */


    /**
     * A no-op here. Only applies to Voice.
     * @param exit - whether to wait or not for an envelope to finish
     */
    @Override
    public void setWaitForEnv(boolean exit) { }


    /**
     * A no-op here.... only Voice-level components need to worry about
     * connecting and disconnecting.
     * @param ci
     */
    @Override
    public void addChannelInput(ChannelInput ci) { }


                                                                                    /*
                  .      .       .                 .      .     //
           .. ... .. ... .. ...      .. ... .. ... .. ..      ... .. ..    // ///
         ** . *** . ** . ***    **   . . . . . . . .    . . .  . .   . . ** *** **
      //  ... // ...                    *                              ///  //

    */



    ChannelState channelState;

    public void updateState(MidiMessage msg) {
        channelState.update(msg);
    }
    
    private final ConcurrentLinkedDeque<Voice> available =
        new ConcurrentLinkedDeque<>();

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
        channelMix.setOutput(synth.getMainMix());


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
        if (!available.isEmpty()) voice = available.peek();
        else voice = VoiceMaker.getVoice(progName,synth, this);

        return voice;
    }

    public Voice getVoice() {
        Voice voice;

        if (!available.isEmpty()) voice = available.pop();
        else {
            out.println("ChannelVoicePool: creating new voice: "+progName);
            voice = VoiceMaker.getVoice(progName,synth, this);
        }

        if (voice == null) return null;

        // propagate channel state
        channelState.getMessages()
            .forEach( voice::processMidiMessage );

        voice.resume();
        return voice;
    }

    public void releaseVoice(Voice voice) {
        voice.pause();
        available.add(voice);
    }
    
    
}
