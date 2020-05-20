package ondes.midi;

import javax.sound.midi.*;
import static java.lang.System.out;
import java.util.*;

public class MIDIInfo {
    static boolean CONDENSED=true;

    static void showInfo(MidiDevice.Info info) {
        try {
            MidiDevice midiDev = MidiSystem.getMidiDevice(info);
            midiDev.open();
            List<Transmitter> transList=midiDev.getTransmitters();
            Receiver rcvr=null;

            // calling midiDev.getReceiver() here causes the recvList size
            // to be 1 instead of 0
            try {
                rcvr = midiDev.getReceiver();
            }
            catch (Exception ex) {
                out.println("error getting rcvr");
            }

            List<Receiver> recvList=midiDev.getReceivers();

            if (CONDENSED) {
                out.println(
                    info.getDescription() + "; " +
                    info.getName() + "; " +
                    info.getVendor() + "; " +
                    info.getVersion() + "\n" +
                    //"isOpen(): " + midiDev.isOpen() + "\n" +

                    "Maximum # of Transmitters: " + midiDev.getMaxTransmitters() + "; " +
                    "Open Transmitters: " + transList.size() + "\n" +

                    "Maximum # of Receivers: " + midiDev.getMaxReceivers() + "; " +
                    "Open Receivers: " + recvList.size() + "; " +
                        ((midiDev instanceof Synthesizer)?"Synthesizer; ":"") +
                        ((midiDev instanceof Sequencer)?"Sequencer; ":"") +
                        "\n"+


                    //"rcvr: " + rcvr + "; " +
                    " -- "
                );

            }
            else {

                out.println(

                    "Description: " + info.getDescription() + "\n" +
                        "Name: " + info.getName() + "\n" +
                        "Vendor: " + info.getVendor() + "\n" +
                        "Version: " + info.getVersion() + "\n" +
                        "isOpen(): " + midiDev.isOpen() + "\n" +

                        "Maximum # of Transmitters: " + midiDev.getMaxTransmitters() + "\n" +
                        "Open Transmitters: " + transList.size() + "\n" +

                        "Maximum # of Receivers: " + midiDev.getMaxReceivers() + "\n" +
                        "Open Receivers: " + recvList.size() + "\n" +

                        "rcvr: " + rcvr + "\n" +
                        "is instanceof Synthesizer: " + (midiDev instanceof Synthesizer) + "\n" +
                        "is instanceof Sequencer: " + (midiDev instanceof Sequencer) + "\n" +

                        " ------------- ");
            }

            midiDev.close();
        }
        catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();
        
        for (MidiDevice.Info info : infoList) {
            if (args.length==0 || info.getName().contains(args[0])) {
                showInfo(info);
            }
        }
    }
}
