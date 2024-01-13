package ondes.mlz;
import java.util.*;

import static java.lang.Math.*;
import static java.lang.System.out;
/**
 * <p>
 *     playing with Helmholtz oscillation
 * </p>
 */
public class Helmholtz {
    public static final String fmt="%3.4f";
    static int seq=0;


    /**
     * <p>
     *      Yields a cosine wave.
     * </p>
     * <p>
     *      The formula for a wavelength "wl" :
     *      <pre>
     *          k = 160 / (wl * wl)
     *      </pre>
     * <p>
     *      Derived from comparison of curve plot(xrs,yrs) with y=x^2/10
     *      wl is ~(x + 3) where x is the first positive value of dy
     *      it could be a rounding error, as x is not exact.
     * </p>
     * <p>
     *     See the comments "curve matching" below.
     * </p>
     * <p>
     * @param k - the weight of delta y; to achieve wavelength wl,
     *          set k = (160 / wl**2) (see above)
     * @param y - Initial 'y,' which affects the amplitude,
     *            but not the period.
     * </p>
     *
     */
    public static void showHelmholtz(double k, double y, int length) {
        out.println("helmHoltz(" + k + ", " + y + ")");
        List<List<? extends Number>> xy = helmholtz(k, y, length);
        out.println("x"+seq+"="+xy.get(0));
        out.println("y"+seq+"="+xy.get(1));
        seq++;
        out.println();
    }

    /**
     *
     * @param k - related to wavelength somehow
     * @param y - affects amplitude only
     * @param length - how many points to calculate
     * @return - the x and y arrays (x being simply an int range)
     */
    static List<List<? extends Number>> helmholtz(double k, double y, int length) {

        List<Integer> xs=new ArrayList<>();
        List<Double> ys=new ArrayList<>();

        double dy = 0; // start at the top of the curve.

        for (int x=0; x<length; ++x) {
            dy -= y * k;
            y += dy;
            //out.println(String.format("x=%d y=%3.4f dy=%3.4f", x,y,dy));
            xs.add(x);
            ys.add(y);
        }

        return Arrays.asList(xs,ys);
    }

    static void t0() {
        showHelmholtz(0.001, 10, 200);  // period: ~200
        showHelmholtz(0.001, 20, 200);  // period: ~200
        showHelmholtz(0.0038, 20, 200);  //
    }

    @SuppressWarnings("unchecked")
    static void t1() {
        List<Integer> xrs = new ArrayList<>();
        List<Double> yrs = new ArrayList<>();

        for (int i = 40; i<10000; i+=40) {
            float k = 1.0f / i;

            double wl = sqrt(160.0/k); // curve matching

            List<List<? extends Number>> xy = helmholtz(k, 1000, 1000);

            List<Double> ys = (List<Double>)xy.get(1);
            for (int x=0; x<ys.size()-1; ++x) {
                if (ys.get(x) < ys.get(x+1)) {
                    out.println(  // curve matching
                        String.format("1/k=%2.6f ; dx zero x=~%d wl/4=%f",1.0f/k,x, wl/4.0));
                    xrs.add(x); yrs.add(1.0/k);
                    break;
                }
            }
        }
        out.println("xs="+xrs);
        out.println("ys="+yrs);

        //  This matches (x^2)/10
        //  plot(xs,ys,xs, xs.^2/10)

    }



    // The following in MatLab using the x and y printed from the output of
    // helmholtz(0.001,10) overlaid with cosine as below yields two nearly
    // identical curves:
    //
    //  plot(x,y, x,10 * cos(x/(10 * pi)))



    public static void main(String[] args) {
        t1();
    }

}
