package ondes.synth.voice;

import ondes.synth.OndesSynth;

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
    OndesSynth synth;
    
    ArrayDeque<Voice> available=new ArrayDeque<>();
    ArrayDeque<Voice> inUse = new ArrayDeque<>();

    ChannelVoicePool(String progName,
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
    
    public Voice getVoice() {
        Voice voice;
        if (available.size() > 0) voice = available.pop();
        else voice = VoiceMaker.getVoice(progName,synth);

        if (voice == null) return null;

        inUse.push(voice);
        voice.resume();
        return voice;
    }

    public void releaseVoice(Voice voice) {
        voice.pause();
        inUse.remove(voice);
        available.add(voice);
    }
    
    
}
