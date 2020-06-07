
# Filter parameters

A **filter** in audio circuitry alters the frequency response and phase of the sine waves that compose the sound. It turns out that filtering is one of the more mathematically complex aspects of synthesis. To fully grasp the details, you must be familiar with calculus and linear equations concepts such as convolution, poles and zeros, the Laplace transform and the Z-transform.   

However, the IIR algorithm itself is fairly straightforward, and using MatLab it is fairly easy to design various types of filters. It generates coefficients that can be used as data (in the IIRSpecLib class, for example) to drive the IIR algorithm.  

There are two basic types of filters: 
 - **FIR** = Finite Impulse Response - Zeros only (no feedback) 
 - **IIR** = Infinite Impulse Response - Poles and Zeros

The FIR does have one Pole at `dc` ("Direct Current") which is another way of saying "frequency zero."

An impulse response is a wave generated when a filter is subjected to a brief impulse, essentially a 'spike.' For an FIR, the impulse response is the same as the set of 'b' parameters used in the filter. An IIR, however has a response of theoretically infinite length, because unlike the FIR filter it feeds back the output (and taps into the delay of the output) back into the signal. That feedback is dictated by the 'a' parameters.  

**Ondes Synth** currently features both IIR and FIR filters. 

Of the two, the IIR filter is more flexible. Since Ondes Synth provides a standard IIR engine, it's possible to generate the `a` and `b` coefficients using MatLab, and plug in the data to the `IIRSpecLib` class for it to be available via a **key:** element as shown below.  

Eventually I would like a 4-pole sweeping VCF with Q like the Moog synthesizer, but one thing at a time. 

## IIR filter - lowpass  

Below is an IIR lowpass filter with a cutoff starting at 440 hz. 
```
lpf:
  type: filter
  shape: iir
  key: lp_4_440
  level-scale: 1
  out: main
```

The IIR filters available so far are all Butterworth filters designed
using MatLab. Butterworth filters are noted for their smooth **pass-band**, **transition**, and **stop-band** curves. Other filter types are capable of sharper cutoffs, by sacrificing the evenness of the frequency response in one or more of the aforementioned bands. 
 
Available keys for filter types presently are mnemonically labelled:

    {type}_{order}_{frequency}
    
So, **lp_6_1k** is a low-pass 6th order filter with a cutoff of 1,000 hz. For a Butterworth filter, that means 6 zeros and 6 poles.     

The higher-order filters are capable of sharper cutoffs, but also are prone to becoming unstable in lower frequencies (Meaning: loud unpleasant noise). They seem to run OK above about 2k.  

Note: the Moog uses a 4-pole filter.

Below are the keys currently available:

``` 
        lp_6_1k   // becomes unstable easily
        lp_6_2k
        lp_6_5k
        lp_6_10k
        lp_6_15k
        lp_6_20k
        
        lp_4_440
        lp_4_1k
```

## FIR filters 

These are both 'notch' filters, as that is what results from doing a running average. As mentioned previously, FIR filters only have zeros, which translate into notches in the frequency response graph. 

They draw what is known as a 'sinc' wave, which is a variation of the formula ((sin x) / x), only the lobes are folded over from the negative quadrant. 

You can hear with the 'filter-test' patch how A440 and its harmonics are all blocked by this fixed-frequency filter:
```
name: filter-test

osc1:
  midi: note-on
  type: wave
  velocity-base: 100
  shape: sine
  level-scale: 2
  out: lpf

lpf:
  type: filter
  shape: sinc
  freq: 440
  out: main
```

And as more of a hack than anything else, I decided to try sweeping the filter. It gets some clicks here and there, but it still sounds pretty cool. 

You can hear it in the `pwm-sweep` patch.

The two components below are the LFO and the filter. See the patch YAML for the rest. 
```

    lpf-lfo:
      type: wave
      signed: true
      freq: .07
      shape: saw
      level-override: 1000
      out: lpf.sweep


    lpf:
      type: filter
      shape: sweep-sinc
      #midi: note-on
      freq: 1480 # F#5 about
      sweep-width: 24 # semitones
      input-amp: 1000
      out: main
```