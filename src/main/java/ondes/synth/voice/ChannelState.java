package ondes.synth.voice;

import ondes.midi.MlzMidi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Track channel-wide state so that voices being 
 * brought online can inherit it.
 */
public class ChannelState {

    int channel;
    
    private final ConcurrentHashMap<Integer,Integer> controllers =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,Integer> afterKeys =
        new ConcurrentHashMap<>();
    int channelPressure;
    int program;
    int pitchBend;

    public ChannelState(int channel) {
        this.channel = channel;
        reset();
    }

    void reset() {
        controllers.clear();
        afterKeys.clear();
        channelPressure = -1;
        program = -1;
        pitchBend = Integer.MIN_VALUE;
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

    public List<MidiMessage> getMessages() {
        ArrayList<MidiMessage> rs = new ArrayList<>();
        try {
            for (int k : afterKeys.keySet()) {
                rs.add(new ShortMessage(0xa + channel, k, afterKeys.get(k)));
            }
            for (int k : controllers.keySet()) {
                rs.add(new ShortMessage(0xb0 + channel, k, controllers.get(k)));
            }
            if (program >= 0)
                rs.add(new ShortMessage(0xc0 + channel, program, 0));
            if (channelPressure >= 0)
                rs.add(new ShortMessage(0xd0 + channel, channelPressure, 0));
            if (pitchBend != Integer.MIN_VALUE)
                rs.add(new ShortMessage(0xe0 + channel, pitchBend & 0x7f, pitchBend >> 7));
        }
        catch (Exception ex) { err.println("ChannelState.getMessages: "+ex); }
        return rs;
    }
    
    
    
    
    
}
