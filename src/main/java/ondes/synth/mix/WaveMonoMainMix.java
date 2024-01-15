package ondes.synth.mix;

public class WaveMonoMainMix extends MainMix {

    int sampleRate;

    public WaveMonoMainMix(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public int currentValue() {
        return 0;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public void update() {

        /*
         TODO - This is where all the action happens

         The MonoMainMix version looks like this:

            public void update() {
                outputBuffer[outPos++] = inputSum();
                srcLineWrite();
            }

          Here we instead append inputSum() to the end of the file,
          after copying the value to both L+R channels

        */
    }

}
