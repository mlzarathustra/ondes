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

        synth.stop = true;


        /*
         TODO - This is where all the action happens

         It's the only place we get called back from Ondes
         in the tight loop at the end of run().

         So the midi stuff has to get handled here, and also



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
