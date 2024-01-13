package ondes.synth.wave;

import ondes.synth.wave.lookup.FloatWaveLookup;
import ondes.synth.wave.lookup.WaveLookup;

import java.util.Arrays;

import static java.lang.System.out;
import static ondes.synth.wave.lookup.SineLookup.sineLookup;

public class WaveEditor extends WaveGen {
    static boolean DB=true;
    protected static FloatWaveLookup waveLookup, backBuffer;
    static float[] harmonics;

    static float valueAtPhase(float phase) {
        float val=0;
        for (int i=0; i<harmonics.length; ++i) {
            float h=harmonics[i];
            if (h != 0) val += sineLookup( phase * (i+1) ) * h;
        }

        return val;
    }

    // todo - refresh rather than new if possible

    public static void setWave(float[] harmonics) {
        //out.println( "WaveEditor: harmonics="+Arrays.toString(harmonics) );
        WaveEditor.harmonics = harmonics;
        if (backBuffer == null) backBuffer = new FloatWaveLookup(WaveEditor::valueAtPhase);
        else backBuffer.refresh();
        FloatWaveLookup tmp = waveLookup;
        waveLookup = backBuffer;
        backBuffer = tmp;
    }

    @Override
    public int currentValue() {
        if (waveLookup == null) return 0;
        return (int)(
            waveLookup.valueAt((float)phaseClock.getPhase()) * getAmp()
        );
    }
}
