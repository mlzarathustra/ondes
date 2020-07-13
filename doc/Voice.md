
# Programming Patches

## Patching together components in YAML

 A `program` in OndeSynth is a set of specifications for a patch in YAML. If you don't know YAML, you can probably fake it by looking at the existing patch files. It's a hierarchical data format with in which indents indicate the hierarchy. Plentiful information exists about it online. 
 
 The application looks in two places for the YAML files: the resources directory `program` (a collection internal to the app, mostly basic patches like plain sine and sawtooth waves) and a directory called `program` relative to the working directory. It will parse subdirectories also, but only if the command line contains `-all`. Subdirectories are meant for test patches that you don't want to clutter up the main list with.
 
 The file name minus '.yaml' will be the patch name. If there are duplicate patch names, you can use `ondes -list` and select by number instead of name.
 
The engine uses the YAML specifications to create a `Voice.` The `ChannelVoicePool` class manages them, creating them beforehand to avoid garbage collection delays. 
 
## example patch (YAML) 
Here is a patch with a single component, of type 'wave' (also called "WaveGen" or "wave generator")
 ```
 osc1:
   midi: note-on   # send this component note ON messages
   detune: -12
   offset: 7 
   type: wave
   shape: square
   out: main
```

## Components 
 - Each component is a Map named by a key (within the main Map), which must be global to the file. For example `osc1` above. The uniqueness constraint is part of the YAML spec.

 - Components can now reside in either the `Voice` or the `Channel` context. Likely candidates for Channel components are LFO's or Echo. For example, a Voice-context LFO will reset each time, so it will give the exact same attack any time a note is hit. However, at the Channel level, the LFO will keep on going no matter what, so the attack will vary depending on the state of the LFO. 
 
    This feature is under development and may not yet be stable. You can make a component into a channel-context component by saying in the definition: 
 
        context: channel
        
    Channel-context components will not be paused like Voice-context components are when the voice stops sounding. Also, there will be only one of a given Channel-context component per Channel, rather than one per voice. And the "main" output of a Channel component will go directly to the limiter, rather than the Voice junction which is considered "main" for a Voice component. Otherwise the Channel component would include the output of all the voices from that channel in every voice's output from that channel, which would not work very well.      
 
A program will usually contain multiple components. The above has only one: a square wave generator by the name of `osc1` Ondes "helps" a little by adding an envelope if there is none, and then everything goes through a limiter before reaching the main out. The limiter on overload will display little diamond <> symbols, so if you see those, it is a good idea to address the issue. 

What a component will do is defined by a series of properties listed below the name key. The most important is **type**

 - **type** - what type of component this is.  These values are in the `ComponentMaker` switch. Currently, options are:
    - **wave**  - wave generator
    - **env** - envelope generator
    - **mix** - a junction between voices
    - **limiter** - keeps a signal from overflowing
    - **op-amp** - multiplies two signals together, so that one can control the output envelope of the other. For example an LFO or an Envelope can do amplitude modulation.
    - **filter** - alters the sound spectrum by changing the frequency response.

    
   More will follow in the future.
   
For the lists of parameters available, see the following files:

 - [Common](Common.md)
 - [WaveGen](WaveGen.md)
 - [Filter](Filter.md)  
 - [Envelope](Envelope.md)
 - [OpAmp](OpAmp.md)
 - [Controller](Controller.md)
 - [MidiNote](MidiNote.md)
 - [Mix](Mix.md)
    
 ----
 
       
    
   
 




