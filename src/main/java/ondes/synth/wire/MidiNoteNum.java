package ondes.synth.wire;

import ondes.midi.FreqTable;
import ondes.synth.component.MonoComponent;

import javax.sound.midi.MidiMessage;
import java.util.Map;

import static ondes.mlz.Util.getList;
import static ondes.synth.component.ConfigHelper.getOutAmpPair;

public class MidiNoteNum extends MonoComponent {

    WiredIntSupplier linearOutput, logOutput;

    public WiredIntSupplier getLinearOutput() {
        if (linearOutput == null) {
            linearOutput = getOwner()
                .getWiredIntSupplierPool()
                .getWiredIntSupplier(this::currentLinear);
        }
        return linearOutput;
    }

    public WiredIntSupplier getLogOutput() {
        if (logOutput == null) {
            logOutput = getOwner()
                .getWiredIntSupplierPool()
                .getWiredIntSupplier(this::currentLog);
        }
        return logOutput;
    }

    private double minFreq, maxFreq;

    private float linearOut = 1, logOut = 1;
    boolean linearActive = false, logActive = false;


    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        // no need to call super.configure() - not setting main out

        //  Linear is the frequency.
        Object[] outPair = getOutAmpPair(config, "linear-out");
        if (outPair != null) {
            linearOut = (Integer) outPair[0];
            setOutput( getList(outPair[1]),  components, getLinearOutput());
            minFreq = FreqTable.getFreq(0);
            maxFreq = FreqTable.getFreq(127);
            linearActive = true;
        }

        //  Log is the note number.
        outPair = getOutAmpPair(config, "log-out");
        if (outPair != null) {
            logOut = (Integer) outPair[0];
            setOutput( getList(outPair[1]), components, getLogOutput());
            logActive = true;
        }

    }

    int scaledLinear, scaledLog;

    int currentLinear() {
        return scaledLinear;
    }

    int currentLog() {
        return scaledLog;
    }

    void scaleLinear() {
        double freq = (int) FreqTable.getFreq(midiNoteNum);
        scaledLinear = (int) ( freq / (maxFreq - minFreq) * linearOut );
    }

    void scaleLog() {
        scaledLog =(int) ( (((float)midiNoteNum) / 128 ) * logOut );
    }

    int midiNoteNum = 0;

    @Override
    public void noteON(MidiMessage msg) {
        midiNoteNum = msg.getMessage()[1];
        if (linearActive) scaleLinear();
        if (logActive) scaleLog();
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
