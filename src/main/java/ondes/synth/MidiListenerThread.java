package ondes.synth;

import javax.sound.midi.MidiMessage;
import java.util.*;
import static java.lang.System.out;

public class MidiListenerThread extends Thread {
    OndeSynth synth;
    MidiListenerThread(OndeSynth synth) {
        super("OndeSynth - MidiListenerThread");
        this.synth = synth;
    }

    private final Deque<MidiMessage> queue = new ArrayDeque<>();
    boolean stop=false;

    public synchronized void routeMidiMessage(MidiMessage msg) {
        queue.add(msg);
        this.notify();
    }

    public void run() {
        out.println("MIDI Listener starting.");
        for (;;) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {
                //  with notify, we shouldn't reach this.
                out.println("MidiListenerThread interrupted");
            }
            if (stop) return;

            //  process queue
            while (!queue.isEmpty()) {
                MidiMessage msg;
                synchronized(this) {
                    msg = queue.pop();
                }
                synth.routeMidiMessage(msg,0);
            }
        }
    }
}





