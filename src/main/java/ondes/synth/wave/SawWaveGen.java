package ondes.synth.wave;

/**
 * A crude, but effective, sawtooth.
 * It's in fact a ramp wave, with a sharp drop and a slow rise.
 *
 */
class SawWaveGen extends WaveGen {

    private double phase = 0; // range: 0-1
    private double dutyCycle = 0.5;

    @Override
    long[] nextBuf() {
        for (int i = 0; i< samples.length; ++i) {
            while (phase > 1) phase -= 1;
            samples[i] = (long) ((phase * 2.0 - 1.0) * amp);
            phase += freq / sampleRate;
        }
        //if (loops<3) { loops++; System.out.println(Arrays.toString(samples)); }
        return samples;
    }

    @Override
    void reset() {
        phase = 0.5;
    }

}
