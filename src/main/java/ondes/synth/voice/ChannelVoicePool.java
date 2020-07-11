package ondes.synth.voice;

import ondes.synth.OndeSynth;
import ondes.synth.component.MonoComponent;

import javax.sound.midi.MidiMessage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

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
public class ChannelVoicePool {
    String progName;
    OndeSynth synth;
    int chan;
    private Map<String, MonoComponent> components=new HashMap<>();

    public void addComponent(String key, MonoComponent comp) {
        components.put(key, comp);
    }
    public Map<String, MonoComponent> getComponents() { return components; }
    public MonoComponent getComponent(String key) { return components.get(key); }


    ChannelState channelState;

    public void updateState(MidiMessage msg) {
        channelState.update(msg);
    }
    
    private final ArrayDeque<Voice> available=new ArrayDeque<>();
//    private final ArrayDeque<Voice> inUse = new ArrayDeque<>();

    /**
     * Ten voices seems like a good default.
     *
     * @param progName - which program to load
     * @param synth - what synth will be playing them
     */
    public ChannelVoicePool(String progName, OndeSynth synth, int chan) {
        this(progName,synth,chan,10);
    }

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
