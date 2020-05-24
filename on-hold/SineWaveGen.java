package ondes.synth.wave;

/**
 * Generate a sine wave. Adapted from an example found on the internet.
 */
class SineWaveGen extends WaveGen {

    static double twoPI=Math.PI*2.0;
    private double phase=0;
    private int scaledAmp;

    @Override
    void setFreq(double freq) {
        super.setFreq(freq);

        //scaledAmp = amp;

        scaledAmp = (int)(amp * (1.0 + 55.0/(2*freq) ));
        // pitch scaling, to help out the bass notes
    }

    @Override
    public void reset() { phase=0; }

    @Override
    synchronized long[] nextBuf() {
        //System.out.print("*");

        while (phase > twoPI) phase -= twoPI;
        for (int i = 0; i< samples.length; ++i) {
            samples[i] = (long) (scaledAmp * Math.sin(phase));
            phase += twoPI * freq / sampleRate;
        }
        //if (loops<3) { loops++; System.out.println(Arrays.toString(samples)); }
        return samples;
    }



}
