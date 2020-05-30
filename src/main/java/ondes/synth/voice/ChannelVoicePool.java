package ondes.synth.voice;

import ondes.synth.OndesSynth;

import java.util.ArrayDeque;
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
public class ChannelVoicePool {
    String progName;
    OndesSynth synth;
    
    ArrayDeque<Voice> available=new ArrayDeque<>();
    ArrayDeque<Voice> inUse = new ArrayDeque<>();

    /**
     * Ten voices seems like a good default.
     *
     * @param progName - which program to load
     * @param synth - what synth will be playing them
     */
    public ChannelVoicePool(String progName, OndesSynth synth) {
        this(progName,synth,10);
    }

    /**
     * Ten voices seems like a good default.
     *
     * @param progName - which program to load
     * @param synth - what synth will be playing them
     */
    public ChannelVoicePool(String progName,
                     OndesSynth synth,
                     int count) {
        
        this.progName = progName;
        this.synth = synth;
        
        while (count-- > 0) {
            Voice voice = VoiceMaker.getVoice(progName,synth);
            if (voice == null) return; // getVoice should give an error.
            available.add(voice);
        }
    }

    int count=0;
    public Voice getVoice() {
        //out.println("getVoice: available="+available.size());
        if (++count == 11) {
            out.println("11th voice");
        }

        Voice voice;
        if (available.size() > 0) voice = available.pop();
        else voice = VoiceMaker.getVoice(progName,synth);

        if (voice == null) return null;

        inUse.push(voice);
        voice.resume();
        return voice;
    }

    public void releaseVoice(Voice voice) {
        //out.println("releaseVoice()");
        voice.pause();
        inUse.remove(voice);
        available.add(voice);
    }
    
    
}
