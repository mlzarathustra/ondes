package ondes.synth.envelope;

/**
 * <p>
 *     One step (state) of the envelope.
 * </p>
 *
 * <p>
 *
 *
 *
 *
 */
class Step {

    /**
     * <p>
     *     rate is the number of milliseconds to execute a full sweep,
     *     from level 0 to 1 or vice versa.  It is a logarithmic curve,
     *     meaning that the rate starts out quicker and slows as it
     *     converges with destLevel.
     * </p>
     * <p>
     *     So at the rate of 1000 (1 second) it will take longer than
     *     1/10th of a second to sweep from .1 to 0. The reason it
     * </p>
     *
     */
    private int rate;

    /**
     * destLevel is floating point, 0 <= destLevel <= 1
     */
    double destLevel;

    Step(int r, double l) {
        rate=r;
        destLevel=l;
    }

    double nextVal(double curLevel) {
        if (curLevel == destLevel) return curLevel;


        curLevel=Math.max(0.0,Math.min(1.0,curLevel)); //  clip between 0.0 and 1.0
        return curLevel;
    }

    // This check may need to happen above, as we need to know
    // the value of the previous step so we know which direction we
    // were going in.
    //
    boolean isComplete(double level) {
        return false;

        // TODO - implement

//                (delta == 0) ||
//                (delta > 0 && level >= destLevel) ||
//                (delta < 0 && level <= destLevel);
    }
}
