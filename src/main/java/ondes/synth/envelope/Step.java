package ondes.synth.envelope;

/**
 * One step (state) of the envelope.  rate is 1..100; destLevel is 0..1
 * <p>
 *
 * Assuming a sample rate of 44100, the rate will range between
 * 1 sample and about 23 seconds (from the formula 1/rate**3
 * in setRate() below)
 * <p>
 *
 * 1 is the quickest rate, 100 the slowest.
 *
 */
class Step {
    private int rate;
    double destLevel, delta;

    Step(int r, double l) {
        rate=Math.max(1, Math.min(100,r));
        destLevel=Math.max(0,Math.min(1,l));
    }

    void setDelta(double curLevel) {
        delta = 1.0 / (double)(rate*rate*rate);
        if (destLevel < curLevel) delta = -delta;
    }

    double nextVal(double curLevel) {
        if (curLevel == destLevel) return curLevel;

        curLevel += delta;
        if (    (delta > 0 && curLevel > destLevel) ||
                (delta < 0 && curLevel < destLevel)) {

            curLevel=destLevel;
        }
        curLevel=Math.max(0.0,Math.min(1.0,curLevel)); //  clip between 0.0 and 1.0
        return curLevel;
    }

    boolean isComplete(double level) {
        return
                (delta == 0) ||
                (delta > 0 && level >= destLevel) ||
                (delta < 0 && level <= destLevel);
    }
}
