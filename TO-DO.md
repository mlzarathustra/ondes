# OndeSynth - to do


 - tighten the synchronization if possible:
    - phase clock activation
    - add component output to main mix inputs

---
 - if an envelope is done and at zero but the voice isn't, return without looking at inputs. 

---
  - LFO pitch mod (i.e. FM)
    - level-override: needs to allow for min and max, like out-level-amp: does for envelopes.
    
 ----
 - sweep IIR filter by linear extrapolation between data points.      
    
 ----
 
 - Ring modulation = f(t) * g(t) where both are sine functions (i.e. have a negative sweep)      
 
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
   
 *****

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

 - effects 
    - echo
    - reverb
    - flange / phase
    - distortion 
 