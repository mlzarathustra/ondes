package ondes.synth.envelope;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import javax.sound.midi.MidiMessage;
import java.util.*;

import static java.lang.System.out;
import static java.lang.System.err;
import static java.util.stream.Collectors.toList;
import static java.lang.Math.*;
import static ondes.mlz.Util.getList;

/**
 * <p>
 *     Env is a state machine. The current state is represented by stepIdx
 *     For more on the parameters, see Envelope.md
 * </p>
 */
public class Envelope extends MonoComponent {

    /**
     * NOTE: this is the order they MUST appear in (though any
     * may be omitted)
     */
    static final List<String> options =
        Arrays.asList("re-trigger", "hold", "release", "alt-release");
    static Map<String, Integer[]> envs = new HashMap<>();

    static {
        //  pairs are { rate, level } (@see Step)
        //  levels are from 0 to 100
        envs.put("organ", new Integer[] { 0,0, 8,100, 8,0});
        envs.put("clavier", new Integer[] {0,0, 0,100, 2,75, 25,50, 50,0, 10,0});
        envs.put("fade", new Integer[] {0,0, 15,75, 25,100, 35,0});
    }

    private final ArrayList<Step> steps = new ArrayList<>();
    public WiredIntSupplier levelOutput = null;
    double outLevelMin=0, outLevelMax=100;
    boolean firstNoteON = true;
    int chan, note; // for exit

    /**
     * These are all indexes into steps
     */
    private int curStep = -1;
    private int reTrigger = -1;
    private int hold = -1;
    private int release = -1;

    // /// // /// // /// // /// // /// // /// // /// // /// // /// //
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
    /**
     * range: 1..100
     * @see Step#clip(double)
     */
    private double curLevel;

    public Envelope() { }

    @Override
    public void noteON(MidiMessage msg) {
        noteON=true;
        ON = true;

        chan = msg.getStatus() & 0xf;
        note = msg.getMessage()[1];

        if (firstNoteON) curStep = 0;
        else curStep = max(reTrigger, 0);
    }

    @Override
    public void noteOFF(MidiMessage msg) {
        if (!susDown) ON=false;

        curStep = release;

        // TODO - does it jump to alt release if susDown?
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
                curStep = release;
            }
        }
        out.println("Env: sustain "+(val>0 ? "ON" : "OFF"));
    }

    void nextStep() {

        //  TODO - this needs to be a lot smarter!

        if (curStep < steps.size() - 1) ++curStep;

        if (isComplete()) synth.noteEnded(chan,note);

    }

    boolean isComplete() {
        return false;

        // TODO - implement

    }

    public int currentLevel() {
        return (int)currentLevel(outLevelMin, outLevelMax);
    }

    public double currentLevel(double min, double max) {
        // TODO - REMEMBER: level is 100x, so it's
        //    (inputs.sum() * level / 100.0) * (max - min) + min

        //  TODO - If this level is the same as the last, delay
        //        for the number of ms specified. Document.
        //

        if (!ON && curLevel == 0) return 0;
        // TODO - use Step.done instead
//        if (isComplete(curLevel)) {
//            if (curStep == release) return (int)0.0;
//            if (curStep == steps.size()-1) return (int)curLevel; // sustain
//
//            nextStep();
//        }
        double rs=curLevel;
        Step.StepResult stepResult = steps.get(curStep).nextVal(curLevel);
        curLevel = stepResult.level;

        if (stepResult.done) nextStep();

        return rs;
    }

    @Override
    public int currentValue() {
        int signal=0;
        for (WiredIntSupplier inp : inputs) signal += inp.getAsInt();
        return (int)(currentLevel(0.0, 1.0) * signal);
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
    }

    void show() {
        steps.forEach(out::println);
        out.println(String.format("[Indexes] reTrigger=%d, hold=%d, release=%d, altRelease=%d",
            reTrigger, hold, release, altRelease));
        out.println(String.format("outLevelMin=%f outLevelMax=%f",
            outLevelMin, outLevelMax));
        out.println();
    }

    /*
                    CONFIGURATION

                    points

     */
    class StepParam {
        double rate, level;
        int sampleRate = synth.getSampleRate();

        String option="";
        boolean ok = false;
        StepParam(String line) {
            String[] tokens = line.split("[\\s,]+");
            if (tokens.length < 2 || tokens.length > 3) return;
            try {
                rate = Double.parseDouble(tokens[0]);
                level = Double.parseDouble(tokens[1]);
            }
            catch (Exception ex) { return; }
            if (tokens.length == 3) {
                option = tokens[2];
                ok = (options.contains(option) );
            }
            else ok=true;
        }
        public String toString() {
            return "StepParam { rate="+rate+"; level="+level+
                "; option="+option+" }";
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
    void setSteps(Integer... params) {
        if (params.length < 2) {
            err.println("Envelope does not provide a valid list of steps. " +
                "A default will be used.");
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

    public WiredIntSupplier getLevelOutput() {
        if (levelOutput == null) {
            levelOutput = getVoice()
                .getWiredIntSupplierMaker()
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
            StepParam sp = new StepParam(line.toString());
            if (!sp.ok) {
                err.println("ERROR in envelope points: " + line);
                return false;
            }
            //  supply a step 0,0 if needed
            if (steps.size() == 0 && sp.rate != 0) {
                steps.add(zeroZeroStep());
            }
            steps.add(new Step(sp));
            switch (sp.option) {
                case "": break;
                case "re-trigger": reTrigger = steps.size() - 1; break;
                case "hold": hold = steps.size() - 1; break;
                case "release": release = steps.size() - 1; break;
                case "alt-release": altRelease = steps.size() - 1; break;
                default:
                    err.println("Unknown Envelope option: "+sp.option+" will be ignored.");
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

        //  if there's an alt-release, make sure the step before it
        //  goes to 0, since that will be the last step of the
        //  'release' sequence.
        if (altRelease > 0 && steps.get(altRelease - 1).level != 0) {
            steps.add(altRelease, zeroZeroStep());
            altRelease++;
        }

        //  figure out where the 'release' step is
        if (release < 0) {
            if (hold >= 0) {
                release = hold +((hold < steps.size() - 1) ? 1 : 0);
            }
            else {
                if (altRelease < 0) release = steps.size() - 1;
                else release = altRelease - 1;
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
        if (exit) getVoice().setWaitForEnv(exit);

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
            Integer[] presetVals = envs.get(preset.toString());
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

            String minMaxStr = config.get("out-level-amp").toString();
            if (minMaxStr == null) {
                outLevelMin = 0; outLevelMax = 100;
            }
            else {
                try {
                    List<Double> minMax = Arrays.stream(minMaxStr.split("[\\s,]+"))
                        .map( Double::parseDouble ).collect(toList());

                    if (minMax.size() == 1) minMax.add(0.0);

                    if (minMax.size() == 2) {
                        Collections.sort(minMax);
                        outLevelMin = minMax.get(0);
                        outLevelMax = minMax.get(1);
                    }
                    else {
                        err.println(
                            "ERROR! out-level-amp: if present, must be one or two numbers.");
                        outLevelMin=0; outLevelMax=100;
                    }
                }
                catch (NumberFormatException ex) {
                    err.println("ERROR: Envelope: Invalid number in out-level-amp. "+
                        minMaxStr);
                    outLevelMin=0; outLevelMax=100;

                }
            }
        }
        show();
    }

}

