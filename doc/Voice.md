
# Programming Patches

## Patching together components in YAML

 A `program` in OndeSynth is a set of specifications for a patch in YAML. If you don't know YAML, you can probably fake it by looking at the existing patch files. It's a hierarchical data format with in which indents indicate the hierarchy. Plentiful information exists about it online. 
 
 The application looks in two places for the YAML files: the resources directory `program` (a collection internal to the app, mostly basic patches like plain sine and sawtooth waves) and a directory called `program` relative to the working directory. It will parse subdirectories also, but only if the command line contains `-all-patches`. Subdirectories are good for test patches that you don't want to clutter up the main list with.
 
 The file name minus '.yaml' will be the patch name. If there are duplicate patch names, you can use `ondes -list-patches` and select by number instead of name.
 
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

 - **TBD** - there will in the future be components that reside at the voice/channel level, e.g. LFO's.  
 
 - Each component is a map named by a key global to the file. For example `osc1` above.

A program can contain multiple components. The above has only one: a square wave generator by the name of `osc1`

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

 - [Common parameters](Common.md)
 - [WaveGen parameters](WaveGen.md)
 - [Filter parameters](Filter.md)  
    
 --
 
       
    
   
 




