
# Programming Patches

## Patching together components in YAML

 A `program` in OndeSynth is a set of **Components** connected together. I use the term `patch` interchangeably with `program.` A `Voice` on the other hand is a program that has been instantiated into an Object in order to play music. The distinction is somewhat loose. 
 
 It is represented as a set of specifications in YAML. If you don't know YAML, it's easy to pick up, and you can probably fake it by looking at the existing program files (for example in the `program` directory). YAML is a hierarchical data format with in which indents indicate the hierarchy. Plentiful information exists about it online. 
 
 The application looks in two places for the YAML files: a collection internal to the app, mostly basic patches like plain sine and sawtooth waves (in the resources directory `program`) and a directory called `program` relative to the working directory. It will parse subdirectories also, but only if the command line contains `-all`. Subdirectories are currently for test patches that you don't want to clutter up the main list with.
 
 The file name minus '.yaml' will be the patch name. If there are duplicate patch names, you can use `ondes -list` and select by number instead of name.
 
The engine uses the YAML specifications to create a `Voice.` The `ChannelVoicePool` class manages them, creating them beforehand to avoid garbage collection delays. 
 
## example patch (YAML) 
Here is a patch with a single component, of type 'wave' (also called "WaveGen" or "wave generator")
 ```
 osc1:
   midi: note-on   # send this component note ON messages
   detune: 7
   offset: -12 
   type: wave
   shape: square
   out: main
```

In fact, the above is a valid patch that will play back square waves an octave (12 half steps) below the key pitch, detuned by 7 cents above the pitch. It will automatically get an organ-style envelope.

## Components 
Each component is a Map named by a key (within the main Map), which must be global to the file. For example `osc1` above. The requirement for it to be unique is part of the YAML spec.
 
### context

Understanding that a `Channel` consists of multiple `Voices,` Components can reside in either the `Voice` or the `Channel` context. Most will reside in the **Voice** context, and that is the default. Likely candidates for **Channel** components are LFO's or Echo.
 
Another likely **Channel** component is a fixed-frequency IIR filter, as it seems the double precision Math can tend to overload and cause gaps.  
  
 A Voice-context LFO will reset each time, so it will give the exact same shape any time a note is hit. However, at the Channel level, the LFO will keep on going no matter what, so the shape will vary depending on the state of the LFO when the note is triggered. 
 
You can make a component into a channel-context component by saying in the definition: 
 
        context: channel
        
Channel-context components will not be paused like Voice-context components are when the voice stops sounding. Also, there will be only one of a given Channel-context component per Channel, rather than one per voice. 

And the "main" output of a Channel component will go directly to the limiter, rather than the Voice junction which is considered "main" for a Voice component. Otherwise the Channel component would include the output of all the voices from that channel in every voice's output from that channel, which would not work very well.

TODO - soon the above will change slightly - the output of both will go into a channel-level junction used for controller 7 volume control.

### programs        
 
A program will usually contain multiple components. The above has only one: a square wave generator by the name of `osc1` Ondes "helps" a little by adding an envelope if there is none, and then everything goes through a limiter before reaching the main out. The limiter on overload will display little diamond <> symbols, so if you see those, it is a good idea reduce the amplitude being output by your component.  

What a component will do is defined by a series of properties listed below the name key. The most important is **type**

 - **type** - what type of component this is.  These values are in the `ComponentMaker` switch. Currently, options are:
    - **wave**  - wave generator
    - **env** - envelope generator
    - **mix** - a junction between voices
    - **limiter** - keeps a signal from overflowing
    - **op-amp** - multiplies two signals together, so that one can control the output envelope of the other. For example an LFO or an Envelope can do amplitude modulation.
    - **filter** - alters the sound spectrum by changing the frequency response.
    - **balancer** - takes two inputs, and a value indicating the balance of amplitude applied to each. 
    
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
 - [Balancer](Balancer.md)


## Editing Waves 

There are several ways to generate more complex waveforms than the usual sine, square, and so on. 

For example:
 
1. Harmonic wave forms can be created using the [wave editor](WaveEditor.md), which outputs YAML text that you can paste into a program specification. 

1. FM, using linear modulation as does the DX-7. Soon there will be better ways to monitor FM settings so you can more easily capture the parameters for a sound you are hearing. 

    
 ----
 
       
    
   
 




