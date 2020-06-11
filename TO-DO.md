# OndeSynth - to do
 
  - EnvGen
    - two modes (or maybe two outputs)
        - as a EG/VCA combo  
        - DC, to be hooked to a "multiply" component (`OpAmp`)
    - hold point (sustain)   
    - release point (not necessarily the last) so if note-OFF arrives in mid-cycle, 
        it takes the release point (rate, level) as the next step.
    - alt release: only triggered after note-OFF, when sustain is on.
        The logic will be tricky if the pedal goes up and down. 
    - infinite looping: loop-start loop-end - mutually exclusive with hold
    - extra credit: finite looping
    - alternate release (triggered when sustain pedal is down)
            
  - noteOFF routing / voice management - 
     - default: free the voice on note-OFF (as it is now)
     - If there's a designated envelope, free the voice when it is finished.
     - for now, only one envelope determines (rather than an AND or OR logic for several)
 
  - Taper off to avoid clicks at the end of sine waves.
  - Sustain pedal
 
      ```    
           #  A possible use case:  
         
         midi-key: 
           type: midi-key # the key number 0-128
           out: env1.rate 
      ```
  ------
  
  - LFO pitch mod (i.e. FM)
  
 ---- 
 
 Panners - 1D, 2D, 3D
    - control with lfo(s)
    - control with envelope
    
---  

 - (an)harmonic wave gen: 
    - force the data to be a list with 2 or 3 columns
    - the third column can be saw, ramp, square; for harmonic frequencies only, at first.   
  
 
 - get rid of the `name:` property in the patch files and use the file name minus .yaml instead.
    - warn of duplicates
    - normalize ' ' and '_' to '-' 
    - add a 'description' property instead of name and display it when listing patches.

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

 Channel components e.g. LFO
 Add a channel-global: on/off flag to components
 Keep them in ChannelVoicePool
  - It can hand them to VoiceMaker.getVoice() as a HashMap<String,MonoComponent>
  - which in turn, it passes to the Voice() constructor for the configure step.
   
 *****

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
    - flange
 