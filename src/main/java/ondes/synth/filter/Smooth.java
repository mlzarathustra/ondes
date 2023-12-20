package ondes.synth.filter;

import static java.lang.Math.*;

import ondes.synth.component.MonoComponent;

import static java.lang.System.out;
import static java.lang.System.err;
import static ondes.synth.component.ConfigHelper.*;

import java.util.*;

/**
 *    smoothing similar to that used for the envelopes.
 */
public class Smooth extends MonoComponent {

    double y0;
    float levelScale = 1f;
    float cool = .4f;  // signal is too hot otherwise!
    double compScale = 1;
    double k=.01, kInv = 1.0/k;

    int amtInputAmp;
    double amount = 1, amtInputRange;
    boolean modAmt = false;

    @Override
    public void pause() { }

    @Override
    public void resume() { y0 = 0; }

    @Override
    public int currentValue() {
        if (modAmt) modAmt();

        int inp = inputSum();
        return currentValue(inp);
    }


    // // // //

    void setK(double inp) {
        kInv = (amount + inp * inp);
        k = 1.0 / kInv;
        compScale = 1 + kInv/2.5;
        // empirically, about the best match
    }

    private void modAmt() {
        double inp = namedInputSum("range");
        inp = amtInputRange * (inp / amtInputAmp);
        setK(inp);
    }

    private int currentValue(int inp) {
        if (modAmt) modAmt();

        double delta = inp - y0;
        y0 = y0 + (signum(delta)*k + delta*k);
        if ((y0 < inp && delta<0) || (y0>inp && delta>0)) y0 = inp;

        return (int)(levelScale * y0 * compScale);
    }

    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);

        Double dblInp = getDouble(config.get("amount"),
            "Smooth: 'amount' must be a decimal number.");

        if (dblInp != null) {
            amount = abs(dblInp);
        }
        if (amount < 1) {
            err.println("Smooth amount cannot be <1. Setting to 1");
            amount = 1;
        }
        setK(0);

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

        Object[] modInp = getInAmpPair(config, "input-amount", "range");
        if (modInp != null) {
            amtInputAmp = (int) modInp[0];
            amtInputRange = (float) modInp[1];
            modAmt = true;
        }
    }



    static String type="sine";
    static double fakeSampleRate = 40000;

    static void fillInput(int[] input, double freq) {
        double period = (fakeSampleRate / freq);
        for (double i=0; i<input.length; ++i) {

            double phase = (i%period) / period;
            switch(type) {
                case "square":
                    input[(int)i] =  (phase < .5) ? -1000 : 1000;
                    break;

                case "sine":
                    input[(int)i] = (int)(1000 * sin(phase * 2* PI));
                    break;
            }

        }
    }


    public static void main(String[] args) {
        Smooth smooth = new Smooth();
        boolean SHOW_ARRAY = false,
                SHOW_GRAPH_DATA = false,
                SHOW_AVG_AMP = true;


        int len = 5000;

        int[] input = new int[len];
        int[] output = new int[len];

        List<Double> kInvs = new ArrayList<>(), avgs = new ArrayList<>();


        out.println("amps=[];");
        for (double modAmt = 0; modAmt<=8; modAmt++) {
            smooth.setK(modAmt);

            List<Double> amps = new ArrayList<>();
            for (double freq = 25; freq < fakeSampleRate; freq += 100) {
                fillInput(input, freq);

                for (int i = 0; i < input.length; ++i) {
                    output[i] = smooth.currentValue(input[i]);
                }
                double max = Arrays.stream(output).max().getAsInt();
                //out.println(" freq: "+freq+" amp: "+max);

                amps.add(max);
            }

            double avgAmp = (amps.stream().mapToDouble(d -> d).sum() / amps.size());

            //out.println("modAmt: " + modAmt +" average amplitude: " + avgAmp + " 1000/avg = "+(1000.0 / avgAmp));
            if (SHOW_AVG_AMP) {
                out.print("[" + smooth.kInv + "," + (1000.0 / avgAmp) + "],");
                kInvs.add(smooth.kInv);
                avgs.add(1000.0 / avgAmp);
            }

            if (SHOW_GRAPH_DATA) {
                out.println("% modAmt: " + modAmt + "  kInv: " + ((int) smooth.kInv));
                out.println("%    average amplitude: " + (amps.stream().mapToDouble(d -> d).sum() / amps.size()));
                out.println("amps=[amps, " + amps + "'];");
            }
        }
        if (SHOW_GRAPH_DATA) out.println("plot(amps);");

        //smooth.showDKM();

        if (SHOW_ARRAY) {
            out.println("input=" + Arrays.toString(input) + ";");
            out.println("output=" + Arrays.toString(output) + ";");
        }

        if (SHOW_AVG_AMP) {
            out.println();
            out.println("kInvs="+kInvs+";");
            out.println("avgs="+avgs+";");
        }





    }



}
