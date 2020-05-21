package ondes.synth;

public class Snake {

    Wire head, tail;

    void add(Wire w) {
        if (head == null) {
            head = tail = w;
        }
        else {
            tail.next = w;
            w.prev=tail;
            tail = w;
        }
    }

    void remove(Wire w) {
        if (w == head) head = tail = null;
        else {
            w.prev.next = w.next;
            w.next.prev = w.prev;
            w.prev = w.next = null;
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
