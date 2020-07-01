package ondes.synth.voice;

import javax.sound.midi.MidiMessage;
import java.util.*;

import ondes.midi.MlzMidi;
import ondes.synth.component.MonoComponent;
import ondes.synth.component.ComponentMaker;
import ondes.synth.OndeSynth;
import ondes.synth.envelope.Envelope;
import ondes.synth.wire.Junction;
import ondes.synth.wire.MidiNoteNum;
import ondes.synth.wire.WiredIntSupplierPool;

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

    private WiredIntSupplierPool wiredIntSupplierPool = new WiredIntSupplierPool();

    private boolean DB=false;

    public WiredIntSupplierPool getWiredIntSupplierPool() {
        return wiredIntSupplierPool;
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

    private void addEnvelopeListeners(MonoComponent comp) {
        addListener("note-on", comp);
        addListener("note-off", comp);
        addListener("sustain", comp);
    }

    private void addListener(String valStr, MonoComponent comp) {
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

    public void resume() {
        components.values().forEach(MonoComponent::resume);
        synth.getMainOutput().addInput(voiceMix.getMainOutput());
    }
    public void pause() {
        synth.getMainOutput().delInput(voiceMix.getMainOutput());
        components.values().forEach(MonoComponent::pause);
    }

    public void resetWires() {
        wiredIntSupplierPool.reset();
    }

    void constructComponents(Map voiceSpec, OndeSynth synth) {
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
    }

    Envelope getDefaultEnv() {
        Envelope env = new Envelope(synth, "organ");
        env.setVoice(this);
        env.setOutput(voiceMix);
        env.exit = true;
        addEnvelopeListeners(env);
        setWaitForEnv(true);
        return env;
    }

    /**
     * <p>
     *     Add the main mixer to the components.
     * </p>
     * <p>
     *     Currently "main" is the only global component.
     *     Other voice-level components should be filtered out
     *     in the constructComponents() loop, or they
     *     will be configured multiple times.
     *
     * </p>
     */
    void addMainOutput() {
        if (components.values().stream().noneMatch(c -> c instanceof Envelope)) {
            components.put("main", getDefaultEnv());
        }
        else {
            components.put("main", voiceMix);
        }
    }

    @SuppressWarnings("rawtypes")
    void configure() {
        for (String compKey : components.keySet()) {
            if (compKey.equals("main")) continue;

            Map compSpec=(Map)voiceSpec.get(compKey);
            MonoComponent comp=components.get(compKey);
            comp.setVoice(this);
            comp.configure(compSpec,components);

            //  (1) main output doesn't have a regular output
            //  (2) an envelope might not either, if it's only sending a level out.
            //  (3) MidiNoteNum has two level outputs, so we won't check.
            if (comp.mainOutput == null &&
                ! (
                    (comp instanceof Envelope) && ((Envelope)comp).levelOutput != null ) ||
                    (comp instanceof MidiNoteNum)
            ) {
                err.println("Component "+compKey+" mainOutput is null. ");
                // more likely than the below: forgot the out: property
                //"Did you call super.configure()?");
                //err.println("  class="+comp.getClass());
            }
            addMidiListeners(comp, compSpec);
        }
    }

    @SuppressWarnings("unchecked,rawtypes")
    Voice(Map voiceSpec, OndeSynth synth) {
        this.synth = synth;
        this.voiceSpec = voiceSpec;

        constructComponents(voiceSpec, synth);
        addMainOutput();
        configure();  // calls configure for each component.
    }

    public void processMidiMessage(MidiMessage msg) {
        Set<MonoComponent> listeners =
            midiListeners[7 & (msg.getStatus()>>4)];

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

        if (msg.getStatus()>>4 == 8 && !waitForEnv) {  // Note-OFF
            synth.noteEnded(msg);
        }
    }

    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
