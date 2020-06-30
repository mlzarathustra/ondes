package ondes.synth;


import java.util.ArrayList;
import java.util.List;

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

    private final List<List<Integer>> endedNoteQueue = new ArrayList<>();
    boolean stop = false;

    public synchronized void queueNoteEnd(int chan, int note) {
        endedNoteQueue.add(List.of(chan,note));
        this.notify();
    }

    public void endNotes() {
        int i=0;
        for (;;) {
            List<Integer> n;
            synchronized(this) {
                if (i >= endedNoteQueue.size()) break;
                n = endedNoteQueue.get(i);
                ++i;
            }
            synth.noteEnded(n.get(0), n.get(1));
        }
        endedNoteQueue.clear();
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

