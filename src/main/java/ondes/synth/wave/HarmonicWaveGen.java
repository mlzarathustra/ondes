package ondes.synth.wave;

/**
 * <p>
 *   perform additive synthesis by constructing a wave composed
 *   of one or more harmonics of the base frequency (in sine waves).
 * </p>
 * <p>
 *   Similar to CompositeWaveGen but for this one we will
 *   create a snapshot of a single cycle instead of generating
 *   the sine waves each time.
 * </p>
 * <p>
 *   The composition of the wave is currently determined by an ASCII key(word)
 *   which is mapped to a set of values giving the frequency and divisor
 *
 * </p>
 *
 */
class HarmonicWaveGen extends WaveGen {

    static double TAO=Math.PI*2.0;
    private int scaledAmp;

    @Override
    void setFreq(double freq) {
        super.setFreq(freq);

        //scaledAmp = amp;

        scaledAmp = (int)(getAmp() * (1.0 + 55.0/(2*freq) ));
        // pitch scaling, to help out the bass notes
    }

    // a crude, inefficient (but easy to initialize) hashmap
    //
    /** the keys of the "hash-map." The correspondingly indexed
     *  element of waves is the value.
     */
    private String[] labels={ "mellow","odd","bell", "organ" };
    /**
     * the values, as a set of pairs. The pairs are each:
     *      frequency multiplier, divisor
     */
    private double[][] waves= {
            {1,1, 2,2, 3,3},
            {1,1, 2,2, 6,3, 14,3},
            {1,1, 2,2, 11,3, 14,3, 17,3},
            {1,1, 2,2, 3,3, 4,2, 8,2, 12,3}
    };

    private double[] wave=waves[0];



    /*
    synchronized long[] nextBuf() {
        //System.out.print("*");

        while (phase > TAO) phase -= TAO;
        for (int i = 0; i< samples.length; ++i) {
            double sum=0;
            for (int ov=0; ov<wave.length-1; ov+=2) {
                sum += Math.sin(phase*wave[ov])/wave[ov+1];
            }

            samples[i] = (long) (scaledAmp * sum);
            phase += twoPI * freq / sampleRate;
        }
        //if (loops<3) { loops++; System.out.println(Arrays.toString(samples)); }
        return samples;
    }

     */

    /**
     * TODO - transfer the above logic to here using the sampled sine waves
     *
     * @return component level at the instant of this sample.
     */
    @Override
    public int currentValue() {
        return 0;
    }
}
