
# common parameters 

```yaml
osc1: 
  midi: note-on
  type: wave
  out: main
  shape: sine
```  
  
  
The following can apply to all components. 
  
 - **osc1** - this is an example of the label of a component. It's available for use by any component in the same voice (i.e. this same file).
 
 - **midi** - tell which type(s) of MIDI message this component should receive. The **MIDI message types** section below lists the eight types.
 
 - **type** - this parameter is required (so ondes will know what type of component to create)
  
  - **out** - where this component&rsquo;s output is sent to. Here it's going directly to the main out, but it can be directed to any component that takes input. For example a `filter` or `op-amp`. 
 
 - Note that the global **main** component (the MainMix object) is declared by default for every voice. If you neglect to put a line `out: main` in some component, the voice has no way of sounding.
 
 - **context** - use `context: channel` to have one of these per MIDI channel. Otherwise, there will be one per voice. 
 
 - **level-scale** - output scaling. 1 is unchanged. .5 is half as loud. 2 is twice as loud. The Synth only allows you to go to 11. There is a limiter that will save you from the nasty wave truncation if you overload, but it can only smooth out the signal so far. So if it displays the overload symbol `<>` on the console, you want to turn the levels down.  

 
  - **level-override** - Sets the output amplitude, overriding other considerations. For sound WaveGens, we scale amplitude (or actually, peak deviation) according to pitch, so that that low frequencies won't get lost, and note-velocity if specified. However, for an LFO we need to control the output level very precisely. It's meant to be paired with the **input-amp** setting on the destination, i.e. both should be the same.
  
  - **input-amp** - The expected maximum amplitude of the input. For a single source, it should be the same as the **level-override** setting on the source. For multiple sources, you'll have to do some math. The sources are added together.
  
  - **signed** - When `false` tells the WaveGen to output only values above zero. When using as an LFO to modulate amplitude, that's probably what you want. For audible waves or to modulate frequency, leave **signed** set to `true` For an audio signal, that will double the amount of headroom, because you'll be using the amplitude available both above and below zero, instead of only above. Use the **freq** property to set the frequency. 
   
All signals traveling inside the voice are of type integer, so 32 bits of accuracy. They can be positive or negative.
 
 ## MIDI message types
 The codes for the MIDI messages are:
 - **note-on** - Note-ON
 - **note-off** - Note-OFF
 - **after** - After-touch
 - **control** - MIDI controllers
 
    Sub-types:
    - 0 - **bank-msb**
    - 1 - **mod-wheel**
    - 7 - **volume**
    - 10 - **pan**
    - 32 - **bank-lsb**
    - 64 - **sustain** 
    
    If you send any controller to a component it will receive all of them. The labels for the specific controllers are  for convenience, and are all equivalent to simply saying "control."   
 - **program** - program change
 - **pressure** - channel pressure
 - **bend** - pitch bend
 - **system** - system message     


 ** ** 

(The reason it's "note-on" instead of just "on" is that YAML converts the word "on" into a boolean, and we would like a string. It also converts a number of other words to boolean: on,off,yes,no,true,false, in whatever case).

 
