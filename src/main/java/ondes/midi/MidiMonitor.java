package ondes.midi;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.System.out;
import static ondes.midi.MlzMidi.getTransmitter;
import static ondes.mlz.Util.getResourceAsString;

public class MidiMonitor {

    static void usage() {
        out.println(getResourceAsString("usage/MidiMonitor.txt"));
    }

    public static void main(String[] args) {
        String device = "";
        if (args.length>0) device = args[0];

        usage();

        monitor(device, new Receiver() {
            public void close() {};
            public void send(MidiMessage msg, long ts) {
                out.println(ts+" : "+ MlzMidi.toString(msg));
            }
        });
    }


    static void monitor(String devId, Receiver recv) {
        //String devId="828";

        MidiDevice.Info info = getTransmitter(devId);
        if (info == null) {
            out.println(
                "could not find midi transmitting device to match "
                    +devId);
            System.exit(-1);
        }

        MidiDevice dev;
        Transmitter trans;
        try {
            dev = MidiSystem.getMidiDevice(info);
            trans = dev.getTransmitter();

            out.println("Opened device: "+trans);
            out.println("Listening for MIDI messages.");

            trans.setReceiver(recv);
            dev.open();

            BufferedReader in=new BufferedReader(
                new InputStreamReader(System.in));

            out.println("Press [Enter] to quit.");
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
