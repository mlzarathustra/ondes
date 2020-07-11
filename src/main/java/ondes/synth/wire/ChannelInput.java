package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

/**
 * <p>
 *     A channel-level component and a voice-level output.
 * </p>
 * <p>
 *     These need to be disconnected when the voice pauses
 *     and reconnected when it resumes.
 * </p>
 *
 */
public class ChannelInput {
    public MonoComponent component;
    public WiredIntSupplier input;
    public String name;

    ChannelInput(MonoComponent c, WiredIntSupplier i) {
        this(c, i, null);
    }
    ChannelInput(MonoComponent c, WiredIntSupplier i, String n) {
        component = c; input = i; name = n;
    }

    public void connect() {
        if (name == null) component.addInput(input);
        else component.addInput(input, name);
    }

    public void disconnect() {
        if (name == null) component.delInput(input);
        else component.delInput(input, name);
    }



}
