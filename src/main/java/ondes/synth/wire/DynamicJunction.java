package ondes.synth.wire;

/**
 *
 *  A Junction that responds to
 *  MIDI controller 7 (volume pedal)
 *
 */
public class DynamicJunction extends Junction {
    @Override
    public void midiVolume(int val) {
        setLevelScale( ((float)val) / 128.0f );
        //System.out.println("ChannelJunction.midiVolume("+val+")");
    }
}
