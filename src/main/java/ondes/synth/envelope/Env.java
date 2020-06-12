package ondes.synth.envelope;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.System.out;
import static java.lang.System.err;

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
class Env extends MonoComponent {
    /**
     stepIdx range: 0..steps.size() inclusive
     < steps.size() means it's an index into steps
     == steps.size() indicates the release
     */
    private int stepIdx = 0;
    private boolean ON=false;

    private double curLevel;  // range: 0 to 1

    private ArrayList<Step> steps = new ArrayList<>();
    private Step release=new Step(0,0,synth.getSampleRate());

    private Step curStep;

    private void setCurStep(Step step) {
        curStep=step;
    }

    //  clip idx, set curStep, and set delta from curLevel
    //
    private void setCurStep(int idx) {
        stepIdx=Math.max(0,Math.min(idx,steps.size()-1));
        stepIdx=idx;
        if (steps.size() == 0 || stepIdx >= steps.size()) curStep = release;
        else curStep=steps.get(idx);
    }

    Step currentStep() { return curStep; }

    private Step nextStep() {
        if (stepIdx < steps.size() - 1) setCurStep(stepIdx+1);
        return curStep;
    }

    Env() {
        //  TODO - most of the work will happen in configure()
        // when we have the Map

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

        //throw new InstantiationException(" This class needs to be adapted to the Ondes model!");
        //
        // TODO - implement, then uncomment the below and delete the above.

        /*
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

         */
    }


    @Override
    public void noteON(MidiMessage msg) {
        ON=true;
        setCurStep(0);
    }

    @Override
    public void noteOFF(MidiMessage msg) {
        ON=false;
        setCurStep(release);
    }

    @Override
    public void midiSustain(int val) {

        out.println("Env: sustain "+(val>0 ? "ON" : "OFF"));
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
        if (isComplete(curLevel)) {
            if (curStep == release) return 0.0;
            if (stepIdx == steps.size()-1) return curLevel; // sustain

            nextStep();
        }
        double rs=curLevel;
        Step.StepResult stepResult = curStep.nextVal(curLevel);
        curLevel = stepResult.level;

        if (stepResult.done) {

            //  TODO - move on to the next step.


        }

        return rs;
    }




    @Override
    public WiredIntSupplier getMainOutput() {
        return null;
    }

    @Override
    public int currentValue() {
        return 0;
    }

    // This check may need to happen above, as we need to know
    // the value of the previous step so we know which direction we
    // were going in.
    //
    boolean isComplete(double level) {
        return false;

        // TODO - implement

    }



    public void configure(Map config, Map components) {
        super.configure(config, components);

        Object preset = config.get("preset");


        // TODO - implement


    }

    @Override
    public void pause() {
        // TODO - implement
    }

    @Override
    public void resume() {
        // TODO - implement
    }


}

