package ondes.synth;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Wire {
    private IntSupplier in;
    private IntConsumer out;
    private double scale = 1;

    void setScale(double v) { scale = v; }

    public void update() {
        out.accept( (int)(scale * in.getAsInt()) );
    }

    Wire prev, next;
}
