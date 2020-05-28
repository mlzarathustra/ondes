package ondes.mlz;


import java.util.TreeMap;

import static java.lang.Math.abs;

/**
 * <p>
 *     Tracks the maximum absolute value
 *     over the wave segment.
 * </p>
 * <p>
 *     Using the TreeMap makes it run about
 *     3x as fast as the brute for the cases
 *     I tried.
 * </p>
 */
public class MaxTracker {

    TreeMap<Integer,Integer> bufMap =
        new TreeMap<>((a, b) -> b-a);
    int[] buf;

    public MaxTracker(int cap) {
        buf = new int[cap];
    }

    int here =-1;
    boolean first = true;

    public void accept(int val) {
        val = abs(val);
        if (here == buf.length - 1) first = false;

        here = (here + 1) % buf.length;
        if (!first) evict(buf[here]);
        buf[here] = val;
        add(val);

    }
    public int getCurrentMax() { return bufMap.firstKey(); }

    void add(int val) {
        bufMap.put(val, bufMap.getOrDefault(val, 0)+1);
    }
    void evict(int val) {
        int n=bufMap.getOrDefault(val,0);
        if (n<=1) bufMap.remove(val);
        else bufMap.put(val,n-1);
    }

}
