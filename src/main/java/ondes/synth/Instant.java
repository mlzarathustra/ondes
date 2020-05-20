package ondes.synth;

import java.util.ArrayList;

interface DeltaListener {
    void update(Instant now);
}

public class Instant  {
    class PhaseClock {
        private double frequency;
        private double delta;
        private double phase;
        long lastUpdate;

        PhaseClock(double f) {
            frequency=f;
            setDelta();
        }

        void setDelta() {
            delta = frequency / sampleRate;
        }

        public void update() {
            while (lastUpdate < sampleNumber) {
                phase += delta;
                lastUpdate++;
            }
            while (phase >= 1) phase -= 1;
        }
        public double getPhase() { return phase; }
    }

    private int sampleRate;
    private long sampleNumber;

    private ArrayList<PhaseClock> clocks=new ArrayList<>();
    private ArrayList<DeltaListener> deltaListeners=new ArrayList<>();

    Instant(int sr) { sampleRate = sr; }

            // // // // //

    public PhaseClock addPhaseClock(double f) {
        PhaseClock pc=new PhaseClock(f);
        clocks.add(pc);
        return pc;
    }
    public void delPhaseClock(PhaseClock pc) { clocks.remove(pc); }

    public void addDeltaListener(DeltaListener dl) { deltaListeners.add(dl); }
    public void delDeltaListener(DeltaListener dl) { deltaListeners.remove(dl); }

    public int getSampleRate() { return sampleRate; }
    public long getSampleNumber() { return sampleNumber; }

    // // // // //

    void next() {
        sampleNumber++;
        clocks.forEach(PhaseClock::update);
        deltaListeners.forEach( dl -> dl.update(this) );
    }


}
