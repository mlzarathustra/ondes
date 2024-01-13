package ondes.synth.wire;

import java.util.function.IntSupplier;

public abstract class WiredIntSupplier implements IntSupplier {
    protected boolean visited;
    protected int curOut;

    /**
     * <p>
     *     Acts as a latch so that loops won't be infinite.
     *     If a Supplier has already been visited,
     *     it returns the value it already computed in this cycle.
     * </p>
     * <p>
     *     So "visited" needs to be reset for each sample.
     * </p>
     *
     * @return - a possibly cached value
     */
    public int getAsInt() {
        if (visited) return curOut;
        visited = true;
        curOut=updateInputs();
        return curOut;
    }

    /**
     * Override to update all inputs to the Component
     * if there are any, and return the current value
     *
     * @return - the current output value (level) of this
     * component after all the inputs have been updated
     */
    abstract public int updateInputs();

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

}
