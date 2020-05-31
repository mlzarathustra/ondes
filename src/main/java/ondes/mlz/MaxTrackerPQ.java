package ondes.mlz;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.System.out;

/**
 * <p>
 *     Tracks the maximum absolute value over the wave segment.
 *     Priority Queue version.
 * </p>
 * <p>
 *     Outperforms the TreeMap version - shaves off about 1/3
 *     of elapsed time.
 * </p>
 *  */
public class MaxTrackerPQ {

    static class Pair implements Comparable { int n, count;
        Pair(int n, int count) { this.n=n; this.count=count; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            return ((Pair)o).n == n;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(n);
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Pair) {
                return ((Pair) o).n - n;  // it's a Max queue
            }
            return -1;
        }

        @Override
        public String toString() {
            return "{ n="+n+", count="+count+" } ";
        }
    }

      //      ///  // /// //   // ///            /// //
       // ///   // ///              /// // /// // ///  // ///


    PriorityQueueMap<Pair> bufMap = new PriorityQueueMap<>(Pair::compareTo);
    int[] buf;

    int here =-1;
    boolean first = true;


    public MaxTrackerPQ(int cap) {
        buf = new int[cap];
        reset();
    }

    public void reset() {
        here = -1;
        first = true;
    }

    public void accept(int val) {
        if (here == buf.length - 1) first = false;
        here = (here + 1) % buf.length;
        if (!first) evict(buf[here]);
        buf[here] = val;
        add(val);

    }
    public int getCurrentMax() {
        return bufMap.peekFirst().n;
    }

    Pair dummy = new Pair(0,0);
    void add(int val) {
        val = abs(val);
        dummy.n=val;
        Pair pr = bufMap.get(dummy);
        if (pr == null) bufMap.add(new Pair(val,1));
        else pr.count++;
    }

    void evict(int val) {
        val = abs(val);
        dummy.n=val;
        Pair pr = bufMap.get(dummy);
        if (pr == null) return;
        if (pr.count == 1) bufMap.remove(pr);
        else pr.count--;
    }



}

                //                           //                //
        //       // //                // // /////////   // //
     // //  /   //////   * .    * ***  // /////// // / .    .    .
          //           //         .      *      //               //    //  //  .



























