package ondes.mlz;

import org.testng.annotations.Test;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.System.out;
import static org.testng.Assert.*;

public class MaxTrackerPQTest {

    @Test
    public void testMain() {
        PriorityQueueMap<MaxTrackerPQ.Pair> Q = new PriorityQueueMap<>(MaxTrackerPQ.Pair::compareTo);

        Q.add(new MaxTrackerPQ.Pair(3,100));
        Q.add(new MaxTrackerPQ.Pair(5,101));
        Q.add(new MaxTrackerPQ.Pair(1,102));
        Q.add(new MaxTrackerPQ.Pair(7,103));
        Q.add(new MaxTrackerPQ.Pair(4,104));

        String ex1 = "[{ n=7, count=103 } , { n=5, count=101 } , " +
            "{ n=1, count=102 } , { n=3, count=100 } ," +
            " { n=4, count=104 } ]";
        assertEquals(Q.list.toString(), ex1);

        Q.remove(new MaxTrackerPQ.Pair(1,0));
        Q.remove(new MaxTrackerPQ.Pair(4,0));
        Q.add(new MaxTrackerPQ.Pair(8,0));

        assertEquals(Q.removeFirst().n, 8);
        assertEquals(Q.removeFirst().n, 7);
        assertEquals(Q.removeFirst().n, 5);
        assertEquals(Q.removeFirst().n, 3);

        assertNull(Q.removeFirst());

    }


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

        MaxTrackerPQ maxTracker = new MaxTrackerPQ(bufSize);
        MaxTrackerPQTest.MadMax madMax = new MaxTrackerPQTest.MadMax(bufSize);

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