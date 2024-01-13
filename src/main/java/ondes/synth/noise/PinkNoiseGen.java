package ondes.synth.noise;

import ondes.synth.wave.WaveGen;
import static java.lang.Math.*;

import java.util.Random;

public class PinkNoiseGen extends WaveGen {

    static final Random rnd = new Random();

    int[] holdLengths=
        { 1, 23 };
    // I was experimenting with longer holds to add lower frequencies, but
    // they turn out to add also this kind of high static.
         //{2000, 1000, 500, 250, 175}; // mostly low with high static; irritating!
         //{ 2003, 1009, 503, 251, 173}; // prime numbers. similar, but more hollow

    int[] latch=new int[holdLengths.length];

    int lastValue=0;

    boolean LIMIT = true;

    @Override
    public int currentValue() {
        for (int i=0; i<holdLengths.length; ++i) {
            if (synth.getInstant().getSampleNumber() % holdLengths[i] == 0) {
                int rndInt = rnd.nextInt(2 * getAmp());

                if (signed) rndInt -= getAmp();
                latch[i] = rndInt;
            }
        }

        int rs = 0;
        for (int value : latch) rs += value;

        //out.println(" rs="+rs);

        //System.out.println("noise cv:"+rs);
        int curValue = rs / holdLengths.length;
        float diff = curValue - lastValue;

        if (LIMIT) {
            lastValue += (int) ( max(abs(diff), 3) * signum(diff));
            //lastValue += (int) ( ( diff / 20 ));
        }
        else lastValue += diff;

        return lastValue*3;


    }


}
