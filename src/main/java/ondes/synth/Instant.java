package ondes.synth;

import java.util.ArrayList;
import static java.lang.System.out;


@SuppressWarnings("FieldMayBeFinal")
public class Instant  {
    public class PhaseClock {
        private float frequency;
        private double delta;
        private double phase;
        long lastUpdate = -1;

        PhaseClock(float f) {
            setFrequency(f);
        }

        void setDelta() {
            delta = frequency / sampleRate;
        }

        public void update() {
            if (lastUpdate < 0) lastUpdate = sampleNumber;
            while (lastUpdate < sampleNumber) {
                phase += delta;
                lastUpdate++;
            }
            while (phase >= 1) phase -= 1;
        }
        public double getPhase() { return phase; }

        public float getFrequency() { return frequency; }
        public void setFrequency(float freq) {
            frequency = freq;
            setDelta();
        }
    }

    private int sampleRate;
    private long sampleNumber;

    private ArrayList<PhaseClock> clocks=new ArrayList<>();

    Instant(int sr) { sampleRate = sr; }

            // // // // //

    public PhaseClock addPhaseClock() { return addPhaseClock(0); }
    public PhaseClock addPhaseClock(int f) {
        return addPhaseClock(((float) f)/1_000_000);
    }
    public PhaseClock addPhaseClock(float frequency) {
        out.println("addPhaseClock: count is "+clocks.size());
        PhaseClock pc=new PhaseClock(frequency);
        clocks.add(pc);
        return pc;
    }
    public void delPhaseClock(PhaseClock pc) {
        out.println("delPhaseClock: count is "+clocks.size());
        clocks.remove(pc);
    }

    public int getSampleRate() { return sampleRate; }
    public long getSampleNumber() { return sampleNumber; }

    // // // // //

    void next() {
        sampleNumber++;
        clocks.forEach(PhaseClock::update);
    }


}
