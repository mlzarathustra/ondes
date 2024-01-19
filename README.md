# OndeSynthesizer

A synthesizer that lets you plug together arbitrary components, which can be configured using a plain text editor and YAML.

Many key features are working now, including DX-7 style FM and a resonant sweeeping filter (2-pole).

To hear some of what you can do so far:
[OndeSynth demo on YouTube](https://www.youtube.com/playlist?list=PLk0M1i4FJYGJ04WGUqPTOEU0NjMNrJEqt)

There is extensive documentation starting at [Voice.md](doc/Voice.md) and there are a number of example patches included.
 
![](doc/images/ondes-synth-architecture-diagram.png)
For more diagrams, see [Diagrams.md](doc/Diagrams.md)

## synthesizer
The concept is a fully modular synthesizer that will play through your computer's output device as triggered by a MIDI input device, for example an electronic keyboard. 

Programs (patches) are represented by YAML files. There are numerous examples included, and all that one needs is a text editor to create more.  

## requirements

- Java 11 - can be obtained from [the open jdk project](https://openjdk.java.net/projects/jdk/11/)
- Gradle - from [gradle.org](https://gradle.org/)
- a MIDI keyboard connected to the computer
- an audio output system (e.g. speakers)
- [CygWin](http://cygwin.org/), or other Bash shell.  

There is no official packaging, so simply download or clone the project and run `gradle uberJar` or use the `b` bash script to set up the jar for the shell scripts to run.

I assume that `.` is on the path, so if it's not on yours, you'll have to type ./ a lot more.

I have so far only tested using Windows, but it should work with any system supported by JavaSound.

## running OndeSynth

Start up the synth with a default patch on all 16 MIDI channels by using the `o` script in the main directory. For example:

        o -in 828 -out "main out" 

On my system, the MOTU 828 will be the midi input, and the MOTU "main out" will be the output. You can also specify patches to use for each channel. Type `o` by itself for the usage information.

## recording to WAV

Alternately, you can record to WAV from a Midi file, with a syntax of

    p <sequence>.mid <output>.wav 

The `p` script also has options, which you can see by typing it by itself on the command line. 


## Other tools

You may need to use the tools provided (`midiInfo` and `audioInfo`) to figure out what to tell JavaSound for the -in and -out options above. 

Once you settle upon inputs and outputs, you may want to put them in the file `ondes-args` so you don't have to keep typing them.

If you're in a bash shell (including [CygWin](http://cygwin.org/)) you can use the below commands to run the tools. If not, you can look at those files to figure out how to run the java class. 

`midiInfo` - shows MIDI devices and their transmitters and receivers.

`midiMon` - monitors MIDI messages on a given device, or the default device if none is specified. Use the LABEL field from `midiInfo` to specify a device.  A good way to check that Java is hearing your keystrokes. 

`audioInfo` - shows Audio devices and their "source" and "target" lines.

`w` - is a wave editor, that allows you to interactively compose harmonics into a waveform that can then be used to specify a `harmonic` wave oscillator.  The Wave Editor can save the result into YAML format for copying and pasting into a patch file. 




---
### bank scripts
There are a series of bash scripts (in the `scripts` directory) that tell the app to load different sounds into the 16 channel voices and wait for MIDI input. You will probably need to adjust the -in and -out parameters for your own system.

---
### misc info

See [JavaSoundNaming.md](doc/JavaSoundNaming.md) for an explanation of how JavaSound names inputs and outputs, which is distinctly confusing.

Given the state of computer sound responding to MIDI (sluggish) this app is generally not suitable for live performance of anything requiring timing. However, it should still be useful in cases where timing is not critical.  Please see [AudioBuffer.md](doc/AudioBuffer.md) for more on the latency responding to MIDI.

This is partly due to the lack of support for JavaSound, meaning that it is still using the older Windows API's rather than the more efficient WSAPI calls.


---
## programming patches

Next you'll probably want to check out the files in the `doc` directory, starting with: 

[Voice.md](doc/Voice.md)






