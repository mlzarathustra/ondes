package ondes.synth.voice;

import javax.sound.midi.MidiMessage;
import java.util.*;

import ondes.midi.MlzMidi;
import ondes.synth.component.MonoComponent;
import ondes.synth.component.ComponentMaker;
import ondes.synth.OndeSynth;
import ondes.synth.wire.Junction;
import ondes.synth.wire.WiredIntSupplierMaker;

import static java.lang.System.err;
import static ondes.mlz.Util.getList;

@SuppressWarnings("FieldMayBeFinal,unchecked")
public class Voice {
    private Map voiceSpec;
    private OndeSynth synth;
    private HashMap<String, MonoComponent> components=new HashMap<>();
    private boolean waitForEnv = false;

    public void setWaitForEnv(boolean v) { waitForEnv = v; }

    public int midiNote, midiChan;

    private WiredIntSupplierMaker wiredIntSupplierMaker = new WiredIntSupplierMaker();

    private boolean DB=false;

    public WiredIntSupplierMaker getWiredIntSupplierMaker() {
        return wiredIntSupplierMaker;
    }

    public Map getVoiceSpec() { return voiceSpec; }

    /**
     * Components connect to our junction rather than the main mix
     * so that we can disconnect them from main when deactivating
     * the voice, and then reattach when reactivating.
     */
    Junction voiceMix;
    {
        Map<String,String> map=new HashMap<>();
        map.put("type","mix");
        voiceMix = (Junction) ComponentMaker.getMonoComponent(map, synth);
        if (voiceMix == null) {
            err.println("Could not get a Junction for voice!");
        }
        else {
            voiceMix.setVoice(this);
        }
    }

    /**
     * Eight listeners, one for each message type (MIDI status >> 4)
     * @see #processMidiMessage(MidiMessage)
     */
    private ArrayList<MonoComponent>[] midiListeners= new ArrayList[8];
    {
        for (int i=0; i<8; ++i) {
            midiListeners[i] = new ArrayList<>();
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

    @SuppressWarnings("rawtypes")
    private void addMidiListeners(MonoComponent comp, Map compSpec) {
        Object obj = compSpec.get("midi");
        if (obj == null) return;
        for (Object val : getList(obj)) {
            String valStr= val.toString();

            for (int i=0; i<midiMessageTypes.length; ++i) {
                if (Arrays.asList(midiMessageTypes[i]).contains(valStr)) {
                    midiListeners[i].add(comp);
                    break;
                }
            }
        }
    }

    public void resume() {
        components.values().forEach(MonoComponent::resume);
        synth.getMainOutput().addInput(voiceMix.getMainOutput());
    }
    public void pause() {
        components.values().forEach(MonoComponent::pause);
        synth.getMainOutput().delInput(voiceMix.getMainOutput());
    }

    public void resetWires() {
        wiredIntSupplierMaker.reset();
    }

    @SuppressWarnings("unchecked,rawtypes")
    Voice(Map voiceSpec, OndeSynth synth) {
        this.synth = synth;
        this.voiceSpec = voiceSpec;

        // step 1 : construct components
        for (Object key : voiceSpec.keySet()) {
            Object value=voiceSpec.get(key);
            if (!(value instanceof Map)) continue;
            Map valMap=(Map)voiceSpec.get(key);
            MonoComponent c= ComponentMaker.getMonoComponent(valMap, synth);
            if (c == null) {
                err.println("ERROR - could not load component "+key);
                err.println("  --> "+voiceSpec.get(key));
                System.exit(-1);
            }
            components.put(key.toString(), c);
        }

        // step 1a: Add the main mixer to the components.
        //    Currently it is the only global component.
        //    Note that any others like this must be filtered out
        //    in the following loop.
        components.put("main", voiceMix);

        // step 2 : configure
        //          (including: connect to other components)
        for (String compKey : components.keySet()) {
            if (compKey.equals("main")) continue;

            Map compSpec=(Map)voiceSpec.get(compKey);
            MonoComponent comp=components.get(compKey);
            comp.setVoice(this);
            comp.configure(compSpec,components);

            addMidiListeners(comp, compSpec);
        }
    }

    public void processMidiMessage(MidiMessage msg) {
        ArrayList<MonoComponent> listeners =
            midiListeners[7 & (msg.getStatus()>>4)];

        if (DB) {
            err.println("Voice.processMidiMessage "+ MlzMidi.toString(msg));
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

        if (msg.getStatus()>>4 == 8 && !waitForEnv) {  // Note-OFF
            synth.noteEnded(msg);
        }
    }

    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
