package ondes.synth.voice;

import ondes.midi.MlzMidi;

import javax.sound.midi.MidiMessage;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.out;

/**
 * Track channel-wide state so that voices being 
 * brought online can inherit it.
 */
public class ChannelState {
    
    HashMap<Integer,Integer> controllers = new HashMap<>();
    HashMap<Integer,Integer> afterKeys = new HashMap<>();
    int channelPressure;
    int program;
    int pitchBend;

    public ChannelState() { reset(); }

    void reset() {
        controllers.clear();
        afterKeys.clear();
        channelPressure = -1;
        program = -1;
        pitchBend = -1;
    }

    public void update(MidiMessage msg) {
        //out.println("ChannelState.update: "+ MlzMidi.toString(msg));

        // we only get messages for the whole channel,
        // not specific notes.
        byte[] d = msg.getMessage();

        switch(msg.getStatus() >> 4) {
            case 0xa:
                afterKeys.put((int)d[1],(int)d[2]);
                break;
            case 0xb:
                controllers.put((int)d[1], (int)d[2]);
                break;

            case 0xc:
                program = d[1];
                break;
            case 0xd:
                channelPressure = d[1];
                break;
            case 0xe:
                pitchBend = ( d[1] + (d[2] << 7) ) - 0x40;
                break;
                
            case 0xf: break; // not tracking system events. 
        }
        
    }
    
    
    
    
    
}
