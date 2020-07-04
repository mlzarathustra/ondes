package ondes.synth.effect;

import ondes.synth.component.ModParam;
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

//    float amount = 0;  // feedback must be < 1.0 !!
//
//    int amtAmp, timeAmp;
//    float amtRange, timeRange;
//    float amtBase, timeBase;
//    boolean modAmt, modTime;

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
/*
    // zero out everything from end to t0
    void zeroUnused() {
        int end = (t0 + offset + 1) % tape.length;
        if (end < t0) {
            for (int i=end; i<t0; ++i) tape[i] = 0;
        }
        else {
            for (int i=end; i<tape.length; ++i) tape[i] = 0;
            for (int i=0; i<t0; ++i) tape[i] = 0;
        }
    }

    void fillUnused(int newOffset) {
        float u = newOffset - offset;
        for (int i = 0; i < u; ++i) {
            float fade = (u-i)/u;

            tape[(t0 + offset +i + 1) % tape.length] =
                (int)(fade * tape[(t0 + i) % tape.length]);
        }
    }
*/

    /**
     * Set the delay to "ms" milliseconds,
     * and zero out the delay memory.
     *
     * It would be nice to avoid the "click"
     * but doing so without zippering when
     * there are a series of calls is tricky.
     *
     * @param ms - milliseconds of delay to set up
     */
    void setCurDelay(float ms) {
        ms = min(abs(ms), maxDelay);
        curDelay = (int) ms;
        int newOffset = howManySamples(ms);
        //if (newOffset > offset) fillUnused(newOffset);
        Arrays.fill(tape, 0);
        offset = newOffset;
    }

    @Override
    public int currentValue() {
        amtParam.mod(); timeParam.mod();

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

        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        Float fltInp = getFloat(config.get("level-scale"), levelScaleErr);
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
