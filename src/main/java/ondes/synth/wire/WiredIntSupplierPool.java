package ondes.synth.wire;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntSupplier;

/**
 * The list of connections within a single voice
 * We need to keep the list because they all need to
 * have the 'visited' flag reset for every sample.
 */
public class WiredIntSupplierPool {

    private final Queue<WiredIntSupplier> wires = new ConcurrentLinkedQueue<>();

    public WiredIntSupplier getWiredIntSupplier(IntSupplier iu) {
        WiredIntSupplier wire = new WiredIntSupplier() {
            public int updateInputs() { return iu.getAsInt(); }
        };
        wires.add(wire);
        return wire;
    }

    public void reset() {
        for (WiredIntSupplier wire : wires) wire.setVisited(false);
    }

    public String toString() {
        return "WiredIntSupplierPool { wires.size(): "+wires.size()+" } ";
    }

}
