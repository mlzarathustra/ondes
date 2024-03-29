package ondes.synth.wave;


import java.util.Map;
import static java.lang.System.err;
import static java.lang.System.out;

import static ondes.synth.component.ConfigHelper.*;


/**
 * Generate Pulse-Modulated Square wave.
 * Meaning: modulate the duty cycle with a Low-Frequency Oscillator (LFO)
 */
class PwmWaveGen extends WaveGen {

    private final double dutyCycle = 0.5;
    private float inputAmpInv = 0;
    private float modPercent = 0;


    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Double dblInp= getDouble(config.get("input-amp"),
            "'input-amp' must be a number, typically " +
                "the same as the level-override of the sender.");
        if (dblInp != null) {
            float inputAmp =  dblInp.floatValue();
            if (inputAmp != 0) inputAmpInv = 1.0f/inputAmp;
        }

        String modPctErr = "mod-percent must be a number from 0 to 100.";
        dblInp = getDouble(config.get("mod-percent"), modPctErr);
        if (dblInp != null) {
            modPercent = (float)dblInp.doubleValue();
            if (modPercent<0 || modPercent >100) {
                err.println(modPctErr);
                modPercent = 0;
            }
        }
    }

    @Override
    public int currentValue() {
        modFreq();
        float inpSum=namedInputSum("pwm");

        float mod = inpSum * inputAmpInv;
        double modDutyCycle = dutyCycle + (modPercent/200.0) * mod;
        return  (
            (phaseClock.getPhase() > modDutyCycle) ?
                getAmp() : -getAmp()
        );
    }
}
