package ondes.mlz;

import java.util.*;

/**
 * <p>
 *     A Heap represented as a List cross-referenced by a Map.
 * </p>
 * <p>
 *     It should give O(log n) for remove(E a)
 *     and O(1) for get(E a).
 * </p>
 * <p>
 *    Without the Map, it has to search the whole List,
 *    so it would be O(n + log n) and O(n) respectively.
 * </p>
 * <p>
 *     It is cast as a "MIN QUEUE" but by inverting the
 *     comparator, it can be a "MAX QUEUE."
 *
 * </p>
 */

public class PriorityQueueMap<E> implements Iterable<E> {

    //  Maps from E to the position of E in the list
    Map<E, Integer>          map=new HashMap<>();

    List<E>                  list=new ArrayList<>();
    Comparator<? super E>    comparator;

    // //////////// //////  //// //

    /**
     * <p>
     *     If E is comparable, and you want to use its
     *     comparator, you'll need to provide it here.
     * </p>
     *
     * @param comparator - how we determine the lesser
     */
    public PriorityQueueMap(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    public int parent(int i) { return (i-1)/2; }
    public int left(int i) { return 2*i+1;}
    public int right(int i) { return 2*i+2; }

    public PriorityQueueMap<E> add(E val) {
        list.add(val);
        map.put(val, list.size()-1);
        bubbleUp(list.size() - 1);

        return this; // syntactic sugar for chained adds
    }

    private void bubbleUp(int i) {
        if (i == 0) return;
        if ( isGreater( parent(i), i ) ) {
            swap(i, parent(i));
            bubbleUp(parent(i));
        }
    }
    private void trickleDown(int parent) {
        int left = left(parent), right = right(parent);
        if (isGreater(parent, left) || isGreater(parent, right)) {
            if (isGreater(left, right)) {
                swap(parent, right);
                trickleDown(right);
            } else {
                swap(parent, left);
                trickleDown(left);
            }
        }
    }

    private void swap(int i, int j) {
        E ti=list.get(i);
        list.set(i,list.get(j));
        list.set(j,ti);

        map.put(list.get(i), i);
        map.put(list.get(j), j);
    }

    //  is a > b? using the Comparator supplied.
    private boolean isGreater(E a, E b) {
        if (a==null || b==null) return false;
        return comparator.compare(a,b) > 0;
    }

    // compares two elements of the list safely
    // whose indexes are given by a and b
    private boolean isGreater(int a, int b) {
        if (a>list.size()-1 || b>list.size()-1) return false;
        return comparator.compare(list.get(a), list.get(b)) > 0;
    }

    public boolean isEmpty() { return list.size() == 0; }

    public E peekFirst() {
        if (list.size() == 0) return null;
        return list.get(0);
    }

    public E removeFirst() {
        if (list.size() == 0) return null;
        E rs = list.get(0);
        list.set(0, list.get(list.size() - 1));
        list.remove(list.size() - 1);
        map.remove(rs);
        if (list.size() > 0) {
            map.put(list.get(0), 0);
            trickleDown(0);
        }
        return rs;
    }

    public E get(E a) {
        Integer idx = map.get(a);
        if (idx == null) return null;
        return list.get(idx);
    }

    // change priority
    public E replace(E cur, E rpl) {
        Integer i=map.get(cur);
        if (i==null) return null;
        list.set(i,rpl);
        map.put(rpl,i);
        if (isGreater(cur, rpl)) bubbleUp(i);
        else trickleDown(i);
        map.remove(cur);
        return cur;
    }

    public void remove(E a) {
        Integer idx=map.get(a);
        if (idx==null) return;

        if (idx == list.size() - 1) {
            map.remove(list.remove(list.size() - 1));
        }
        else {
            E last = list.get(list.size() - 1);
            map.remove(list.remove(list.size() - 1));
            replace(a, last);
        }

    }

    //  N.B. does not iterate in sort order.
    @Override
    public Iterator<E> iterator() { return list.iterator(); }
}
