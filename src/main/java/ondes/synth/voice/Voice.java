package ondes.synth.voice;

import javax.sound.midi.MidiMessage;
import java.util.*;

import ondes.midi.MlzMidi;
import ondes.synth.ComponentOwner;
import ondes.synth.component.ComponentContext;
import ondes.synth.component.MonoComponent;
import ondes.synth.component.ComponentMaker;
import ondes.synth.OndeSynth;
import ondes.synth.envelope.Envelope;
import ondes.synth.wire.*;

import static java.lang.System.err;
import static java.lang.System.out;
import static ondes.mlz.Util.getList;
import static ondes.synth.component.ComponentContext.*;

@SuppressWarnings("FieldMayBeFinal,unchecked,rawtypes")
public class Voice implements ComponentOwner {
    private boolean DB=false;

    private Map voiceSpec;
    private OndeSynth synth;
    private HashMap<String, MonoComponent> components=new HashMap<>();
    private boolean waitForEnv = false;
    private ChannelVoicePool channelVoicePool;
    private List<ChannelInput> channelInputs = new ArrayList<>();
    private Envelope defaultEnv;
    private MonoComponent mainMix;

    public void setWaitForEnv(boolean v) { waitForEnv = v; }

    @Override
    public void addInput(WiredIntSupplier output) {
        // todo implement
    }

    @Override
    public void addInput(WiredIntSupplier output, String select) {
        // todo implement
    }

    public int midiNote, midiChan;

    private WiredIntSupplierPool wiredIntSupplierPool = new WiredIntSupplierPool();

    public WiredIntSupplierPool getWiredIntSupplierPool() {
        return wiredIntSupplierPool;
    }

    public Map getVoiceSpec() { return voiceSpec; }

    public void addChannelInput(ChannelInput ci) {
        channelInputs.add(ci);
    }

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
            voiceMix.setOwner(this);
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
        for (int i=0; i<channelInputs.size(); ++i) {
            channelInputs.get(i).connect();
        }
        synth.getMainOutput().addInput(voiceMix.getMainOutput());
    }
    public void pause() {
        synth.getMainOutput().delInput(voiceMix.getMainOutput());

        List<MonoComponent> list = new ArrayList(components.values());
        for (int i=0; i<list.size(); ++i) {
            MonoComponent comp = list.get(i);
            if (comp.context == VOICE) comp.pause();
        }
        for (int i=0; i<channelInputs.size(); ++i) {
            channelInputs.get(i).disconnect();
        }
    }

    public void resetWires() {
        out.println("wiredIntSupplierPool: "+wiredIntSupplierPool);
        wiredIntSupplierPool.reset();
    }

    @SuppressWarnings("rawtypes")
    void constructComponents(Map voiceSpec, OndeSynth synth) {
        for (Object key : voiceSpec.keySet()) {
            Object value=voiceSpec.get(key);
            if (!(value instanceof Map)) continue;
            Map valMap=(Map)voiceSpec.get(key);

            MonoComponent C = null;
            ComponentContext context = VOICE;
            Object contextObj = valMap.get("context");
            if (contextObj != null && contextObj.equals("channel")) {
                context = CHANNEL;
                C = channelVoicePool.getComponent(key.toString());
            }
            if (C == null) {
                C= ComponentMaker.getMonoComponent(valMap, synth);
            }
            if (C == null) {
                err.println("ERROR - could not load component "+key);
                err.println("  --> "+voiceSpec.get(key));
                System.exit(-1);
            }

            if (context == VOICE) components.put(key.toString(), C);
            else {
                C.context = CHANNEL;
                channelVoicePool.addComponent(key.toString(), C);
            }
        }
    }

    Envelope getDefaultEnv() {
        if (defaultEnv == null) {
            defaultEnv = new Envelope(synth, "organ");
            defaultEnv.setOwner(this);
            defaultEnv.setOutput(voiceMix);
            defaultEnv.exit = true;
            addEnvelopeListeners(defaultEnv);
            setWaitForEnv(true);
        }
        return defaultEnv;
    }

    MonoComponent getMainMix() {
        if (mainMix == null) {
            if (components.values()
                .stream()
                .noneMatch(c -> c instanceof Envelope)) {
                    mainMix = getDefaultEnv();
            } else {
                    mainMix = voiceMix;
            }
        }
        return mainMix;
    }



    /**
     * <p>
     *     Add the main mixer to the components.
     *     This is a VOICE-level output.
     * </p>
     * <p>
     *     Currently "main" is the only global component.
     *     Other voice-level components should be filtered out
     *     in the constructComponents() loop, or they
     *     will be configured multiple times.
     *
     * </p>
     */
    void addMainOutput(ComponentContext ctx) {

        switch (ctx) {
            case VOICE:
                components.put("main", getMainMix());
                break;

            case CHANNEL:
                components.put("main", synth.getMainOutput());
                break;
        }
    }

    void addChannelComponents() {
        for (String compKey : channelVoicePool.getComponents().keySet()) {
            components.put(compKey, channelVoicePool.getComponents().get(compKey));
        }
    }

    void removeChannelComponents() {
        for (String compKey : channelVoicePool.getComponents().keySet()) {
            components.remove(compKey);
        }
    }

    @SuppressWarnings("rawtypes")
    void configure() {
        for (String compKey : components.keySet()) {
            if (compKey.equals("main")) continue;
            Map compSpec=(Map)voiceSpec.get(compKey);
            MonoComponent comp=components.get(compKey);

            if (comp.context == VOICE) comp.setOwner(this);
            else if (comp.context == CHANNEL) comp.setOwner(channelVoicePool);

            addMainOutput(comp.context);

            comp.configure(compSpec,components);

            //  (1) main output doesn't have a regular output
            //  (2) an envelope might not either, if it's only sending a level out.
            //  (3) MidiNoteNum has two level outputs, so we won't check.
            if (comp.mainOutput == null &&
                ! (
                    (comp instanceof Envelope) && ((Envelope)comp).levelOutput != null ||
                    (comp instanceof MidiNoteNum)
                )
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
    Voice(Map voiceSpec, OndeSynth synth, ChannelVoicePool channelVoicePool) {
        this.synth = synth;
        this.voiceSpec = voiceSpec;
        this.channelVoicePool = channelVoicePool;

        constructComponents(voiceSpec, synth);

        addChannelComponents();

        configure();  // calls configure for each component.

        removeChannelComponents();
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
