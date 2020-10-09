package ondes.synth.voice;

import javax.sound.midi.MidiMessage;
import java.util.*;

import ondes.App;
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




    @SuppressWarnings("rawtypes")
    void constructComponents(Map voiceSpec, OndeSynth synth) {
        for (Object key : voiceSpec.keySet()) {
            Object value=voiceSpec.get(key);
            if (!(value instanceof Map)) continue;
            Map valMap=(Map)voiceSpec.get(key);

            MonoComponent C = null;
            ComponentContext context = context(valMap);
            if (context == CHANNEL) {
                C = channelVoicePool.getComponent(key.toString());
            }
            if (C == null) {
                C= ComponentMaker.getMonoComponent(valMap, synth);
            }
            if (C == null) {
                err.println("ERROR - could not load component "+key);
                err.println("  --> "+voiceSpec.get(key));
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
        addMainOutput(VOICE); // avoid concurrent mod exception below

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
