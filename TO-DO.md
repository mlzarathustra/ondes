# OndeSynth - to do

  - `WaveLookup` constructor: normalize level 

  - for the **waves** parameter, allow a pointer to a harmonic or anharmonic wave generator. Inherit the waves from the WG indicated. 

 ---
  - Panners - 
     - control with lfo(s)
     - control with envelope
 ---
  - Provide an API endpoint for morbleu
 

 -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-      
 ---
 ---
 ---   
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
 ----------
   - make a wave editor, maybe in Groovy? with a UI 
  