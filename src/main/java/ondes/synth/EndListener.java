package ondes.synth;

import javax.sound.midi.MidiMessage;

/**
 * Listens for the end of a note
 * (when it's done sounding)
 *
 */
public interface EndListener {
    default void noteEnded(MidiMessage msg) {
        int chan = msg.getStatus() & 0xf;
        int note = msg.getMessage()[1];
        noteEnded(chan, note);
    }
    void noteEnded(int chan, int note);
}
