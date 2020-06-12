package ondes.synth.envelope;

import static java.lang.Math.*;

/**
 * <p>
 *     One step (state) of the envelope.
 * </p>
 * <p>
 *     It has a level and a rate, and knows how to take the next step
 *     to a given level given the specified sampling rate.
 * </p>
 */
class Step {

    static class StepResult {
        double level;
        boolean done;
    }
    StepResult stepResult = new StepResult();

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
    double level;

    /**
     * probably an integer, but double for the math
     */
    double sampleRate;

    /**
     * used for computing the next step.
     */
    double d, k, m;

    /**
     * <p>
     *     For some reason, 4.6052 (the denominator) is log(10).
     *     Someone with better math skills could tell me why.
     * </p>
     * <p>
     *     What I CAN say is that this equation does seem to
     *     produce the delay described when I plot it in MatLab.
     *     It was more or less a lucky guess.
     *     What's life without mystery? :^)
     * </p>
     *
     * @param rate - rate of full transition (in milliseconds)
     * @param level - level to transition to
     * @param sampleRate - sampling frequency
     */
    Step(int rate, double level, int sampleRate) {
        this.rate=rate;
        this.level =level;
        this.sampleRate = sampleRate;

        d = (sampleRate * rate / 1000.0) / 4.6052;
        k = m = 1.0/d;
    }

    StepResult nextVal(double curLevel) {

        if (curLevel == level) {
            stepResult.level = curLevel;
            stepResult.done = true;
            return stepResult;
        }

        double nextLevel = curLevel + (k + (level - curLevel) * m);

        stepResult.done = false;
        if ( (curLevel > level && nextLevel <= level) ||
            (curLevel < level && nextLevel >= level) ) {

            stepResult.done = true;
            stepResult.level = level;
            return stepResult;
        }
        //  clip between 0.0 and 1.0
        stepResult.level = max(0.0, min(1.0, nextLevel));
        return stepResult;
    }
}
