# Ondes synth - to do

 - if output component is followed by a number, use it to scale output.
  
 - pitch bend
 
 - try out the Nyquist multiplier for velocity in WaveGen.velocityMultiplier
   

 - the dot notation might help also with input levels for the OpAmp. If we can label the inputs, then we can give them base/amt settings.

 - LFO pitch mod (i.e. FM)
 
 - migrate **level-scale** to the MonoComponent level (so automatically configure for it).
 Note that some components will ignore it (e.g. LFO's)  They can warn about it.

 - add velocity scaling for **op-amp** 
 (default: off => velocity-base: 100 velocity-amount: 0)
 
 - add an option **pitch-scale**: amount
 
 - try modulating the duty cycle of a sawtooth wave.
      
 - add a low-pass filter near 20khz to reduce aliasing

 - create a component that simply maps midi key to a value
    with an option for linear or logarithmic. 
   
 
 -----------
 - EnvGen needs to be hooked to a "multiply" component (`OpAmp`)
 
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

 