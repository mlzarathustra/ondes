package ondes.synth;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.ChannelInput;
import ondes.synth.wire.WiredIntSupplier;
import ondes.synth.wire.WiredIntSupplierPool;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ComponentOwner {

    WiredIntSupplierPool getWiredIntSupplierPool();
    void setWaitForEnv(boolean exit);
    void addInput(WiredIntSupplier output);
    void addInput(WiredIntSupplier output, String select);
    void addChannelInput(ChannelInput ci);
    void addMidiListeners(MonoComponent comp, Map compSpec);
}
