package ondes.synth.effect;

import ondes.synth.component.ModParam;
import ondes.synth.component.MonoComponent;

import java.util.Arrays;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.System.err;
import static java.lang.System.out;
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

    public Echo() {
        super();
    }

    int howManySamples(double ms) {
        return (int)ceil(
            (ms / 1000) * synth.getSampleRate()
        );
    }

    void setMaxDelay(float ms) {
        maxDelay = ms;
        tape = new int[howManySamples(ms)];
    }

    /**
     * <p>
     *     Set the delay to "ms" milliseconds, and queue
     *     the "tape" to be cleared after it fades out.
     * </p>
     * <p>
     *     IMPORTANT: you MUST set maxDelay before
     *     calling this.
     * </p>
     *
     * @param ms - milliseconds of delay to set up
     */
    void setCurDelay(float ms) {
        ms = min(abs(ms), maxDelay);
        out.println("setCurDelay: ms="+ms+"; maxDelay="+maxDelay);
        if (curDelay == ms) return;
        curDelay = (int) ms;
        offset = howManySamples(ms); // wait until after fade.
        //out.println("offset is "+offset);

        if (first) { first = false; return; }
        first = false;

        if (fadeUntil == 0) fadeAmount = 1;
        fadeUntil = synth.getInstant().getSeconds() + changeDecay;
    }

    //  To avoid the click, we fade out the existing effect
    //  signal then zero the buffer.
    double changeDecay = .05; // seconds
    double changeDecrement; // need sample rate, so set in config.
    double fadeUntil = 0;
    double fadeAmount = 0;

    boolean first; // if the buffer was just cleared, no fade.

    int fadeEffect() {
        if (synth.getInstant().getSeconds() > fadeUntil) {
            fadeUntil = 0;
            Arrays.fill(tape,0);
            offset = howManySamples(curDelay);
            return inputSum();
        }
        if (fadeAmount > 0) fadeAmount -= changeDecrement;
        if (fadeAmount < 0) fadeAmount = 0;

        int x0 = inputSum();
        int y0 = (int)(x0 + tape[t0] * amtParam.getCurrent() * fadeAmount);
        t0 = (t0 + 1) % tape.length;
        return (int)(levelScale * y0);
    }

    @Override
    public int currentValue() {
        amtParam.mod(); timeParam.mod();
        if (fadeUntil > 0) return fadeEffect();

        int x0 = inputSum();
        int y0 = (int)(x0 + tape[t0]* amtParam.getCurrent());
        tape[(t0 + offset) % tape.length] = y0;
        t0 = (t0 + 1) % tape.length;
        return (int)(levelScale * y0);
    }

    // // // //

    ModParam amtParam, timeParam;

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components); // set outputs
        //setMaxDelay(1000); // default shouldn't be needed

        amtParam = new ModParam(config, "amount", "percent", 0,
            this::namedInputSum);
        amtParam.setScale(1.0f / 100);

        timeParam = new ModParam(config, "time", "ms", 1000,
            this::namedInputSum,
            this::setCurDelay);

        setMaxDelay( timeParam.getBase() + timeParam.getRange() );

        // important! MUST set maxDelay first,
        // because timeParam.mod() requires it.
        timeParam.mod();

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        Float fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            else levelScale = fltInp;
        }

        changeDecrement = 1.0 / (changeDecay * synth.getSampleRate());
    }

    @Override
    public void pause() { }

    @Override
    public void resume() {
        Arrays.fill(tape,0);
        first = true;
    }

}
