package ondes.synth.effect;

import ondes.synth.component.MonoComponent;

import java.util.Arrays;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.System.err;
import static ondes.synth.component.ConfigHelper.*;

/**
 *
 * <p>
 *     ECHO! Echo! echo, cho, o, o . . ......
 * </p>
 */
public class Echo extends MonoComponent {

    private double levelScale = 1;

    int[] tape;
    int t0 = 0;
    float maxDelay; // ms
    float curDelay; // for modulation
    int offset; // curDelay's offset into tape

    float amount = 0;  // feedback must be < 1.0 !!

    int amtAmp, timeAmp;
    float amtRange, timeRange;
    float amtBase, timeBase;
    boolean modAmt, modTime;

    public Echo() {
        super();
        setMaxDelay(1000);
    }

    int howManySamples(float ms) {
        return (int)ceil(
            (ms / 1000) * synth.getSampleRate()
        );
    }

    void setMaxDelay(float ms) {
        maxDelay = ms;
        tape = new int[howManySamples(ms)];
    }

    void setCurDelay(float ms) {
        ms = min(abs(ms), maxDelay);
        curDelay = (int) ms;
        offset = howManySamples(ms);
    }

    void modAmt() {
        if (!modAmt) return;
        float amt = amtRange * namedInputSum("amount") / amtAmp;
        if (amt + amtBase == amount) return;
        amount = amt + amtBase;
    }

    void modTime() {
        if (!modTime) return;
        float time = timeRange * namedInputSum("time") / timeAmp;
        if (time + timeBase == curDelay) return;
        setCurDelay(time + timeBase);
    }

    @Override
    public int currentValue() {
        modAmt(); modTime();

        int x0 = inputSum();
        int y0 = (int)(x0 + tape[t0]*amount);
        tape[(t0 + offset) % tape.length] = y0;
        t0 = (t0 + 1) % tape.length;
        return (int)(levelScale * y0);
    }

    // // // //

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components); // set outputs

        //  set amount (percentage) and time (ms)
        Float fltInp = getFloat(config.get("amount"),
            "Echo amount must be a decimal number.");
        if (fltInp != null) amtBase = fltInp;

        //  Max delay is the sum of the "time" input  plus
        //  the "input-time" modulation below

        fltInp = getFloat(config.get("time"),
            "Echo time must be a decimal number");
        if (fltInp != null) timeBase = fltInp;
        else timeBase = 1000; // default: 1 sec

        Object[] modInp = getInAmpPair(config, "input-amount", "percent");
        if (modInp != null) {
            amtAmp = (int) modInp[0];
            amtRange = (float) modInp[1];
            modAmt = true;
        }
        modInp = getInAmpPair(config, "input-time", "ms");
        if (modInp != null) {
            timeAmp = (int) modInp[0];
            timeRange = (float) modInp[1];
            modTime = true;
        }

        setMaxDelay( timeBase + timeRange );


        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() {
        Arrays.fill(tape,0);
    }

}
