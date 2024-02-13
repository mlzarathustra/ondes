# OndeSynth - to do

  - Voicemaker.findProg("") - finds "pwm" because it every string "contains" "" and it picks the first shortest name which (alphabetically sorted) happens to be pwm. 

  - Provide an API endpoint for morbleu
    
  - named inputs - translate them into an array
    brass.yaml is a good example case 

  - catalog defined programs 

  - Balancer
      - write documentation
      - What is the meaning of     
        ```amp: 1000000  # here implies: -1000..1000```
        in test-balancer.yaml?
      - play with test patches
      - control with envelope

- 4-pole filter = 2+2, following all inputs (freq, res)


- Finish ChannelVoicePool.updateState - it needs to propagate the channel state to the channel-context components


---

  - Sample Rate change
    - Is there an easy way to make the fixed IIR filters compensate
      for sample rate change?

    - BiQuad should be getting sampleRate from synth, so it should work.
    
  - `WaveLookup` constructor: normalize level 

  - for the **waves** parameter, 
    - allow a pointer to a harmonic or anharmonic wave generator. 
    - Inherit the waves from the WG indicated. 

 ---

### Sample Rate Adjust 

search for DBG0115

The actual sample rate is currently only set in MonoMainMix.openOutputLine():
Apparently 44100 and 48000 are OK, but higher than that it gets gaps.

44100 appears in some test methods, which don't affect the main synth running.



<br/><br/><br/><br/><br/>



####################################################################

 
  - if there is only one envelope, assume it is the primary.
  - change the keyword "exit" to "primary"?
 ---
   performance? - shut off startup tasks:
   https://answers.microsoft.com/en-us/windows/forum/windows_10-performance/windows-10-performance-and-install-integrity/75529fd4-fac7-4653-893a-dd8cd4b4db00

 ---
 - (an)harmonic wave gen: 
    - allow for a third "column" in each set - perhaps require that the input be a list of pairs or triplets
    - the third column can be saw, ramp, square; for harmonic frequencies only, at first.
    See `CompositeWave.valueAtPhase()` - the easiest will probably be to break the list into 3 components (as the Anharmonic waves now do) with the 3rd being a string key of the wave form (or null if sine)  
    
 ---
  - try smoothing gaps in controller sweep using "smooth" algorithm.
 ---
  - distortion - use (2 * atan(y))/pi 
     x > 0 (the slope is 1/(1+x^2), so it will be 1 at 0)
     y will be asymptotic to 1.
---
 - straighten out the various level coefficients in WaveGen
    - ambBase
    - pitchScale
    - velocityMultiplier
    - levelScale    
 ----
  - a MONO mode with portamento
 ---
 - if a number follows the output component name, use it to scale output.
 - pitch bend
 - try out the Nyquist multiplier for velocity in WaveGen.velocityMultiplier
 - the dot notation might help also with input levels for the OpAmp. If we can label the inputs, then we can give them base/amt settings. Or should that only happen on the output?
 - migrate **level-scale** to the MonoComponent level (so automatically configure for it).
 Note that some components will ignore it (e.g. LFO's) if level-override is set. They can warn about it.

 - add velocity scaling for **op-amp** 
 (default: off => velocity-base: 100 velocity-amount: 0)
 
 - add an option **pitch-scale**: amount
 
 - try modulating the duty cycle of a sawtooth wave.
  
 - try Helmholtz as oscillator / resonator   

 - multi-voice polyphony - could have a mode that re-uses the first available voice (for portamento &c.) 
 
 --- 
 - Multi-channel (e.g. Stereo) components
   The input and output would be an int array 
   or a `long` for just two channels.

-----------
  - error reporting: report on non-wavegen comps that don't have input?
  - report on unknown properties given? 
 ---
 - aliasing - oversample and then downsample? 
 ---
 - effects 
    - reverb
    - flange / phase
    - distortion 
 -----------
   - EnvGen
     - infinite looping: loop-start loop-end - mutually exclusive with hold
     - extra credit: finite looping

  