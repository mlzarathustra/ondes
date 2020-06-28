package ondes.synth.envelope;

import ondes.synth.Instant;

import static java.lang.System.out;

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
        if (Envelope.DB) out.println("Hold.reset(): start="+start);
    }

    boolean done() {
        return instant.getSeconds() >= start + rate/1000.0;
    }

    StepResult nextVal(double curLevel) {
        if (curLevel != level) return super.nextVal(curLevel);
        stepResult.done = done();
        stepResult.level = level;
        return stepResult;
    }




}
