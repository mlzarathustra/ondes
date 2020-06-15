package ondes.synth.voice;

import ondes.synth.OndeSynth;

import java.util.ArrayDeque;

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
    
    ArrayDeque<Voice> available=new ArrayDeque<>();
    ArrayDeque<Voice> inUse = new ArrayDeque<>();

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
        
        while (count-- > 0) {
            Voice voice = VoiceMaker.getVoice(progName,synth);
            if (voice == null) return; // getVoice should give an error.
            voice.midiChan = chan;
            available.add(voice);
        }
    }

    /**
     * retrieve one without starting it (so we can get the name)
     * @return a voice from this channel.
     */
    public Voice peekVoice() {
        Voice voice;
        if (available.size() > 0) voice = available.peek();
        else voice = VoiceMaker.getVoice(progName,synth);

        return voice;
    }

    int count=0;
    public Voice getVoice() {
        Voice voice;
        if (available.size() > 0) voice = available.pop();
        else voice = VoiceMaker.getVoice(progName,synth);

        if (voice == null) return null;

        inUse.push(voice);

        // TODO - need to inherit channel state

        voice.resume();
        return voice;
    }

    public void releaseVoice(Voice voice) {
        voice.pause();
        inUse.remove(voice);
        available.add(voice);
    }
    
    
}
