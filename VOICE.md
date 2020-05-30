
# Program Configuration

## Terminology 
 - `program` refers to a set of specifications for a synth patch. It starts out in YAML and then turns into a Map when parsed. That's how it's kept internally.
 - `voice` when we wish to use those specifications to make noise, the engine turns them into a Voice. 
 
## Specifications (YAML) 
Below is a basic set of specifications.
 ```
 name: square
 
 osc1:
   midi: note-on   # send this component note ON messages
   detune: -12
   offset: 7 
   type: wave
   shape: square
   out: main
```

The properties are as follows:
 - **name** - a label used so you can tell the synth you want this sound. Use the name or a distinct substring with the -all or -ch<n> command-line options to the synth. Alternately, you may use the index of the sound given by the `-list-programs` option. 
 
A program can contain multiple components but the above has only one, a square wave generator by the name of osc1. 
  
 - **osc1** - this is the label of the component. It's available for use by any component in the same voice (i.e. this same file).
 Note that the global **main** component (the MainMix object) is declared by default for every voice. If you neglect to put a line `out: main` in some component, the voice has no way of sounding.
 
 - **midi** - tell which type(s) of MIDI message this component should receive. Possibilities are listed below.
 
 - **detune** - cents relative to the base frequency. 100 cents equals a half step, so adding detune plus offset you can choose any frequency relative to the base. May be positive or negative.
 
 - **offset** - number of half steps to offset the frequency from the base. -12 is down an octave. 6 is a tritone. Dust off your music theory books! 
 
 - **type** - what type of component this is.  These values may be found in the `ComponentMaker` switch. Currently options are:
    - wave - wave generator
    - env - envelope generator
    - mixer - a junction between voices
    - limiter - keeps a signal from overflowing
    
   More will follow in the future.
 - **shape** - specific to the wave generator. Other components may have the same label but with different possible values.
  
 - **out** - where this component&rsquo;s output is sent to. Here it's going directly to the main out, but it could equally go to a DCA (digitally controlled amplifier) modulated with an envelope generator.
 
 ## other properties 
  - **output-amp** - Sets the output amplitude, overriding other considerations. For sound WaveGens, we scale according to pitch, so that that low frequencies won't get lost, and note-velocity if specified. However, for an LFO we need to control the output level very precisely. It's meant to be paired with the **input-amp** setting on the destination, i.e. both should be the same.
  
  - **input-amp** - The expected maximum amplitude of the input. For a single source, it should be the same as the **output-amp** setting on the source. For multiple sources, you'll have to do some math. The sources are added together.
  
  - **mod-percent** - The percentage of modulation for a PWM wave. See `pwm.yaml` in the resources directory for examples. PWM stands for "Pulse Width Modulation" and refers to changing the duty cycle of a square wave, which gives a kind of combing effect.
  
  All signals traveling inside the voice are of type integer, so 32 bits of accuracy. They can be positive or negative.
  
  - **preset** - for a harmonic wave, there are a few presets you can use as an alternative to the **waves** setting that follows. Use one or the other. You can't use both.
  
    Current harmonic wave presets are: 
    - mellow
    - odd
    - bell
    - organ
  
  - **waves** - For the harmonic or anharmonic waves, the composition of the waves is given as a series of integer pairs: 
      - frequency multiplier - we multiply the base frequency by this number to arrive at the frequency of the sine wave you want to add. 
      
      - divisor - we divide the amplitude by this number to get the amplitude of your "harmonic" (or anharmonic as the case may be)

      Note that for harmonic waves, you may specify non-integer values, but since the wave will be a snapshot of the single cycle (and hence harmonic) you will not hear an anharmonic tone, but rather a kind of buzzing. It can yield possibly useful results, so I left it that way.
       
      If you want a genuine anharmonic voice, use the "anharmonic" wave shape. Note that the anharmonic WaveGen is much less efficient than the harmonic one, as it creates all of the waves on the fly. The harmonic WaveGen takes a snapshot of a single wave and uses it as a wavetable. (which is, incidentally, also how a plain sine wave works). 
      
    Here are the settings that the "octave organ" uses (a harmonic WaveGen)   
```
          waves:
            - 1 1
            - 2 2
            - 4 4
            - 8 8
            - 16 16
```
    
 
 ## MIDI message types
 The codes for the MIDI messages are:
 - **note-on** - Note-ON
 - **note-off** - Note-OFF
 - **after** - After-touch
 - **control** - MIDI controllers, including 
    - 0 - bank select MSB
    - 1 - mod wheel
    - 7 - volume
    - 10 - pan
    - 32 - bank select LSB
    - 64 - sustain pedal
 - **program** - program change
 - **pressure** - channel pressure
 - **bend** - pitch bend
 - **system** - system message     
 
Note: the above is almost in numeric order. The first two are swapped, because it bugs me to see Note OFF (0x8) before Note ON (0x9). The rest are sequential. In real life, the ON comes first, right? Even though 0x9 has the 1 bit set and 0x8 doesn't (is that why they swapped them?)

Note also: the reason it's "note-on" instead of just "on" is that YAML insists on converting the word "on" into a boolean. (same is true of a list of other words... on,off,yes,no,true,false, in whatever case).
 
 ## wave shapes
 possible wave shapes at this point are:
 - square 
 - sine
 - saw
 - ramp-up
 - ramp-down
 - harmonic
 - anharmonic
 - pwm

 
 
 --
 
       
    
   
 




