package ondes.synth.wire;

import java.util.*;
import java.util.function.IntSupplier;

/**
 * The list of connections within a single voice
 * We need to keep the list because they all need to
 * have the 'visited' flag reset for every sample.
 *
 */
public class WiredIntSupplierMaker  {

    List<WiredIntSupplier> wires = new ArrayList<>();

    public WiredIntSupplier getWiredIntSupplier(IntSupplier iu) {
        WiredIntSupplier wire = new WiredIntSupplier() {
            public int updateInputs() { return iu.getAsInt(); }
        };
        wires.add(wire);
        return wire;
    }

    public void reset() {
        for (WiredIntSupplier wire : wires) {
            wire.setVisited(false);
        }
    }

}
