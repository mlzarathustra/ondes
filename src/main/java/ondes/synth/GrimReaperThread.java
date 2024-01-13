package ondes.synth;


import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.System.out;

/**
 * Don't worry -- the voices don't actually die.
 * They will be reincarnated.
 *
 */
public class GrimReaperThread extends Thread {

    OndeSynth synth;
    GrimReaperThread(OndeSynth synth) {
        super("OndeSynth - GrimReaperThread");
        this.synth = synth;
    }

    private final Queue<List<Integer>> endedNoteQueue =
        new ConcurrentLinkedQueue<>();
    boolean stop = false;

    public synchronized void queueNoteEnd(int chan, int note) {
        endedNoteQueue.add(List.of(chan,note));
        this.notify();
    }

    public void endNotes() {
        while (!endedNoteQueue.isEmpty()) {
            List<Integer> n = endedNoteQueue.poll();
            synth.noteEnded(n.get(0), n.get(1));
        }
    }

    public void run() {
        out.println("Grim Reaper is listening :^D");

        for (;;) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ignore) { /* not reached */ }
            if (stop) return;
            endNotes();
        }
    }
}

