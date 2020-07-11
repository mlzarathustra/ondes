package ondes.synth.wire;

import java.util.*;
import java.util.function.IntSupplier;

/**
 * The list of connections within a single voice
 * We need to keep the list because they all need to
 * have the 'visited' flag reset for every sample.
 */
public class WiredIntSupplierPool {

    private final List<WiredIntSupplier> wires = new ArrayList<>();

    public synchronized WiredIntSupplier getWiredIntSupplier(IntSupplier iu) {
        WiredIntSupplier wire = new WiredIntSupplier() {
            public int updateInputs() { return iu.getAsInt(); }
        };
        wires.add(wire);
        return wire;
    }

    public synchronized void reset() {
        for (WiredIntSupplier wire : wires) {
            wire.setVisited(false);
        }
    }

    public String toString() {
        return "WiredIntSupplierPool { wires.size(): "+wires.size()+" } ";
    }

}
