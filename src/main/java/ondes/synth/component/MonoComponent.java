package ondes.synth.component;

import ondes.synth.OndesSynth;
import ondes.synth.voice.Voice;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public abstract class MonoComponent {

    Voice voice;
    public Voice getVoice() { return voice; }
    public void setVoice(Voice v) { voice = v; }

    /**
     * "mainOutput" is the IntSupplier that a target component
     * can use as input.
     *
     * In some cases there may be more than one output
     * but all components will have at least one.
     *
     */
    public WiredIntSupplier mainOutput = null;

    /**
     * Multiple inputs are common - e.g. ENV and LFO
     * The below implies they will be summed together.
     *
     * Components with alternate inputs will have to define
     * their own. For example, a delay could intake a signal
     * and be modulated by a separate LFO.
     *
     * The output value at each sample is cached by WiredIntSupplier,
     * to avoid infinite looping (e.g. if a component uses its
     * output as input, common in FM synthesis). So the "visited"
     * flag needs to be reset for every sample.
     *
     * Getting the output value is basically a depth-first walk.
     */
    public List<WiredIntSupplier> inputs = new ArrayList<>();
    public List<MonoComponent> outputs = new ArrayList<>(); // for release

    protected OndesSynth synth;

    public abstract void configure(Map config, Map components);

    /**
     * disconnect phase clocks.
     */
    public abstract void pause();

    /**
     * restart phase clocks.
     */
    public abstract void resume();

    /**
     * Detach this component:
     * disconnect from main mix and stop phase clocks.
     */
    public abstract void release();

    void setSynth(OndesSynth s) { synth = s; }

    /**
     *
     * <p>
     *     Provide the output of this component to another component.
     *     Creates one if needed, or returns the existing one.
     *
     * </p>
     * <p>
     *     The subclass generally MUST define "mainOutput," as it's the
     *     whole point of various components that the functionality of
     *     the output depends on the component.
     *
     * </p>
     * <p>
     *     The exception is MainMix, which outputs by a whole other means
     *     to the audio system.
     * </p>
     *
     * @return - the supplier of output data
     */
    public WiredIntSupplier getMainOutput() {
        if (mainOutput == null) {
            mainOutput = getVoice()
                .getWiredIntSupplierMaker()
                .getWiredIntSupplier(this::currentValue);
        }
        return mainOutput;
    }

    /**
     * The component should override this function with one that
     * returns the current output value
     *
     * @return - the current output level of this component
     */
    public abstract int currentValue();

    /**
     * By default, we just add whatever inputs we are requested to.
     * If a component wants to get fussy about rejecting them, it
     * can override this method.
     *
     * @param input - the input to add.
     */
    public void addInput(WiredIntSupplier input) {
        inputs.add(input);
    }

    /**
     * In most cases we don't have to worry about removing an input,
     * as the rest of the components we're connected to have the same
     * life cycle. The one exception is "main"
     */
    public void delInput(WiredIntSupplier input) {
        inputs.remove(input);
    }

    /**
     * If this component should receive MIDI ON messages,
     * override this function, and add the property "midi: note-on"
     * in the YAML file.
     *
     * @param msg - a Note-ON message for this channel
     */
    public void noteON(MidiMessage msg) { }

    /**
     * If this component should receive MIDI OFF messages,
     * override this function, and add the property "midi: note-off"
     * in the YAML file.
     *
     * @param msg - a Note-OFF message for this channel
     */
    public void noteOFF(MidiMessage msg) { }

    /**
     * If this component should receive MIDI ON messages,
     * override this function, and add the property "midi: after"
     * in the YAML file.
     *
     * @param msg - an Aftertouch message for this channel
     */
    public void midiAfter(MidiMessage msg) { }

    /**
     * <p>
     * If this component should receive MIDI Control messages,
     * override this function, and add the property "midi: control"
     * in the YAML file.
     * </p>
     *
     * <p>
     * Controls include
     * </p>
     * <ul>
     *     <li> 0 - bank select MSB </li>
     *     <li> 1 - mod wheel </li>
     *     <li> 7 - volume </li>
     *     <li> 10 - pan </li>
     *     <li> 32 - bank select LSB </li>
     *     <li> 64 - sustain pedal </li>
     *
     * </ul>
     * @param msg - a Controller message for this channel
     */
    public void midiControl(MidiMessage msg) { }

    /**
     * If this component should receive MIDI Program Change messages,
     * override this function, and add the property "midi: program"
     * in the YAML file.
     *
     * @param msg - a Program Change message for this channel
     */
    public void midiProgram(MidiMessage msg) { }

    /**
     * If this component should receive MIDI Channel Pressure messages,
     * override this function, and add the property "midi: pressure"
     * in the YAML file.
     *
     * @param msg - a Channel Pressure message for this channel
     */
    public void midiPressure(MidiMessage msg) { }

    /**
     * If this component should receive MIDI Pitch Bend messages,
     * override this function, and add the property "midi: bend"
     * in the YAML file.
     *
     * @param msg - a Pitch Bend message for this channel
     */
    public void midiBend(MidiMessage msg) { }

    /**
     * <p>
     * If this component should receive MIDI System messages,
     * override this function, and add the property "midi: system"
     * in the YAML file.
     * </p>
     *
     * In this context, may be useful for clocks.
     *
     * @param msg - a System message for this channel
     */
    public void midiSystem(MidiMessage msg) { }


}
