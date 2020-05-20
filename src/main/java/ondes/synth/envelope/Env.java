package ondes.synth.envelope;

import java.util.ArrayList;

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

/**
 * Env is basically a state machine. The current state is
 * represented by stepIdx
 * <p>
 *
 * The first step is triggered by the attack.
 * <p>
 *
 * When we reach (or pass) the destLevel of a step, we move on
 * to the next step.
 * <p>
 *
 * The destLevel of the last element of steps is the sustain,
 * held until the noteOFF triggers the release step.
 *
 */
class Env {
    /**
     stepIdx range: 0..steps.size() inclusive
     < steps.size() means it's an index into steps
     == steps.size() indicates the release
     */
    private int stepIdx = 0;
    private boolean ON=false;

    private double curLevel;  // range: 0 to 1

    private ArrayList<Step> steps = new ArrayList<Step>();
    private Step release=new Step(0,0);

    private Step curStep;

    private void setCurStep(Step step) {
        curStep=step;
        curStep.setDelta(curLevel);
    }

    //  clip idx, set curStep, and set delta from curLevel
    //
    private void setCurStep(int idx) {
        stepIdx=Math.max(0,Math.min(idx,steps.size()-1));
        stepIdx=idx;
        if (steps.size() == 0 || stepIdx >= steps.size()) curStep = release;
        else curStep=steps.get(idx);
        curStep.setDelta(curLevel);
    }

    Step currentStep() { return curStep; }

    private Step nextStep() {
        if (stepIdx < steps.size() - 1) setCurStep(stepIdx+1);
        return curStep;
    }

    /**
     * Expects pairs of integers alternating: rate,level;
     * rate is 1-100, level is 0-100. Rate will range from
     * one sample width to about 23 seconds, with 1 being
     * the quickest.
     * <p>
     *
     * The level is (obviously) a percent of full volume.
     *
     */
    Env(Integer... params) throws InstantiationException {
        if (params.length < 2) {
            throw new InstantiationException("must provide at least one step ");
        }
        for (int idx=0; idx<params.length-1; idx+=2) {
            steps.add(new Step(params[idx],((double)params[idx+1])/100.0));
        }
        if (steps.size() > 0) {
            release = steps.remove(steps.size()-1); // the last step is the release
        }
        // else if only one step is given, use the default release.

        release.destLevel = 0.0; // force the final level to zero
        setCurStep(0);
    }


    void noteON() {
        ON=true;
        setCurStep(0);
    }

    void noteOFF() {
        ON=false;
        setCurStep(release);
    }


    boolean isCompleteOrig() {
        // in release phase, and having reached zero.
        return curStep == release && curLevel == 0;
    }


    // if curLevel has reached zero and it's not the first step
    //        we can assume we're done even if we're not at the release.
    //        (e.g. clavier sound)
    //  We have to signal back to the event thread to un-grey the note.
    //  We can't set the background color from a different thread.
    //
    boolean isComplete() {          // ##GREY
        return curLevel == 0 && curStep != steps.get(0);
    }

    double nextVal() {
        if (!ON && curLevel == 0) return 0;
        if (curStep.isComplete(curLevel)) {
            if (curStep == release) return 0.0;
            if (stepIdx == steps.size()-1) return curLevel; // sustain

            nextStep();
        }
        double rs=curLevel;
        curLevel = curStep.nextVal(curLevel);
        return rs;
    }
}

