package ondes.synth.wire;

public abstract class ScalingWiredIntSupplier extends WiredIntSupplier {

    private double scale = 1;
    void setScale(double v) { scale = v; }

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
        curOut=(int)(scale * updateInputs());
        return curOut;
    }


}
