
# Frequency Modulation

Oscillators will have two inputs governing frequency, both named. So the `out:` property will use the dotted notation, in order to specify the name: `log` or `linear`.

The `log` (logarithmic) input is for analog-style vibrato, and is measured in scale semitones. The `linear` input is measured in frequency. This is what you want for DX-7 style FM.   

For simplicity, I'll say the **modulating** and **sounding** oscillators, though you can use the same oscillator for both.

On the modulating side, you must give the output tag (below, **osc7.log** and **osc7.linear**)


```yaml
mod-osc6: 
  freq: 0.11
  type: wave
  shape: sine
  level-override: 1000
  out: osc7.log  # to the logarithmic input for vibrato

mod-osc8: 
  midi: note-on
  type: wave
  shape: sine
  level-override: 1000
  out: osc7.linear  # this is the one for FM  
```

On the sounding side, you can define the width of the modulation, but because the type of modulation is different, the terminology varies between the two.

For input-log, you define the width in semitones. For input-linear, it's in frequency. Both will also be affected by the envelope or other attenuation of the modulating oscillator.

In both cases, you need to specify the expected amplitude of the input. Because the input is integer, you can achieve greater accuracy by setting a higher input amplitude. This corresponds with the `level-override` of the modulating oscillator: they should be the same, to achieve unity when the incoming level is divided by the input-amp parameter. 

It's possible you would want to use both, e.g. the linear input for FM wave-forming and the log input for vibrato.  



```yaml
osc7: 
  midi-note-on
  type: wave
  shape: sine
  input-log:
    semitones: 12
    amp: 1000  # corresponds with level-override: of the input

osc8: 
  midi-note-on
  type: wave
  shape: sine
  input-linear:  
    freq: 100
    amp: 1000  # corresponds with level-override: of the input

```