package ondes.synth;

import java.util.function.IntSupplier;

public class WiredIntSupplier implements IntSupplier {
    private boolean visited;
    private int curOut;

    public int getAsInt() {
        if (visited) return curOut;
        visited = true;
        curOut=updateInputs();
        return curOut;
    }

    // override to update all inputs and return current value
    int updateInputs() { return 0; }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
