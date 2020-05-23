package ondes.synth;

import java.util.function.IntSupplier;

public abstract class WiredIntSupplier implements IntSupplier {
    private boolean visited;
    private int curOut;

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
