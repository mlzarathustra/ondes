package ondes.synth;

import javax.sound.midi.MidiMessage;
import java.util.*;
import static java.lang.System.out;

public class MidiListenerThread extends Thread {
    OndesSynth synth;
    MidiListenerThread(OndesSynth synth) {
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
                    //out.println(" MidiListenerThread wait() ended");
                }
            } catch (InterruptedException ex) {
                //  with notify, we shouldn't reach this.
                out.println("MidiListenerThread interrupted");
            }
            if (stop) return;

            //  process queue
            //out.println( " MidiListener.queue: "+queue);
            while (!queue.isEmpty()) {
                MidiMessage msg;
                synchronized(this) {
                    //out.println(" MidiListenerThread.run() queue[A]: "+queue);
                    msg = queue.pop();
                }
                //out.println(" MidiListenerThread.run() queue[B]: "+queue);
                synth.routeMidiMessage(msg,0);
            }
        }
    }
}





