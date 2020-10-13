package ondes.synth.wave;

import ondes.synth.wave.lookup.WaveLookup;

import java.util.Arrays;

import static java.lang.System.out;

public class WaveEditor extends WaveGen {
    static boolean DB=true;
    protected WaveLookup waveLookup;


    public static void setWave(float[] harmonics) {
        out.println( "WaveEditor: harmonics="+Arrays.toString(harmonics) );


    }

    @Override
    public int currentValue() {
        return (int)(
            waveLookup.valueAt(phaseClock.getPhase()) * getAmp()
        );
    }
}
