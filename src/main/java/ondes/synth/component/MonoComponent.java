package ondes.synth.component;

import ondes.synth.ComponentOwner;
import ondes.synth.OndeSynth;
import ondes.synth.wire.ChannelInput;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.err;
import static java.util.stream.Collectors.toList;
import static ondes.mlz.Util.getList;
import static ondes.synth.component.ComponentContext.*;

@SuppressWarnings("rawtypes")
public abstract class MonoComponent {

//    Voice voice;
//    public Voice getVoice() { return voice; }
//    public void setVoice(Voice v) { voice = v; }

    ComponentOwner owner;
    public ComponentOwner getOwner() { return owner; }
    public void setOwner(ComponentOwner o) { owner = o; }


    public ComponentContext context = VOICE;

    /**
     * @param comp - the component whose input list
     *             will receive our MAIN output.
     * @see #setOutput(MonoComponent)
     */
    public void setOutput(MonoComponent comp) {
        setOutput(comp, getMainOutput());

    }

    /**
     * <p>
     *     We "set our output" to comp by adding it
     *     to comp's list of inputs.
     * </p>
     * <p>
     *     This happens at the configure step, only.
     *     Subsequently, GLOBAL and CHANNEL context
     *     inputs will be connected or disconnected
     *     on pause() and resume()
     * </p>
     *
     * @param comp - the component to send our output to
     * @param output - the output we are sending
     */
    public void setOutput(MonoComponent comp,
                          WiredIntSupplier output) {

        comp.owner.addInput(output);
        if (comp.context == CHANNEL) {
            owner.addChannelInput(new ChannelInput(comp, output));
        }

//                                          todo - del comments
//        if (comp.context == VOICE) {
//            comp.addInput(output);
//        }
//        else {
//            voice.addChannelInput(new ChannelInput(comp, output));
//        }
    }

    /**
     * <p>
     *     For the '.' notation. e.g. "osc1.pwm"
     *     would create an input in osc1 labeled pwm.
     *     Or more precisely, add our output to
     *     an input list with the key of "pwm"
     * </p>
     * <p>
     *     In that case, "pwm" would be the "select."
     * </p>
     *
     * @param comp - the component whose input list
     *             will receive our output.
     */
    public void setOutput(MonoComponent comp, String select) {
        setOutput(comp, select, this.getMainOutput());
    }

    public void setOutput(MonoComponent comp,
                                 String select,
                                 WiredIntSupplier output) {

        owner.addInput(output, select);
        if (comp.context == CHANNEL) {
            owner.addChannelInput(new ChannelInput(comp, output, select));
        }

        //  todo - the ChannelInput goes in the Voice in specific, so maybe
        //     generic owner isn't right here?

//                                          todo - del comments
//        if (comp.context == VOICE) {
//            comp.addInput(output, select);
//        }
//        else {
//            voice.addChannelInput(new ChannelInput(comp, output, select));
//        }
    }


    /**
     * "mainOutput" is the IntSupplier that a target component
     * can use as input.
     *
     * In some cases there may be more than one output
     * but all components will have at least one.
     *
     * Used in 2 places. Would be better if it were private.
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
    private final List<WiredIntSupplier> inputs = new ArrayList<>();

    private final HashMap<String, List<WiredIntSupplier>> namedInputs
        = new HashMap<>();

    protected synchronized int namedInputSum(String name) {
        List<WiredIntSupplier> inp = namedInputs.get(name);
        int rs=0;
        if (inp != null) {
            for (WiredIntSupplier input : inp) {
                rs += input.getAsInt();
            }
        }
        return rs;
    }

    protected int namedInputSize(String name) {
        List<WiredIntSupplier> inp = namedInputs.get(name);
        return inp == null ? 0 : inp.size();
    }

    protected List<WiredIntSupplier> getNamedInputs(String name) {
        return namedInputs.get(name);
    }

    /**
     * for most inputs
     * @return - the sum of all the inputs at this current sample
     */
    protected int inputSum() {
        int sum=0;
        int i=0;
        for (;;) {
            synchronized(this) {
                if (i >= inputs.size()) break;
                sum += inputs.get(i).getAsInt();
                ++i;
            }
        }
        return sum;
    }

    /**
     * for op amp
     * @return - the product of all the inputs at this current sample
     */
    protected synchronized double inputProd() {
        double rs = 1;
        for (WiredIntSupplier input : inputs) rs *= input.getAsInt();
        return rs;
    }



    protected OndeSynth synth;

           // /// // /// // ///     // /// // /// // /// //
       // *** // *** // *** // *** // *** //     ** /// ** //
    /// ** /// ** /// ** /// ** /// ** /// ** ///


    /**
     * Set this component's outputs.
     *
     * @param config - the configuration map from YAML
     * @param components - a map of all the components
     *                   in this voice, by name.
     */
    public void configure(Map config, Map components) {
        Object compOut = config.get("out");
        if (compOut == null && config.get("out-level") == null) {
            err.println("Missing out: key in " + this.getClass());
            err.println("A Component will not do much without output!");
            return;
        }
        if (compOut == null) return;
        List compOutList = getList(compOut);
        setOutput(compOutList, components, getMainOutput());
    }

    /**
     * <p>
     *     Two values: actually, the first and second values encountered.
     *     If only one value is present, the first is assumed to be zero.
     *     We do not sort, as that allows for inverted levels.
     * </p>
     * @param minMaxStr - a string containing two decimal numbers, separated
     *                  by commas or spaces
     * @return - the two numbers, or { 0, 100 } if there was an error.
     */
    public double[] getMinMaxLevel(String minMaxStr) {
        if (minMaxStr == null) {
            return new double[2];
        }

        double[] rs = new double[2];

        try {
            List<Double> minMax = Arrays.stream(minMaxStr.split("[\\s,]+"))
                .map( Double::parseDouble ).collect(toList());

            if (minMax.size() == 1) minMax.add(0, 0.0);

            if (minMax.size() == 2) {
                rs[0] = minMax.get(0);
                rs[1] = minMax.get(1);
            }
            else {
                err.println(
                    "ERROR! Expected 1-2 decimal numbers, got: "+minMaxStr);
                rs[0]=0; rs[1]=100;
            }
        }
        catch (NumberFormatException ex) {
            err.println(
                "ERROR! Expected decimal numbers, got: "+minMaxStr);
            rs[0]=0; rs[1]=100;
        }
        return rs;
    }

    /**
     * <p>
     *     Assigns an output to a component's input list. Handles the '.' syntax,
     *     in the first part of the 'if.' outSelect is the part after the dot.
     * </p>
     * @param compOutList - a list of strings defining the output destinations.
     *                    Can be plain component names (e.g. vcf1) or a dotted name
     *                    (e.g. osc1.pwm)
     * @param components - the components we can connect to in this voice, plus main
     * @param output - which output to connect (i.e. to add to the other component's
     *               input list)
     */
    public void setOutput(List compOutList, Map components, WiredIntSupplier output) {
        for (Object oneOut : compOutList) {
            String label = oneOut.toString();
            if (label.contains(".")) {
                String outCompStr = label.substring(0,label.indexOf("."));
                String outSelect = label.substring(label.indexOf(".")+1);
                MonoComponent outComp = (MonoComponent) components.get(outCompStr);
                if (outComp == null) {
                    err.println("ERROR! Attempting to connect to non-existent " +
                        "component '"+outCompStr+"'.'"+outSelect+"'");
                }
                else setOutput(outComp, outSelect, output);
            }
            else {
                MonoComponent comp = (MonoComponent) components.get(oneOut);
                if (comp == null) {
                    err.println("ERROR! Attempting to connect to non-existent " +
                        "component '"+oneOut+"'");
                    return;
                }
                setOutput(comp, output);
            }
        }

    }

    /**
     * disconnect phase clocks.
     */
    public abstract void pause();

    /**
     * restart phase clocks.
     */
    public abstract void resume();

    void setSynth(OndeSynth s) { synth = s; }

    /**
     *
     * <p>
     *     Provide the output of this component to another component.
     *     Creates one if needed, or returns the existing one.
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
            mainOutput = getOwner()
                .getWiredIntSupplierPool()
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
    public synchronized void addInput(WiredIntSupplier input) {
        inputs.add(input);
    }

    /**
     * <p>
     *     Add a named input. For example, the main input of a WaveGen
     *     will be to modulate frequency, so it has an input named "pwm"
     *     to modulate pulse width.
     * </p>
     * <p>
     *     By default, we just add whatever inputs we are requested to.
     *     If a component wants to get fussy about rejecting them,
     *     it can override this method.
     * </p>

     * @param input - the input to add.
     * @param select - the name of the input list to keep it in
     */
    public synchronized void addInput(WiredIntSupplier input, String select) {
        List<WiredIntSupplier> inputs =
            namedInputs.computeIfAbsent(
                select,
                k->new ArrayList<>());

        inputs.add(input);
    }

    /**
     * In most cases we don't have to worry about removing an input,
     * as the rest of the components we're connected to have the same
     * life cycle. The one exception is "main"
     */
    public synchronized void delInput(WiredIntSupplier input) {
        inputs.remove(input);
    }

    public synchronized void delInput(WiredIntSupplier input, String select) {
        List<WiredIntSupplier> inputs = namedInputs.get(select);
        inputs.remove(input);
    }

                                                                                           /*
          .. ...                                 .. **    ..        ..  .
              // /// // /// // /// //               ** *     .. ... ... ..... ..... *
                      . . ... . . .. . . .. . . .. . . .. . . .. .
                   \\\\  //// \\\\ /// \\ /// \\ /// \\ /// \\ /// \\ ///
                       .      ** --         ++ +++ +. +++   /// \\ /// \\ /// \\ //
                                        /// \\ /// \\ /// \\ //

                                   ..........



          .....       MIDI EVENTS                              ............


    */

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
    public void midiControl(MidiMessage msg) {
        //out.println("Controller: "+msg.getMessage()[1]+" length: "+msg.getLength());

        switch (msg.getMessage()[1]) {
            case 0: midiBankSelectMSB(msg.getMessage()[2]); return;
            case 1: midiModWheel(msg.getMessage()[2]); return;
            case 7: midiVolume(msg.getMessage()[2]); return;
            case 10: midiPan(msg.getMessage()[2]); return;
            case 32: midiBankSelectLSB(msg.getMessage()[2]); return;
            case 64: midiSustain(msg.getMessage()[2]); return;
        }
    }

    public void midiBankSelectMSB(int val) { }
    public void midiModWheel(int val) { }
    public void midiVolume(int val) { }
    public void midiPan(int val) { }
    public void midiBankSelectLSB(int val) { }
    public void midiSustain(int val) { }

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
