package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

import javax.sound.midi.MidiMessage;
import java.util.Map;

public class MidiNoteNum extends MonoComponent {

    WiredIntSupplier linearOutput, logOutput;

    public WiredIntSupplier getLinearOutput() {
        if (linearOutput == null) {
            linearOutput = getVoice()
                .getWiredIntSupplierPool()
                .getWiredIntSupplier(this::currentLinear);
        }
        return linearOutput;
    }

    public WiredIntSupplier getLogOutput() {
        if (logOutput == null) {
            logOutput = getVoice()
                .getWiredIntSupplierPool()
                .getWiredIntSupplier(this::currentLinear);
        }
        return logOutput;
    }


    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        // no need to call super.configure() - not setting main out


    }




        // TODO - scaling
    //     see monoComponent.getMinMaxLevel

    int currentLinear() {
        return midiNoteNum;
    }


    int midiNoteNum = 0;

    @Override
    public void noteON(MidiMessage msg) {
        midiNoteNum = msg.getMessage()[1];
    }


    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public int currentValue() {
        return 0;
    }
}
