package ondes.synth.envelope;

import static java.lang.Math.*;
import static java.lang.System.out;

/**
 * <p>
 *     One step (state) of the envelope.
 * </p>
 * <p>
 *     It has a level and a rate, and knows how to take the next step
 *     to a given level given the specified sampling rate, or
 *     signal that it is done getting there.
 * </p>
 */
class Step {

    static class StepResult {
        double level;
        boolean done;
        public String toString() {
            return "StepResult { level="+level+"; done="+done+" }";
        }
    }
    StepResult stepResult = new StepResult();

    /**
     * rate is how long it takes to get to "level"
     */
    double rate;


    /**
     * level is floating point, 0 <= level <= 100
     */
    final double level;

    /**
     * guarantee that level is within range.
     */
    double clip(double level) {
        return max(0.0, min(100.0, level));
    }

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
     * <p>
     *     level is between 0 to 100. If a value outside the range is given,
     *     it will be clipped without warning.
     * </p>
     *
     * @param rate - rate of full transition (in milliseconds)
     * @param level - level to transition to
     * @param sampleRate - sampling frequency
     */
    Step(double rate, double level, int sampleRate) {
        this.rate = max(0,rate);
        this.level = clip(level);
        this.sampleRate = sampleRate;

        d = ( rate * sampleRate / 1000.0) / 4.616;
        k = m = 1.0/d;
    }

    Step(Envelope.StepParam sp) {
        this((int)sp.rate, sp.level, sp.sampleRate);
    }

    boolean SHORT_STRING = true;
    public String toString() {
        String type= (this instanceof Hold) ? "Hold" : "Step";
        if (SHORT_STRING) {
            return String.format(
                type+" { rate=%8.1f level=%7.2f sampleRate=%3.0f -- " +
                    "d=%6.3e k=%2.3e m=%2.3e", rate, level, sampleRate, d,k,m);
        }
        return type+" { rate="+rate+"; level="+level+
            "; sampleRate="+sampleRate+
            "; d="+d+"; k="+k+"; m="+m+" } ";
    }

    /**
     * See t1.m for MatLab test
     * @param curLevel - the current signal level
     * @return - the next signal level 0 <= rs <= 100
     */
    StepResult nextVal(double curLevel) {
        if (rate == 0 || curLevel == level) {
            stepResult.level = level;
            stepResult.done = true;
            return stepResult;
        }

        double delta = level - curLevel;
        double nextLevel = curLevel + (signum(delta)*k + delta*m);

        stepResult.done = false;
        if ( (curLevel > level && nextLevel <= level) ||
            (curLevel < level && nextLevel >= level) ) {

            if (Envelope.DB) out.println("StepResult: DONE");

            stepResult.done = true;
            stepResult.level = level;
            return stepResult;
        }

        stepResult.level = clip(nextLevel);
        return stepResult;
    }
}
