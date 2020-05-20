package ondes;

import ondes.midi.MlzMidi;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.System.out;
import static ondes.midi.MlzMidi.getTransmitter;

public class Monitor {
    String devId;

    public static void main(String[] args) {
        out.println("Press [Enter] to quit.");
        new Monitor("828").monitor();
    }

    Monitor(String id) { devId = id; }

    void monitor() {
        String devId="828";

        MidiDevice.Info info = getTransmitter(devId);
        if (info == null) {
            out.println("could not find midi device to match "+devId);
            System.exit(-1);
        }

        MidiDevice dev = null;
        Transmitter trans = null;
        try {
            dev = MidiSystem.getMidiDevice(info);
            trans = dev.getTransmitter();

            out.println(trans);

            trans.setReceiver(new Receiver() {
                public void close() {};
                public void send(MidiMessage msg, long ts) {
                    out.println(ts+" : "+ MlzMidi.toString(msg));
                }
            });
            dev.open();

            BufferedReader in=new BufferedReader(
                new InputStreamReader(System.in));

            in.readLine();
            dev.close();
            System.exit(0);
        }
        catch (Exception ex) {
            out.println("attempting to open midi device "+devId);
            out.println(ex);
            System.exit(-1);
        }
    }

}
