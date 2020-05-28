package ondes.mlz;


import org.testng.annotations.Test;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.System.out;
import static org.testng.Assert.assertEquals;


public class MaxTrackerTest {

    //  BRUTE version
//
    static class MadMax {
        int[]buf;
        int here=-1;

        MadMax(int cap) { buf=new int[cap]; }

        public void accept(int val) {
            here = (here + 1) % buf.length;
            buf[here] = abs(val);
        }
        public int getCurrentMax() {
            int max=Integer.MIN_VALUE;
            for (int i : buf) if (i>max) max=i;
            return max;
        }
    }

    @Test
    void testMaxTracker() {
        // 1/10 of a second at 44.1khz
        int bufSize = 4410;


        MaxTracker maxTracker = new MaxTracker(bufSize);
        MadMax madMax = new MadMax(bufSize);

        int sampleRate = 44100;
        double freq = 30;
        double amp = 3000;

        int duration = 3; // seconds

        for (double slice = 0; slice<duration * sampleRate; ++slice) {
            if (slice%7 == 0) amp--;

            int y = (int) ( sin( freq * slice/sampleRate ) * amp );

            maxTracker.accept(y);
            madMax.accept(y);

            //out.print(maxTracker.getCurrentMax()+" ");

            if (madMax.getCurrentMax() != maxTracker.getCurrentMax()) {
                out.println("ERROR! slice="+slice+"; madMax="+madMax.getCurrentMax()+
                    "; maxTracker="+maxTracker.getCurrentMax()+
                    "; y="+y);
            }

            assertEquals(madMax.getCurrentMax(), maxTracker.getCurrentMax());
        }
    }
}
