# OndeSynth - to do
 
  - Play back from MIDI file
    - Do I want to use SynthSession for this? 
    - For now, only "play back" to a WAV file
    - Implement WaveMonoMainMix, AS a MainMix, to capture data
    - write to wave file

    Voicemaker.findProg("") - finds "pwm" because it every string "contains" "" and it picks the first shortest name which (alphabetically sorted) happens to be pwm. 

    For some reason, it fails to do so from within PlayMidiFile.java. 




  - Provide an API endpoint for morbleu
    
  - named inputs - translate them into an array
    brass.yaml is a good example case 

  - Clean up documentation - make sure all the 
    - components are documented
    - all defined programs are documented
    

  - 4-pole filter = 2+2, following all inputs (freq, res)


---

  - Balancer
    - play with test patches
    - control with lfo(s)
    - control with envelope
    - write documentation
    

  - `WaveLookup` constructor: normalize level 

  - for the **waves** parameter, allow a pointer to a harmonic or anharmonic wave generator. 
  - Inherit the waves from the WG indicated. 

 ---

# Sample Rate Adjust 

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

  