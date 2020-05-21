package ondes.synth;

import java.util.function.IntConsumer;

/**
 * The nomenclature may be confusing because
 * it's from the perspective of the wire:
 * <br/><br/>
 *
 * IN - is the data coming in to the wire from the
 * IntSupplier source. It is cached for each time slice
 * by WiredIntSupplier, to avoid infinite looping
 * (e.g. if a component uses its output as input,
 * common in FM synthesis)
 * <br/><br/>
 *
 * OUT - is the data going out of the wire to the
 * IntConsumer target.
 *
 *
 */
public class Wire {
    private WiredIntSupplier in;
    private IntConsumer out;
    private double scale = 1;

    void setScale(double v) { scale = v; }

    public void update() {
        out.accept( (int)(scale * in.getAsInt()) );
    }

    Wire prev, next;

    public boolean isVisited() {
        return in.isVisited();
    }

    public void setVisited(boolean visited) {
        in.setVisited(visited);
    }

}
