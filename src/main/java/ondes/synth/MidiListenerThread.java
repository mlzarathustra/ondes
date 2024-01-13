package ondes.synth;

import javax.sound.midi.MidiMessage;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.System.out;

public class MidiListenerThread extends Thread {
    OndeSynth synth;

    MidiListenerThread(OndeSynth synth) {
        super("OndeSynth - MidiListenerThread");
        this.synth = synth;
    }

    private final Queue<MidiMessage> queue = new ConcurrentLinkedQueue<>();
    boolean stop=false;

    public void routeMidiMessage(MidiMessage msg) {
        //out.println("MidiListenerThread.queue.add() "+ MlzMidi.toString(msg));
        queue.add(msg);
        synchronized (this) {
            this.notify();
        }
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
                synth.routeMidiMessage(queue.poll(),0);
            }
        }
    }
}





