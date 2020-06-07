# Ondes Synthesizer
(under construction)
 
![](notes/images/ondes-synth-architecture-diagram.png)


## synthesizer
The concept is a fully modular synthesizer that will play through your computer's output device as triggered by a MIDI input device, for example an electronic keyboard. 

Programs (patches) are represented by YAML files. There are numerous examples included, but with YAML, it is easy to create new ones.

It currently works on my system by using the `run` script in the main directory like this:

        run -in 828 -out "main out" -ch1 octave -ch2 10

You may need to use the tools provided (`midiInfo` and `audioInfo`) to figure out what to tell JavaSound for the -in and -out options above. Type `run` with no arguments for command line help.

(This is all assuming you ran Gradle to build the jar file. See below)

Given the state of computer sound responding to MIDI (sluggish) this app is generally not suitable for live performance of anything requiring timing. However, it should still be useful in cases where timing is not critical.  Please see "audio buffer size" below for more on the latency responding to MIDI.  

Yet to be created are the Envelope generation and friends (e.g. multidimensional panning envelopes). At present, the Wave Generators and some basic IIR filtering are fairly solid within the framework.

See [the to-do file](TO-DO.md) for more on what is in the pipeline.  

## requirements 

 - Java 11 - can be obtained from [the open jdk project](https://openjdk.java.net/projects/jdk/11/)
 - Gradle - from [gradle.org](https://gradle.org/)
 - a MIDI keyboard connected to the computer
 - an audio output system (e.g. speakers)

 
There is no official packaging, so simply download or clone the project and run `gradle uberJar` or use the `b` bash script to set up the jar for the shell scripts to run.  


## tools
Included are some tools to show you what JavaSound thinks your system looks like. (after you build the uberJar using `gradle uberJar` or the `b` bash script)

If you're in a bash shell (including [cygwin](http://cygwin.org/)) you can use the below commands to run the tools. If not, you can look at those files to figure out how to run the java class. 

`midiInfo` - shows MIDI devices and their transmitters and receivers.

`midiMon` - monitors MIDI messages on a given device, or the default device if none is specified. Use the LABEL field from `midiInfo` to specify a device.  

`audioInfo` - shows Audio devices and their "source" and "target" lines.

---
### bank scripts
There are a series of bash scripts (in the `scripts` directory) that tell the app to load different sounds into the 16 channel voices and wait for MIDI input. You will probably need to adjust the -in and -out parameters for your own system.

---
## programming patches

Next you'll probably want to check out the files in the `doc` directory, starting with [Voice.md](doc/Voice.md)


---
## audio buffer size

Why the keyboard is so sluggish to respond: it's inherent in the design of computer sound, which is geared for playback of already-existing signals, e.g. a CD. In such applications, a delay is unimportant, so long as everything is delayed by the same amount. However, when the system has to respond to timed input, it can do nothing until the current buffer is emptied. Hence the delay. 

You can set the buffer size with the following argument to the ondes.App class:
  -buffer-size <size>
  
If you get nothing but clicks or sound with breaks in it, the audio buffer needs to be bigger. Default is 2048, which should work with most systems.

For computer-based sound, the signal needs to be sent in big chunks, namely the buffer. If the buffer is too small, the audio system can't send the bytes fast enough and you get clicks or breaks as described above.

The problem with making the buffer bigger is that whenever you trigger a note, the system needs to wait for the current buffer to be processed before anything new can emerge. Since a buffer of about 2048 samples seems to be generally required, that means a delay of up to 2048/44100 seconds before the note begins to sound. How long that delay is will depend on where it happens to be in filling the buffer when you hit the note. 

The delay is called "latency." The variation in the delay is called "jitter." 

2048/44100 is about  0.0464399 seconds or 46 milliseconds. It's quite palpable. 

It's possible ASIO might help with this problem, but JavaSound offers no official support. Also, I've had the same trouble with ASIO, so it may not even help. For more on the gory details, see: http://jsresources.sourceforge.net/faq_misc.html#asio  
  

## JavaSound naming convention


The naming Java Sound employs is confusing, as it is expressed from the perspective of the mixer or outside device rather than that of the application: 

 - MIDI 
    - transmitter - The application uses to receive MIDI messages, from a keyboard or sequencer
    - receiver - The application uses to send MIDI messages for playback, to a synth or sequencer
    
- AUDIO 
    - source - The application uses to play back sounds by sending audio data 
    - target - The application uses to receive audio data fed in from a sound or audio signal source.  
 
For the purposes of the app, I am sticking as close to the original labeling as possible, while at the same time doing my best to abstract away some of the busy work.  



