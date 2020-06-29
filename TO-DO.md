# OndeSynth - to do

 ---
  - make it easier to create default envelopes (e.g. have a shortcut for midi ON, OFF and control) plus exit: true
   
 ---
 
  - allow for command-line defaults in a properties(?) file or something
 
 ---
 
  - error reporting: report on non-wavegen comps that don't have input?
  - report on unknown properties given? 
 
 ---
 
  - distortion - use (2 * atan(y))/pi 
     x > 0 (the slope is 1/(1+x^2), so it will be 1 at 0)
     y will be asymptotic to 1.
 
 ---
 
  - can we use 
        arcsin( (y[0]-y[-1]) / (x[0]-x[-1]) ) 
        
    to limit the angular change?
     
    That should cause some kind of filtering. (probably requires
    a lookup table, easier given that the denominator is constant)
 
 ---
  
 - sweep IIR filter by linear extrapolation between data points.     
 
---
 - straighten out the various level coefficients in WaveGen
    - ambBase
    - pitchScale
    - velocityMultiplier
    - levelScale    
    
 ----
 
 - Panners - 1D, 2D, 3D
    - control with lfo(s)
    - control with envelope
    
---  
  - Channel components e.g. LFO
 Add a channel-global: on/off flag to components
     - Keep them in ChannelVoicePool
     - It can hand them to VoiceMaker.getVoice() as a HashMap<String,MonoComponent>
     - which in turn, it passes to the Voice() constructor for the configure step.
   
 
 --- 
  - Anharmonic - adjust frequence of phase clocks. See TODO at WaveGen.modFreq()

 ---

 - (an)harmonic wave gen: 
    - force the data to be a list with 2 or 3 columns
    - the third column can be saw, ramp, square; for harmonic frequencies only, at first.   

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

 - create a component that simply maps midi key to a value
    for use in modulation, 
    with an option for linear or logarithmic. 
  
 - try Helmholtz as oscillator / resonator   
 
 -----------
   - EnvGen
 
     - infinite looping: loop-start loop-end - mutually exclusive with hold
     - extra credit: finite looping
 
   - If there is no envelope, add a default one to taper off to avoid clicks at the end of sine waves.
  
       ```    
            #  A possible use case:  
          
          midi-key: 
            type: midi-key # the key number 0-128
            out: env1.rate 
       ```
   ------

 - multi-voice polyphony - could have a mode that re-uses the first available voice (for portamento &c.) 
 
 - Multi-channel (e.g. Stereo) components
   The input and output would be an int array 
   or a `long` for just two channels.

----------------

 - optimize the limiter by clearing the buffer when we reach a new max. Verify that it's actually faster that way before installing, though. 
 
 - manage ChannelVoicePool from a separate thread

-----------

aliasing - oversample and then downsample? 

 ---

 - effects 
    - echo
    - reverb
    - flange / phase
    - distortion 
 