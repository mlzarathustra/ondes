package ondes.synth.envelope;

import ondes.synth.Instant;



/**
 * Like Step, but it implements a hold at this level
 * for the specified amount.
 *
 */

public class Hold extends Step {
    Instant instant;
    double start;

    Hold(int rate, double level, Instant instant) {
        super(rate,level,instant.getSampleRate());
        this.instant = instant;
        stepResult.level = level;
    }

    void reset() {
        start = instant.getSeconds();
    }

    boolean done() {
        return instant.getSeconds() >= start + ((double)rate)/1000.0;
    }

    StepResult nextVal(double curLevel) {
        stepResult.done = done();
        return stepResult;
    }




}
