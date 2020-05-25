package ondes.synth.wire;

import java.util.function.IntSupplier;

public abstract class WiredIntSupplier implements IntSupplier {
    protected boolean visited;
    protected int curOut;

    WiredIntSupplier() {

    //  TODO - register this in a global (LINKED) list for reset
    //    they will have to be removed by any Voice
    //    releasing them, or they will build up.
    }

    /**
     * Acts as a latch so that loops won't be infinite
     * If a Supplier has already been visited, it returns
     * the value it already computed in this cycle.
     * <br/><br/>
     *
     * So "visited" needs to be reset for each sample.
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
     */
    abstract public int updateInputs();

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

}
