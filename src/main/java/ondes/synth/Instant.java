package ondes.synth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.out;


@SuppressWarnings("FieldMayBeFinal")
public class Instant  {
    public class PhaseClock {
        private float frequency;
        private double delta;
        private double phase;
        long lastUpdate = -1;


        PhaseClock() { this(0); }
        PhaseClock(float f) { setFrequency(f); }

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
            //frequency = min(maxFreq,max(0,freq));
            frequency = freq;
            setDelta();
        }
    }

    private int sampleRate;
    private long sampleNumber;
    private int maxFreq;
    private double seconds;

    private final ConcurrentLinkedDeque<PhaseClock>
        clocks=new ConcurrentLinkedDeque<>(),
        phaseClockPool = new ConcurrentLinkedDeque<>();

    Instant(int sr) {
        sampleRate = sr;
        maxFreq = sr/2;
        reservePhaseClocks(1000);
    }

            // // // // //


    /**
     * Avoid the "new" delay by creating them beforehand.
     * @param n - How many to create.
     */
    public void reservePhaseClocks(int n) {
        while (n-- > 0) phaseClockPool.push(new PhaseClock());
    }

    public PhaseClock addPhaseClock() { return addPhaseClock(0); }

    public PhaseClock addPhaseClock(float frequency) {
        PhaseClock pc;
        if (phaseClockPool.isEmpty()) {
            pc=new PhaseClock();
        }
        else pc = phaseClockPool.pop();
        pc.setFrequency(frequency);

        clocks.add(pc);
        return pc;
    }
    public void delPhaseClock(PhaseClock pc) {
        phaseClockPool.push(pc);
        clocks.remove(pc);
    }

    public int getSampleRate() { return sampleRate; }
    public long getSampleNumber() { return sampleNumber; }

    public double getSeconds() { return seconds; }

    // // // // //

    public void next() {
        sampleNumber++;
        seconds = ((double)sampleNumber)/sampleRate;
        clocks.forEach(PhaseClock::update);
    }


}
