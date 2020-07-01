package ondes.synth.wire;

import static java.lang.Math.*;

import ondes.synth.component.MonoComponent;

import static java.lang.System.out;
import static java.lang.System.err;
import static ondes.synth.component.ConfigHelper.*;

import java.util.Arrays;
import java.util.Map;

/**
 *    smoothing similar to that used for the envelopes.
 */
public class Smooth extends MonoComponent {

    double y0;
    float levelScale = 1f;

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        y0 = 0;
    }

    @Override
    public int currentValue() {
        int inp = inputSum();
        return currentValue(inp);
    }


    // // // //


    double k=.01;

    private int currentValue(int inp) {
        double delta = inp - y0;
        y0 = y0 + (signum(delta)*k + delta*k);
        if ((y0 < inp && delta<0) || (y0>inp && delta>0)) y0 = inp;

        return (int)(levelScale * y0);
    }




    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Double dblInp = getDouble(config.get("amount"),
            "Smooth: 'k' must be a decimal number.");

        if (dblInp != null) k = 1.0 / abs(dblInp);
        //err.println("k is "+k);

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



    }





    public static void main(String[] args) {
        Smooth smooth = new Smooth();

        int[] input = new int[50];
        int[] output = new int[50];
        Arrays.fill(input, 0, 25, -100);
        Arrays.fill(input, 25, 50, 100);

        for (int i=0; i<input.length; ++i) {
            output[i] = smooth.currentValue(input[i]);
        }

        //smooth.showDKM();

        out.println("input="+Arrays.toString(input)+";");
        out.println("output="+Arrays.toString(output)+";");





    }



}
