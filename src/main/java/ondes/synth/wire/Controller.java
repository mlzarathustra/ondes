package ondes.synth.wire;

import ondes.synth.component.MonoComponent;
import static ondes.synth.component.ConfigHelper.*;


import javax.sound.midi.MidiMessage;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * <p>
 *     Listens to a particular controller, and
 *     sends the scaled value as output.
 * </p>
 * <p>
 *     Has no inputs (aside from the controller)
 * </p>
 *
 */
public class Controller extends MonoComponent {

    int controlNumber;
    int outLevel;
    double minLevel, maxLevel;

    /**
     * <p>
     * Sets our outputLevel according to the
     * level in the MIDI message (0-127)
     *
     * @param lvl - from MIDI message
     *            </p>
     */
    void setLevel(int lvl) {
        outLevel = (int)(
            minLevel + ( ((double) lvl) / 127.0 ) * (maxLevel - minLevel)
        );
    }

    /**
     * <p>
     *     It only reaches here when a note is down, or when a Note-ON is
     *     received and it updates the voice to the channel state.
     * </p>
     *
     * @param msg - a Controller message for this channel
     */
    public void midiControl(MidiMessage msg) {
        //out.println("controller #"+controlNumber+": "+msg.getMessage()[2]);
        if (msg.getMessage()[1] == controlNumber) {
            setLevel(msg.getMessage()[2]);
        }
    }

    @Override
    public int currentValue() { return outLevel; }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        String inpStr = config.get("amp").toString();
        if (inpStr == null) {
            err.println("Controller: amp was not given, so no control will happen.");
            return;
        }
        double[] mm = getMinMaxLevel(inpStr);
        minLevel = mm[0];
        maxLevel = mm[1];

        Integer intInp = getInt(config.get("number"),
            "Controller - no valid number given, so no control will happen.");

        if (intInp != null) controlNumber = intInp;

        Map <String,String>spoof = new HashMap<>();
        spoof.put("midi","control");
        getOwner().addMidiListeners(this, spoof);
    }
}

















