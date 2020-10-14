package ondes.synth.wave.lookup;

import ondes.synth.wave.WaveGen;

import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static ondes.synth.wave.lookup.SineLookup.sineLookup;

public abstract class CompositeWave extends WaveGen {

    public final float[] defaultWave = { 1,1, 2,2, 3,3 };
    protected float[] harmonicParams =defaultWave;

    protected static HashMap<String,FloatWaveLookup> waveLookups=new HashMap<>();

    protected FloatWaveLookup waveLookup;

    boolean first=true;
    protected float valueAtPhase(double phase) {
        double sum=0;
        for (int ov = 0; ov< harmonicParams.length-1; ov+=2) {
            sum += sineLookup( phase * harmonicParams[ov] )
                / harmonicParams[ov+1];
        }
        first=false;
        return (float)sum;
    }

    @SuppressWarnings("rawtypes,unchecked")
    protected String[] listToTokenAry(List waveConfig) {
        String str = ""+(waveConfig)
            .stream()
            .map(obj-> obj==null?"":obj.toString())
            .collect(joining(" "));
        return str.split("[\\s,]+");
    }






}
