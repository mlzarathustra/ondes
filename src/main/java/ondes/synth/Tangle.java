package ondes.synth;

import java.util.function.IntSupplier;

/**
 * A linked list of Wires,
 * doing what they always do!
 * <br/><br/>
 *
 *
 *
 * @see ondes.synth.Wire
 */
public class Tangle {

    Wire head, tail;

    void add(Wire w) {
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

    void remove(Wire w) {
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
    void remove(IntSupplier in) {
        Wire w=head;
        while (w != null) {
            Wire next = w.next;
            if (w.in == in) {
                remove(w);
            }
            w = next;
        }
    }

    void update() {
        Wire w=head;
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
