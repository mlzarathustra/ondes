package ondes.synth.wave;

import java.util.Map;
import java.util.Random;

public class NoiseWaveGen extends WaveGen {

    static final Random rnd = new Random();

    @Override
    public int currentValue() {
        int rs = rnd.nextInt(2 * getAmp());

        if (signed) rs -= getAmp();

        //System.out.println("noise cv:"+rs);
        return rs;
    }


}
