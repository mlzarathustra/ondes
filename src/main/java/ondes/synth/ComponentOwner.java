package ondes.synth;

import ondes.midi.MlzMidi;
import ondes.synth.component.MonoComponent;
import ondes.synth.envelope.Envelope;
import ondes.synth.wire.ChannelInput;
import ondes.synth.wire.WiredIntSupplier;
import ondes.synth.wire.WiredIntSupplierPool;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.err;
import static ondes.mlz.Util.getList;

@SuppressWarnings("rawtypes")
public abstract class ComponentOwner {
    boolean DB = false;

    protected HashMap<String, MonoComponent> components=new HashMap<>();

    public abstract WiredIntSupplierPool getWiredIntSupplierPool();
    public abstract void setWaitForEnv(boolean exit);
    public abstract void addChannelInput(ChannelInput ci);


    /**
     * Eight listeners, one for each message type (MIDI status >> 4)
     * @see #processMidiMessage(MidiMessage)
     */
    private Set<MonoComponent>[] midiListeners= new HashSet[8];
    {
        for (int i=0; i<8; ++i) {
            midiListeners[i] = new HashSet<>();
        }
    }

    /**
     * Midi Message types by index (starting with 0x8, note-OFF)
     * For controllers, it's all or nothing. To simplify the code.
     */
    public static final String[][] midiMessageTypes = {
        {"note-off"},
        {"note-on"},
        {"after"},
        {"control", "bank-msb", "mod-wheel", "volume", "pan",
            "bank-lsb", "sustain"},
        {"program"},
        {"pressure"},
        {"bend"},
        {"system"}
    };

    protected void addEnvelopeListeners(MonoComponent comp) {
        addListener("note-on", comp);
        addListener("note-off", comp);
        addListener("sustain", comp);
    }

    private  void addListener(String valStr, MonoComponent comp) {
        for (int i=0; i<midiMessageTypes.length; ++i) {
            if (Arrays.asList(midiMessageTypes[i]).contains(valStr)) {
                midiListeners[i].add(comp);
                break;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void addMidiListeners(MonoComponent comp, Map compSpec) {
        if (comp instanceof Envelope) addEnvelopeListeners(comp);
        Object obj = compSpec.get("midi");
        if (obj == null) return;
        for (Object val : getList(obj)) {
            String valStr= val.toString();
            addListener(valStr, comp);
        }
    }

    protected Set<MonoComponent> getListeners(MidiMessage msg) {
        return midiListeners[7 & (msg.getStatus()>>4)];
    }

    public void processMidiMessage(MidiMessage msg) {
        Set<MonoComponent> listeners = getListeners(msg);

        if (DB) {
            err.println("Voice.processMidiMessage "+ MlzMidi.toString(msg));
            err.flush();
//            err.println("Voice.processMidiMessage: listeners = " +
//                Arrays.toString(midiListeners) + "; ");
        }

        for (MonoComponent comp : listeners) {
            switch (msg.getStatus() >> 4) {
                case 0x8: comp.noteOFF(msg); break;
                case 0x9: comp.noteON(msg); break;
                case 0xa: comp.midiAfter(msg); break;
                case 0xb: comp.midiControl(msg); break;
                case 0xc: comp.midiProgram(msg); break;
                case 0xd: comp.midiPressure(msg); break;
                case 0xe: comp.midiBend(msg); break;
                case 0xf: comp.midiSystem(msg); break;
            }
        }
    }

}
