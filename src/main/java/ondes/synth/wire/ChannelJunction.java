package ondes.synth.wire;

public class ChannelJunction extends Junction {


    @Override
    public void midiVolume(int val) {
        setLevelScale( ((float)val) / 128.0f );
        System.out.println("ChannelJunction.midiVolume("+val+")");
    }

    //  TODO - ChannelVoicePool should have one of these
    //          and all the outputs should go through it.

}
