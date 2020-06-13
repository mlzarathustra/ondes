package ondes.synth.envelope;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import static java.lang.System.err;

/**
 * <p>
 *     Env is a state machine. The current state is represented by stepIdx
 *     For more on the parameters, see Envelope.md
 * </p>
 */
public class Envelope extends MonoComponent {

    /**
     * These are all indexes into steps
     */
    private int curStep = -1;
    private int reTrigger = -1;
    private int hold = -1;
    private int release = -1;
    private int altRelease = -1;


    /**
     * Do we return this voice to the pool when done?
     */
    private boolean exit = false;

    /**
     * true if the last note message received was "ON"
     * or the sustain pedal is down.
     */
    private boolean ON=false;

    /**
     * true if the last note message received was "ON"
     */
    private boolean noteON = false;

    /**
     * true if the last sustain pedal message was "DOWN"
     */
    private boolean susDown = false;

    private double curLevel;  // range: 0 to 1

    private ArrayList<Step> steps = new ArrayList<>();

    public Envelope() {
        //  TODO - most of the work will happen in configure()
        // when we have the Map

    }


    static Map<String, Integer[]> envs = new HashMap<>();

    static {
        //  pairs are { rate, level } (@see Step)
        //  levels are from 0 to 100
        envs.put("organ", new Integer[] { 8, 100, 8, 0});
        envs.put("clavier", new Integer[] {0, 100, 2,75, 25,50, 50,0, 10,0});
        envs.put("fade", new Integer[] {15,75, 25,100, 35,0});
    }

    /**
     * <p>
     *      Expects pairs of integers alternating: rate,level;
     *      rate is in milliseconds (for a full sweep). level is 0-100.
     *      The level is a percent of full volume for the main output, or
     *      the maximum out-level-amp for out-level.
     * </p>
     * <p>
     *     If there are an odd number of integers, the last will be ignored.
     * </p>
     */
    void setSteps(Integer... params) {
        if (params.length < 2) {
            err.println("Envelope does not provide any steps. A default will be used.");
            params = envs.get("organ");
        }
        if (params.length % 2 == 1) {
            err.println("Warning: Envelope received an odd number of values." +
                " The last one will be ignored");
        }
        for (int idx=0; idx<params.length-1; idx+=2) {
            steps.add(new Step(
                params[idx],
                (double)params[idx+1],
                synth.getSampleRate()));
        }
    }

    @SuppressWarnings("rawtypes")
    boolean setSteps(List lines) {
        for (int i=0; i<lines.size(); ++i) {
            String[] tokens = lines.get(i).toString().split("[\\s,]+");




        }



        return true;
    }



    private int getRelease() {
        if (release >= 0) return release;
        return steps.size() - 1;
    }




    @Override
    public void noteON(MidiMessage msg) {
        noteON=true;
        ON = true;
        if (reTrigger >= 0) curStep = reTrigger;
        else curStep = 0;
    }

    @Override
    public void noteOFF(MidiMessage msg) {
        if (!susDown) ON=false;
        curStep = getRelease();
    }

    @Override
    public void midiSustain(int val) {
        if (val > 0) {
            susDown = true;
            ON = true;
        }
        else {
            susDown = false;
            if (!noteON) {
                ON = false;
                curStep = getRelease();
            }
        }
        out.println("Env: sustain "+(val>0 ? "ON" : "OFF"));
    }

    void nextStep() {
        if (curStep < steps.size() - 1) ++curStep;
    }

    double nextVal() {
        if (!ON && curLevel == 0) return 0;
        if (isComplete(curLevel)) {
            if (curStep == release) return 0.0;
            if (curStep == steps.size()-1) return curLevel; // sustain

            nextStep();
        }
        double rs=curLevel;
        Step.StepResult stepResult = steps.get(curStep).nextVal(curLevel);
        curLevel = stepResult.level;

        if (stepResult.done) nextStep();

        return rs;
    }




    @Override
    public WiredIntSupplier getMainOutput() {
        return null;
    }

    @Override
    public int currentValue() {
        // TODO - REMEMBER: level is 100x, so it's
        //    inputs.sum() * level / 100.0

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
        Object inp;
        inp = config.get("exit");
        if (inp instanceof Boolean) {
            exit = (Boolean)inp;
        }
        else if (inp != null) {
            err.println("Envelope: exit property was specified but is not boolean.");
        }

        Object preset = config.get("preset");
        Object points = config.get("points");
        if (preset != null && points != null) {
            err.println("Envelope: both points and preset were specified. " +
                "points will take precedence.");
        }

        if (preset == null && points == null) {
            err.println("Envelope: neither points nor preset were specified. " +
                "Using default.");
            setSteps();
        }
        else if (preset != null) {
            Integer[] presetVals = envs.get(preset.toString());
            if (presetVals == null) {
                err.println("Envelope: Preset "+preset+" does not exist.");
                setSteps();
            }
            else setSteps(presetVals);
        }
        else {
            if (!(points instanceof List)) {
                err.println("Envelope: 'points' is not a list. reverting to default.");
                setSteps();
            }
            if (!setSteps((List)points)) {
                err.println("Reverting to default");
                setSteps();
            }
        }

        // TODO - implement: out, out-level, out-level-amp


    }

    @Override
    public void pause() {/* no phase clocks to shut off.*/}

    @Override
    public void resume() {/* no phase clocks to turn on.*/}


}

