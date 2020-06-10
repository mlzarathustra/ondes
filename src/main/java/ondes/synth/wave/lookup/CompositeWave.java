package ondes.synth.wave.lookup;

import ondes.synth.wave.WaveGen;

import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static ondes.synth.wave.lookup.SineLookup.sineLookup;

import static java.lang.System.out;

public abstract class CompositeWave extends WaveGen {

    public static double TAO=Math.PI*2.0;

    public final double[] defaultWave = { 1,1, 2,2, 3,3 };
    protected double[] harmonicWaves =defaultWave;

    protected static HashMap<String,WaveLookup> waveLookups=new HashMap<>();

    protected WaveLookup waveLookup;

    boolean first=true;
    protected double currentValue(double phase) {
        double sum=0;
        for (int ov = 0; ov< harmonicWaves.length-1; ov+=2) {
            if (first) out.println("CompositeWave.currentValue freq="+harmonicWaves[ov]+
                "; div="+harmonicWaves[ov+1]);
            sum += sineLookup( phase * TAO * harmonicWaves[ov] )
                / harmonicWaves[ov+1];
        }
        first=false;
        return sum;
    }

    @SuppressWarnings("rawtypes,unchecked")
    protected String[] listToTokenAry(List waveConfig) {
        String str = ""+(waveConfig)
            .stream()
            .map(Object::toString)
            .collect(joining(" "));
        return str.split("[\\s,]");
    }






}
