package ondes.synth.voice;

import javax.sound.midi.MidiMessage;
import java.util.*;

import ondes.App;
import ondes.synth.ComponentOwner;
import ondes.synth.component.ComponentContext;
import ondes.synth.component.MonoComponent;
import ondes.synth.component.ComponentMaker;
import ondes.synth.OndeSynth;
import ondes.synth.envelope.Envelope;
import ondes.synth.wire.*;

import static java.lang.System.err;
import static ondes.synth.component.ComponentContext.*;

/**
 *  A Voice is a set of Components
 *  configured according to the specified YAML.
 *
 *  Its state is the state of that voice being played
 *
 *
 */
@SuppressWarnings("FieldMayBeFinal,unchecked,rawtypes")
public class Voice extends ComponentOwner {
    private boolean DB=false;

    private Map voiceSpec;
    private OndeSynth synth;
    private boolean waitForEnv = false;
    private ChannelVoicePool channelVoicePool;
    private List<ChannelInput> channelInputs = new ArrayList<>();
    private Envelope defaultEnv;
    private MonoComponent mainMix;

    public void setWaitForEnv(boolean v) { waitForEnv = v; }

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
    DynamicJunction voiceMix;
    {
        Map<String,String> map=new HashMap<>();
        map.put("type","dynamic-mix");
        voiceMix = (DynamicJunction) ComponentMaker.getMonoComponent(map, synth);
        if (voiceMix == null) {
            err.println("Could not get a Junction for voice!");
        }
        else {
            voiceMix.setOwner(this);
            addMidiListeners(voiceMix, map);
        }
    }

    public void resume() {
        components.values().forEach(MonoComponent::resume);
        for (ChannelInput channelInput : channelInputs) {
            channelInput.connect();
        }
        if (voiceMix.inputSize() > 0) {
             voiceMix.setOutput(channelVoicePool.channelMix);
        }
    }
    public void pause() {
        if (voiceMix.inputSize() > 0) {
            channelVoicePool.channelMix.delInput(voiceMix.getMainOutput());
        }

        List<MonoComponent> list = new ArrayList(components.values());
        for (MonoComponent comp : list) {
            if (comp.context == VOICE) comp.pause();
        }
        for (ChannelInput channelInput : channelInputs) {
            channelInput.disconnect();
        }
    }

    public void resetWires() {
        wiredIntSupplierPool.reset();
    }

    @SuppressWarnings("rawtypes")
    public static ComponentContext context(Map m) {
        Object contextObj = m.get("context");
        if (contextObj != null &&
            contextObj.equals("channel"))
            return CHANNEL;

        Object typeObj = m.get("type");    

        if (typeObj != null && typeObj.equals("controller"))
            return CHANNEL;

        return VOICE;
    }


    /**
     * Given a "program" Map, (voiceSpec) construct each of its
     * components.  The components are named by the top level keys
     * in the YAML file.

     * If the 'context' property of a component is "channel," or if
     * the component is a controller, that component will only be created
     * once per channel, and put into "channelVoicePool."
     * Otherwise, the context is "voice," it will be put into
     * "components."

     * So below, if a "channel" level component with that name exists,
     * we use it, otherwise we create it.

     * Note that we do not connect anything here: we only add the
     * components to either "components" (for the voice level) or
     * "channelVoicePool" (for the channel level)
     *
     * @param voiceSpec - the program, from YAML
     * @param synth - the synth that will be playing it
     */
    @SuppressWarnings("rawtypes")
    void constructComponents(Map voiceSpec, OndeSynth synth) {
        for (Object key : voiceSpec.keySet()) {
            Object value=voiceSpec.get(key);
            if (!(value instanceof Map)) continue;
            Map valMap=(Map)value;

            MonoComponent C = null;
            ComponentContext context = context(valMap);
            if (context == CHANNEL) {
                C = channelVoicePool.getComponent(key.toString());
            }
            if (C == null) {
                C = ComponentMaker.getMonoComponent(valMap, synth);
            }
            if (C == null) {
                err.println("ERROR - could not load component "+key);
                err.println("  --> "+value);
                App.quitOnError();
            }
            C.setName(key.toString());

            if (context == VOICE) {
                C.context = VOICE;
                components.put(key.toString(), C);
            }
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
            }
            else {
                mainMix = voiceMix;
            }
        }
        return mainMix;
    }



    /**
     * Add a component to the "components" map with the key "main"
     *
     * If the component is at the Voice level, "main" will be the
     * local mix.
     *
     * If it's at the Channel level, "main" will be the synth's
     * main output.
     *
     */
    void setMainComponent(ComponentContext ctx) {

        switch (ctx) {
            case VOICE:
                components.put("main", getMainMix());
                break;

            case CHANNEL:
                components.put("main", synth.getMainMix());
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
        setMainComponent(VOICE); // avoid concurrent mod exception below

        for (String compKey : components.keySet()) {
            if (compKey.equals("main")) continue;
            Map compSpec=(Map)voiceSpec.get(compKey);
            MonoComponent comp=components.get(compKey);

            if (comp.context == VOICE) comp.setOwner(this);
            else if (comp.context == CHANNEL) comp.setOwner(channelVoicePool);

            setMainComponent(comp.context);

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

            //  Add the listeners listed in the midi: property of the component.
            //  The below method is found in ComponentOwner, and the list of
            //  valid text values is given in ComponentOwner.midiMessageTypes,
            //
            addMidiListeners(comp, compSpec);
        }
    }

    /**
     * The Voice constructor is only ever called from VoiceMaker.getVoice()
     *
     * First we create all the components for this voice. If this is
     * the first voice created on this channel, also create all the
     * channel level components.
     *
     * To simplify the connection process, we temporarily add the
     * channel-level components to this Voice, so we can connect to them,
     * then we remove them, as they are kept in the ChannelVoicePool
     * class rather than here.
     *
     *
     * @param voiceSpec - a Map from the YAML input
     * @param synth - the OndesSynth
     * @param channelVoicePool - the pool this voice is a member of
     */
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

    @Override
    public void processMidiMessage(MidiMessage msg) {
        super.processMidiMessage(msg);
        if (msg.getStatus()>>4 == 8 && !waitForEnv) {  // Note-OFF
            synth.noteEnded(msg);
        }
    }

    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
