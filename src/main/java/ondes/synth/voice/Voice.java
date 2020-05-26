package ondes.synth.voice;

import ondes.synth.component.MonoComponent;
import ondes.synth.component.ComponentMaker;
import ondes.synth.EndListener;
import ondes.synth.OndesSynth;
import ondes.synth.wire.WiredIntSupplierMaker;

import javax.sound.midi.MidiMessage;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.System.err;

@SuppressWarnings("FieldMayBeFinal,unchecked")
public class Voice {
    private HashMap<String, MonoComponent> components=new HashMap<>();
    private EndListener endListener;
    private OndesSynth synth;

    private WiredIntSupplierMaker wiredIntSupplierMaker = new WiredIntSupplierMaker();

    public WiredIntSupplierMaker getWiredIntSupplierMaker() {
        return wiredIntSupplierMaker;
    }

    private ArrayList<MonoComponent>[] midiListeners= new ArrayList[8];
    {
        for (int i=0; i<8; ++i) {
            midiListeners[i] = new ArrayList<>();
        }
    }

    /**
     * For YAML parsing - package atoms into a list, or
     * return a list if the object already is one.
     *
     * To smooth out the differences between
     * <pre>
     *      midi: on
     * and
     *      midi:
     *        - on
     *        - off
     * and
     *      midi: on, off
     * </pre>
     *
     * @param obj - a list or something to put in a list
     * @return - some kind of list
     */
    public static List<?> getList(Object obj) {
        if (obj instanceof List) return (List<?>)obj;
        List<Object> rs = new ArrayList<>();

        if (obj instanceof String) {
            String[] parts = obj.toString().split("[, ]+");
            rs.addAll(Arrays.asList(parts));
            return rs;
        }

        rs.add(obj);
        return rs;
    }

    public static final String[] midiMessageTypes =
        "off on after control program pressure bend system".split(" ");

    @SuppressWarnings("rawtypes")
    private void addMidiListeners(MonoComponent comp, Map compSpec) {
        Object obj = compSpec.get("midi");
        if (obj == null) return;
        for (Object val : getList(obj)) {
            String valStr= val.toString();

            for (int i=0; i<midiMessageTypes.length; ++i) {
                if (valStr.equals(midiMessageTypes[i])) {
                    midiListeners[i].add(comp);
                    break;
                }
            }
        }
    }

    public void resetWires() {
        wiredIntSupplierMaker.reset();
    }

    public void setEndListener(EndListener el) { endListener=el; }

    @SuppressWarnings("unchecked,rawtypes")
    Voice(Map voiceSpec, OndesSynth synth) {
        this.synth = synth;
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
        components.put("main", synth.getMonoMainMix());

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

        // TODO - if an envelope handles the notification,
        //        we won't do it here.
        //
        if (msg.getStatus()>>4 == 8) {  // Note-OFF
            endListener.noteEnded(msg);
        }

    }

    public String toString() {
        return "Voice { components: "+
            String.join(", ", components.keySet())+" }";
    }

}
