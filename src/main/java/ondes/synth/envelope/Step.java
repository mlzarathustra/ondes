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
     *     rate is the number of milliseconds to execute a full sweep,
     *     from level 0 to 100 or vice versa.  It is a logarithmic curve,
     *     meaning that the rate starts out quicker and slows as it
     *     converges with destLevel.
     * </p>
     * <p>
     *     So at the rate of 1000 (1 second) it will take quite a bit
     *     longer than 1/10th of a second to sweep from .1 to 0.
     * </p>
     * <p>
     *     The reason it can't be proportional to the actual step associated
     *     with it is that events (pedal up or down, note on or off) can cause a
     *     jump from one step to another step different from the sequential
     *     progression.
     * </p>
     *
     * @param rate - rate of full transition (in milliseconds)
     * @param level - level to transition to
     * @param sampleRate - sampling frequency
     */
    Step(int rate, double level, int sampleRate) {
        this.level =level;
        this.sampleRate = sampleRate;

        d = (sampleRate * rate / 1000.0) / 4.616;
        k = m = 1.0/d;
    }

    /**
     * See t1.m for MatLab test
     * @param curLevel - the current signal level
     * @return - the next signal level 0 <= rs <= 100
     */
    StepResult nextVal(double curLevel) {
        if (curLevel == level) {
            stepResult.level = curLevel;
            stepResult.done = true;
            return stepResult;
        }

        double delta = level - curLevel;
        double nextLevel = curLevel + (signum(delta)*k + delta*m);

        stepResult.done = false;
        if ( (curLevel > level && nextLevel <= level) ||
            (curLevel < level && nextLevel >= level) ) {

            stepResult.done = true;
            stepResult.level = level;
            return stepResult;
        }
        stepResult.level = max(0.0, min(100.0, nextLevel));
        return stepResult;
    }
}
