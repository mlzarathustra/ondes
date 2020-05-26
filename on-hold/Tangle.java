package ondes.synth.wire;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A linked list of Wires,
 * doing what they always do!
 *y
 * @see Junction
 */
public class Tangle {

    Junction head, tail;

    public void add(Junction w) {
        if (head == null) {
            head = tail = w;
            w.prev = w.next = null;
        }
        else {
            tail.next = w;
            w.prev=tail;
            tail = w;
        }
    }

    void remove(Junction w) {
        if (w == null) return;
        if (w == head) {
            head = w.next;
            head.prev = null;
        }
        else {
            // w.prev != null because it's not the head
            w.prev.next = w.next;
            if (w.next != null) w.next.prev = w.prev;
        }
        w.prev = w.next = null;
    }

    //  remove all with this supplier
    //
    public void remove(IntSupplier in) {
        Junction w=head;
        while (w != null) {
            Junction next = w.next;
            if (w.in == in) {
                remove(w);
            }
            w = next;
        }
    }
    //  remove all with this consumer
    //
    public void remove(IntConsumer out) {
        Junction w=head;
        while (w != null) {
            Junction next = w.next;
            if (w.out == out) {
                remove(w);
            }
            w = next;
        }
    }

    public void update() {
        Junction w=head;
        while (w != null) {
            w.setVisited(false);
            w=w.next;
        }

        w = head;
        while (w != null) {
            w.update();
            w=w.next;
        }
    }

}
