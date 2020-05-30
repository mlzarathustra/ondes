package ondes.synth.wave;

/**
 * Generate Pulse-Modulated Square wave.
 * Meaning: modulate the duty cycle with a Low-Frequency Oscillator (LFO)
 * LFO frquency is set by a formula according to the wave frequency.
 */
class PWMWaveGen extends WaveGen {

    static final double twoPI = 2.0 * Math.PI;
    private double phase = 0; // range: 0-1
    private double dutyCycle = 0.5;

    private double baseLfoFreq = 1.0 / 8.0;
    private double lfoFreq = baseLfoFreq;
    private double lfoVar = 0.4;  // amt of variation

    private double lfoPhase = 0;

    @Override
    long[] nextBuf() {
        for (int i = 0; i< samples.length; ++i) {
            while (phase > 1) phase -= 1;
            while (lfoPhase > 1) lfoPhase -= 1;
            double modDutyCycle = dutyCycle + Math.sin(lfoPhase*twoPI) * lfoVar;

            samples[i] = (long) ((phase>modDutyCycle)?amp:-amp);

            phase += freq / sampleRate;
            lfoPhase += lfoFreq / sampleRate;
        }
        //if (loops<3) { loops++; System.out.println(Arrays.toString(samples)); }
        return samples;
    }

    @Override
    void reset() {
        phase = 0;
        lfoPhase=0;
    }

    @Override
    public void setFreq(double freq) {
        this.freq=freq;
        lfoFreq = baseLfoFreq * (freq/200.0);
    }

}
