package ondes.synth.wire;

import static java.lang.Math.*;

import ondes.synth.component.MonoComponent;

import static java.lang.System.out;
import static java.lang.System.err;
import static ondes.synth.component.ConfigHelper.*;

import java.util.Arrays;
import java.util.Map;

/**
 *   Attempt to smooth waves by limiting the delta
 *   in angle of the line.
 *
 *   Sounds like junk, but draws some pretty curves
 *   sometimes.
 *
 */
public class Smooth extends MonoComponent {

    double y0, y1, y2;
    double wLimit;

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        y0 = y1 = y2 = 0;
    }

    @Override
    public int currentValue() {
        int inp = inputSum();
        if (wLimit == 0) return inp;
        return currentValue(inp);
    }

    private int currentValue(int inp) {
        double w1 = atan(y1 - y2);
        double wInp = atan(inp - y1) - w1;
        if (abs(wInp) > wLimit) {
            if (inp == y1) {
                y0 = y1 + tan(w1 - signum(w1) * wLimit);
            }
            else {
                y0 = y1 + tan(w1 + (wLimit * signum(inp - y1)));
            }
        }
        else y0 = inp;

        out.print(String.format("inp=%4d  w1=%8.4f  wInp=%8.4f  wLimit=%8.4f  ", inp, w1, wInp, wLimit));
        out.println(String.format("     y0=%8.4f y1=%8.4f y2=%8.4f", y0, y1, y2));

        y2 = y1;
        y1 = y0;

        return (int) y0;
    }


    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Double dblInp = getDouble(config.get("limit"),
            "Smooth: 'limit' must be a decimal number.");

        if (dblInp != null) wLimit = abs(dblInp);
        err.println("limit is "+wLimit);
    }





    public static void main(String[] args) {
        Smooth smooth = new Smooth();
        smooth.wLimit = PI / 20.0;

        int[] input = new int[50];
        int[] output = new int[50];
        Arrays.fill(input, 0, 25, -100);
        Arrays.fill(input, 25, 50, 100);

        for (int i=0; i<input.length; ++i) {
            output[i] = smooth.currentValue(input[i]);
        }

        out.println("input="+Arrays.toString(input)+";");
        out.println("output="+Arrays.toString(output)+";");





    }



}
