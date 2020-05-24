package ondes.synth;

import java.util.ArrayList;

public class Instant  {
    public class PhaseClock {
        private float frequency;
        private double delta;
        private double phase;
        long lastUpdate;

        PhaseClock(float f) {
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

    public PhaseClock addPhaseClock() { return addPhaseClock(0); }
    public PhaseClock addPhaseClock(int f) {
        return addPhaseClock(((float) f)/1_000_000);
    }
    public PhaseClock addPhaseClock(float frequency) {
        PhaseClock pc=new PhaseClock(frequency);
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
