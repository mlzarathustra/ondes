package ondes.mlz;

import org.testng.annotations.Test;

import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.System.out;
import static org.testng.Assert.*;

public class SineLookupTest {

//    @Test
//    public void testSineLookup() {
//    }

    static void f(double x) { // make sure it's using the value
    }

    // stress test sin function vs. lookup table
    // the lookup takes a little more than half the time.
    //
    static void t6(DoubleUnaryOperator fn) {
        long start = System.nanoTime();
        for (int rpt = 0; rpt < 1e5; ++rpt) {
            for (double theta = 0; theta < 2 * PI; theta += 0.001) {
                f(fn.applyAsDouble(theta));
            }
        }

        long end = System.nanoTime();
        out.println("Elapsed: "+((float)(end-start))/1e9 + " seconds");
    }

    // compare results: sin function with lookup table
    static void t7(DoubleUnaryOperator fn1, DoubleUnaryOperator fn2) {
        long start = System.nanoTime();
        for (double theta = 0; theta < 2 * PI; theta += 0.001) {
            double diff = fn1.applyAsDouble(theta) - fn2.applyAsDouble(theta);
            //out.println(" diff: "+diff);
            double MAX_DIFF=2e-5;
            assertTrue( diff < MAX_DIFF, "Difference < "+MAX_DIFF);
        }

        long end = System.nanoTime();
        out.println("Elapsed: "+((float)(end-start))/1e9 + " seconds");
    }



    static int tableSize = 512_000;
    static double[] sineLookupTable = new double[tableSize];
    static double TAO=2*PI;

    static {
        for (int i = 0; i < tableSize; ++i) {
            double theta = TAO * ((double)i)/tableSize;
            sineLookupTable[i] = sin(theta);
        }
    }

    static double sineLookup(double theta) {
        double phase = (theta % TAO)/TAO;
        int idx = (int)(phase * tableSize);
        return sineLookupTable[idx];
    }

    //  Linear antialiasing doesn't seem to significantly
    //  improve accuracy.
    //
    static double sineLookupWithAlias(double theta) {
        double phase = (theta % TAO)/TAO;
        double idx = (int) (phase * tableSize);
        double left = idx % 1.0;

        double here = sineLookupTable[(int)idx];
        if (idx > sineLookupTable.length - 1) return here;
        double next = sineLookupTable[1+(int)idx];
        return here + (left * (next - here));
    }

    @Test
    void skipLongTest() {
        out.println("To run the sine lookup test (long) " +
            "uncomment the @Test annotation \nfor the t8() function " +
            "in ondes.mlz.SineLookupTest.");
    }

    //@Test
    void t8() {
        out.println("using function");
        t6(Math::sin);
        out.println("using lookup");
        t6(SineLookupTest::sineLookup);

        t7(Math::sin, SineLookupTest::sineLookup);
    }


}