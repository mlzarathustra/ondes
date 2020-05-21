package ondes.synth;

import java.util.function.IntConsumer;

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
