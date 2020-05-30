package ondes.synth.wave;

import ondes.synth.wire.WiredIntSupplier;

import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Generate Pulse-Modulated Square wave.
 * Meaning: modulate the duty cycle with a Low-Frequency Oscillator (LFO)
 */
class PwmWaveGen extends WaveGen {

    private double dutyCycle = 0.5;
    private float inputAmp = 0;
    private float modPercent = 0, modMultiplier;

    private double lfoPhase = 0;

/*
    long[] nextBuf() {
        for (int i = 0; i< samples.length; ++i) {
            while (phase > 1) phase -= 1;
            while (lfoPhase > 1) lfoPhase -= 1;
            double modDutyCycle = dutyCycle + Math.sin(lfoPhase*twoPI) * lfoVar;

            samples[i] = (long) ((phase>modDutyCycle)?amp:-amp);

            phase += freq / sampleRate;
            lfoPhase += lfoFreq / sampleRate;
        }
        //if (loops<3) { loops++; System.out.println(Arrays.toString(samples)); }
        return samples;
    }
*/

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config,components);

        Double dblInp= getDouble(config.get("input-amp"),
            "'input-amp' must be a number, typically " +
                "the same as the output-amp of the sender.");
        if (dblInp != null) inputAmp = (float)(1.0 / dblInp);

        String modPctErr = "mod-percent must be a number from 0 to 100.";
        dblInp = getDouble(config.get("mod-percent"), modPctErr);
        if (dblInp != null) {
            modPercent = (float)dblInp.doubleValue();
            if (modPercent<0 || modPercent >100) {
                err.println(modPctErr);
                modPercent = 0;
            }
            modMultiplier = modPercent / 100;
        }
    }


    @Override
    public int currentValue() {
        float inpSum=0;
        // todo - PWM should have dedicated input: main is for freq
        for (WiredIntSupplier input : inputs) {
            inpSum += input.getAsInt();
        }
        float mod = inpSum * inputAmp;

        double modDutyCycle = dutyCycle + (modPercent/200.0) * mod;
//        out.println("PWM.currentValue() inpSum="+inpSum+
//            "; modDutyCycle="+modDutyCycle);

        return  (
            (phaseClock.getPhase() > modDutyCycle) ?
                getAmp() : -getAmp()
        );
    }
}
