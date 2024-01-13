package ondes.synth.envelope;

import ondes.synth.OndeSynth;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.out;
import static java.lang.System.err;
import static java.util.stream.Collectors.toList;
import static java.lang.Math.*;

import static ondes.mlz.Util.getList;
import static ondes.synth.component.ConfigHelper.*;

/**
 * <p>
 *     Env is a state machine. The current state is represented by stepIdx
 *     For more on the parameters, see Envelope.md
 * </p>
 */
public class Envelope extends MonoComponent {
    static public boolean DB = false;

    /**
     * NOTE: this is the order they MUST appear in, though any may be omitted.
     * They may contain embedded dashes (e.g. "re-trigger" and "alt-release")
     */
    static final List<String> options =
        Arrays.asList("retrigger", "hold", "release", "altrelease");


    static Map<String, List<String>> envs = new HashMap<>();

    static {
        //  pairs are { rate, level } (@see Step)
        //  levels are from 0 to 100
        envs.put("organ",
            List.of("8 100 hold", "10 0"));

        envs.put("clavier",
            List.of("4 100", "10 75", "2000 33", "5000 10", "500 0"));

        envs.put("fade",
            List.of("3000 100 re-trigger", "2000 85  hold", "5000 0", "1e9 0 alt-release"));
    }

    private final ArrayList<Step> steps = new ArrayList<>();
    public WiredIntSupplier levelOutput = null;
    double outLevelMin=0, outLevelMax=100;
    boolean firstNoteON = true;
    int chan, note; // for exit

    /**
     * These are all indexes into steps
     */
    private int curStep = 0;
    private int reTrigger = -1;
    private int hold = -1;
    private int release = -1;

    private float levelScale=1;

    // /// // /// // /// // /// // /// // /// // /// // /// // /// //
    private int altRelease = -1;
    /**
     * Do we return this voice to the pool when done?
     */
    public boolean exit = false;

    /**
     * true if the last note message received was "ON"
     */
    private boolean noteON = false;
    /**
     * true if the last sustain pedal message was "DOWN"
     */
    private boolean susDown = false;

    /**
     * true if we're before the release step.
     */
    private boolean preRelease = true;

    /**
     * range: 1..100
     * @see Step#clip(double)
     */
    private double curLevel;

    public Envelope() { }

    public Envelope(OndeSynth synth, String str) {
        this.synth = synth;
        setSteps(envs.get(str));
    }

    @Override
    public void noteON(MidiMessage msg) {
        noteON=true;

        chan = msg.getStatus() & 0xf;
        note = msg.getMessage()[1];

        if (firstNoteON) curStep = 0;
        else curStep = max(reTrigger, 0);
        firstNoteON = false;

    }

    @Override
    public void noteOFF(MidiMessage msg) {
        noteON = false;
        if (!susDown) {
            preRelease = false;
            if (curStep == hold) setCurStep(hold+1);
            else setCurStep(release);
        }
    }

    @Override
    public void midiSustain(int val) {
        if (val > 0) {
            susDown = true;
            if (!preRelease && altRelease >= 0) {
                setCurStep(altRelease);
            }
        }
        else {
            susDown = false;
            if (!noteON) {
                setCurStep(release);
            }
        }
        if (DB) out.println("Env: sustain "+(val>0 ? "ON" : "OFF"));
    }

    private void setCurStep(int step) {
        if (DB) out.println("setCurStep("+step+")");
        curStep = step;

        if (curStep == release) preRelease = false;

        // a Hold needs to know its start time.
        Step next = steps.get(curStep);
        if (next instanceof Hold) {
            ((Hold) next).reset();
        }
    }

    boolean isComplete() {
        return curLevel == 0 &&
            (curStep == altRelease-1 || curStep == steps.size()-1);
    }


    private void nextStep() {

        // holding. no advance
        if (curStep == hold && (susDown || noteON)) return;

        // if at the end, and we are responsible for the exit, queue it.
        if (curStep == altRelease-1 || curStep == steps.size()-1) {

            if (exit) {
                // we can't remove the note immediately from main.out
                // while it's looping through its inputs.
                if (DB) out.println("Queuing note end.");
                synth.queueNoteEnd(chan, note);
            }
            return; // stay here.
        }
        setCurStep(curStep + 1);
    }

    // /// // ///     // /// // ///     // /// // ///    // /// // ///

    /**
     * <p>
     *      For the "out-level" output. Outputs the level
     *      of attenuation without the signal. For modulating
     *      frequency, pwm, &c.
     * </p>
     *
     * @return - the output level in a range defined by the
     *      "out-level-amp" config parameter.
     */
    public int currentLevel() {
        return (int)currentLevel(outLevelMin, outLevelMax);
    }

    /**
     * <p>
     *     the Step will return a level between 0 and 100,
     *     hence the division by 100.0
     * </p>
     * @param min - lowest return value expected
     * @param max - hightest return value expected
     * @return - the level at the current step
     */
    public double currentLevel(double min, double max) {
        Step.StepResult stepResult = steps.get(curStep).nextVal(curLevel);
        curLevel = stepResult.level;

        if (stepResult.done) nextStep();

        return min + ((max - min) * (curLevel / 100.0));
    }

    /**
     * @return - the current output, the input signal attenuated by
     *           the envelope (i.e. currentLevel())
     */
    @Override
    public int currentValue() {
        if (isComplete()) return 0;
        return (int)(currentLevel(0.0, 1.0) * inputSum() * levelScale);
    }


                     // ... // ... //   .. ///                 ..
      // /// // ///     // /// // ///        //     ///      //      ///   // /// .. ...
       //   .. ... .. ... .. ... ..   .. ... // ... // ... // ... // ... // ...
           // *** // *** // *** // ***             *** // *** // *** // *** // ***
                ///  **    /// **     .. ... .. ... // ***      /// **    //

    /*

                    CONFIGURATION

                        presets
     */

    @Override
    public void pause() {/* no phase clocks to shut off.*/}

    @Override
    public void resume() {
        /* no phase clocks to turn on.*/

        // first envelope starts at 0
        firstNoteON = true;
        preRelease = true;
    }

    void show() {
        for (int i=0; i<steps.size(); ++i) out.println("["+i+"] "+steps.get(i));
        out.println(String.format("[Indexes] reTrigger=%d, hold=%d, release=%d, altRelease=%d",
            reTrigger, hold, release, altRelease));
        out.println(String.format("outLevelMin=%f outLevelMax=%f exit=%b",
            outLevelMin, outLevelMax, exit));
        out.println();
    }

    /*
                    CONFIGURATION

                    points

     */
    class StepParam {
        double rate, level;
        int sampleRate = synth.getSampleRate();

        ArrayList<String> stepOptions=new ArrayList<>();
        boolean ok = false;
        StepParam(String line) {
            String[] tokens = line.split("[\\s,]+");
            if (tokens.length < 2) return;
            try {
                rate = Double.parseDouble(tokens[0]);
                level = Double.parseDouble(tokens[1]);
            }
            catch (Exception ex) { return; }
            ok=true;

            for (int i=2; i<tokens.length; ++i) {
                String option = tokens[i].replaceAll("-","");
                ok = (options.contains(option) );
                if (ok) stepOptions.add(option);
            }
        }
        public String toString() {
            return "StepParam { rate="+rate+"; level="+level+
                "; options="+stepOptions+" }";
        }
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
    void setSteps() {
        setSteps(envs.get("organ"));
    }

    public WiredIntSupplier getLevelOutput() {
        if (levelOutput == null) {
            levelOutput = getOwner()
                .getWiredIntSupplierPool()
                .getWiredIntSupplier(this::currentLevel);
        }
        return levelOutput;
    }

    Step zeroZeroStep() {
        return new Step(0, 0, synth.getSampleRate());
    }

    /**
     *
     * <p>
     *     Parse the points given and set steps accordingly.
     * </p>
     * <p>
     *     Some validity checking: both of the first two fields must
     *     be valid numbers, and the option must be one of those known,
     *     and in the proper order.
     * </p>
     * <p>
     *     If the rate of the first step is 0, we'll start at its level.
     *     Otherwise, we add a step 0,0.
     *
     * </p>
     * @param lines - the list of points (strings)
     * @return - true if all OK.
     */
    @SuppressWarnings("rawtypes")
    boolean setSteps(List lines) {
        for (Object line : lines) {
            if (line == null) continue;
            StepParam sp = new StepParam(line.toString());
            if (!sp.ok) {
                err.println("ERROR in envelope points: " + line);
                return false;
            }
            //  supply a step 0,0 if needed
            if (steps.size() == 0 && sp.rate != 0) {
                steps.add(zeroZeroStep());
            }
            // if this level is the same as the last, it's a hold
            // unless this is the alt-release.
            //
            if (steps.size() > 0 &&
                (! sp.stepOptions.contains("altrelease")) &&
                steps.get(steps.size()-1).level == sp.level
            ) {
                steps.add(new Hold((int)sp.rate, sp.level, synth.getInstant()));
            }
            else {
                steps.add(new Step(sp));
            }
            for (String option : sp.stepOptions) {
                switch (option) {
                    case "": break;
                    case "retrigger":
                        reTrigger = steps.size() - 1; break;
                    case "hold":
                        hold = steps.size() - 1; break;
                    case "release":
                        release = steps.size() - 1; break;
                    case "altrelease":
                        altRelease = steps.size() - 1; break;
                    default:
                        err.println("Unknown Envelope option: " + option +
                            " will be ignored.");
                }
            }
        }

        // Make sure options appear in the correct order (-1 = n/a)
        List<Integer> opts = List.of(reTrigger, hold, release, altRelease).stream()
            .filter( n -> n >= 0).collect(toList());
        for (int i=0; i<opts.size() - 1; ++i) {
            if (opts.get(i) > opts.get(i + 1)) {
                err.println("Envelope options in the wrong order." );
                err.println("the correct order is: "+ options);
                return false;
            }
        }

        //  make sure the last step goes to 0
        if (steps.get(steps.size()-1).level != 0) {
            steps.add(zeroZeroStep());
        }
        if (hold >= 0 && steps.get(hold).level == 0) {
            out.println("Envelope: hold step cannot go to 0.");
            hold = -1;
        }

        //  if there's an alt-release, make sure the step before it
        //  goes to 0, since that will be the last step of the
        //  'release' sequence.
        if (altRelease > 0 && steps.get(altRelease - 1).level != 0) {
            steps.add(altRelease, zeroZeroStep());
            altRelease++;
        }

        //  figure out where the 'release' step is
        //  the right answer can be tricky depending on
        //  the event sequence (note ON/OFF, sustain)
        if (altRelease > 0) release = altRelease  - 1;
        else {
            if (release < 0) {
                if (hold >= 0) {
                    release = hold + ((hold < steps.size() - 1) ? 1 : 0);
                } else {
                    if (altRelease < 0) release = steps.size() - 1;
                    else release = altRelease - 1;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
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
        // don't set it if it's false, because another envelope might.
        if (exit) getOwner().setWaitForEnv(exit);

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
        else if (points != null) {
            if (!(points instanceof List)) {
                err.println("Envelope: 'points' is not a list. reverting to default.");
                setSteps();
            }
            else if (!setSteps((List)points)) {
                err.println("Reverting to default");
                setSteps();
            }
        }
        else {
            List<String> presetVals = envs.get(preset.toString());
            if (presetVals == null) {
                err.println("Envelope: Preset "+preset+" does not exist.");
                setSteps();
            }
            else setSteps(presetVals);
        }

        // The level output is for modulating other components (e.g. PWM)
        Object levelOutObj = config.get("out-level");
        if (levelOutObj != null) {
            List levelOutList = getList(levelOutObj);
            setOutput(levelOutList, components, getLevelOutput());

            double[] mm = getMinMaxLevel(config.get("out-level-amp").toString());
            outLevelMin = mm[0];
            outLevelMax = mm[1];
        }

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        Float fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else
                levelScale = fltInp;
        }

        if (DB) show();
    }

}

