
# Program Configuration

## Terminology 
 - `program` refers to a set of specifications for a synth patch. It starts out in YAML and then turns into a Map when parsed. That's how it's kept internally.
 - `voice` when we wish to use those specifications to make noise, the engine turns them into a Voice. 
 
## Specifications (YAML) 
Below is a basic set of specifications.
 ```
 name: square
 
 osc1:
   midi: on   # send this component note ON messages
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
 
 - **type** - what type of component this is.  These values may be found in the `ComponentMaker` switch. Currently options are:
    - wave - wave generator
    - env - envelope generator
    - mixer - a junction between voices
    
   More will follow in the future.
 - **shape** - specific to the wave generator. Other components may have the same label but with different possible values.
  
 - **out** - where this component&rsquo;s output is sent to. Here it's going directly to the main out, but it could equally go to a DCA (digitally controlled amplifier) modulated with an envelope generator.
 
 ## MIDI message types
 The codes for the MIDI messages are:
 - **on** - Note-ON
 - **off** - Note-OFF
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
 
 ## wave shapes
 possible wave shapes at this point are:
 - square 
 - sine
 - saw
 - pwm
 - mellow
 - bell
 - organ
 
 
 --
 
       
    
   
 



