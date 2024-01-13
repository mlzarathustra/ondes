package ondes.tools;

import ondes.SynthSession;

import ondes.synth.wave.WaveEditor;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;
import static ondes.App.quitOnError;

public class SynthConnection {
    Waves waves;
    public void setWaves(Waves w) { waves = w; }

    boolean changed = false;
    class ChangeWatcher extends Thread {
        boolean stop = false;
        public void run() {
            for (;;) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (Exception ex) {
                    //  shouldn't reach
                    out.println("wave editor ChangeWatcher interrupted");
                }

                if (stop) return;

                while (changed) {
                    changed = false;
                    WaveEditor.setWave(waves.getHarmonics());
                }
            }
        }
    }

    final ChangeWatcher changeWatcher = new ChangeWatcher();

    public static final int BUF_SIZE = 2048;
    boolean DB=false;
    SynthSession session;

    SynthConnection(String[] args) {
        String[] progNames = new String[16];
        for (int i=0; i<16; ++i) progNames[i] = "wave-editor";
        String inDevStr="", outDevStr="";

        // Works for most voices. May need to be longer for some.
        int bufferSize=BUF_SIZE;

        //List<String> argList = Arrays.asList(args);
        for (int i=0; i<args.length; ++i) {
            switch (args[i]) {
                case "-in":
                    inDevStr = args[++i];
                    continue;
                case "-out":
                    outDevStr = args[++i];
                    continue;

                case "-buffer-size":
                    bufferSize = Integer.parseInt(args[++i]);
                    continue;

                default:
                    out.println("unknown option: "+args[i]+"; skipping.");
            }
        }
        session = new SynthSession(inDevStr, outDevStr, progNames, bufferSize);
        if (!session.open()) quitOnError();
        changeWatcher.start();
    }

    void start() { session.start(); }
    void close() { session.close(); }

    void registerChange() {
        synchronized(changeWatcher) {
            changed = true;
            changeWatcher.notify();
        }
    }

    void addWave(WaveController wc) {
        if (DB) out.println("adding wave "+wc);
        registerChange();
    }

    void setWaveName(int id, String wn) {
        if (DB) out.println("wave name set to "+wn);
        registerChange();
    }

    void setHarmonic(int id, int h) {
        if (DB) out.println("harmonic set to "+h);
        registerChange();
    }

    void setLevel(int id, float l) {
        if (DB) out.println("level set to "+l);
        registerChange();
    }

}
