# Ondes Synthesizer
(under construction)

## synthesizer
The plan is for this to become a fully modular synthesizer that will play back through your computer's output device as triggered by a MIDI input device (typically an electronic keyboard, though any should work)

Programs (patches) are represented in YAML files so that you will be able to either use presets or roll your own. 

It's in progress, so check back soon!

## tools
Here you'll find some tools to show you what JavaSound thinks your system looks like.

If you do a `gradle build` and you're running cygwin, you can use the below commands to run the tools. If not, you can look at those files to figure out how to run the java class. 

`midiInfo` - shows MIDI devices and their transmitters and receivers.

`midiMon` - monitors MIDI messages on a given device, or the default device if none is specified. Use the LABEL field from midiInfo to specify a device.  

`audioInfo` - shows Audio devices and their "source" and "target" lines.

The naming `JavaSound` employs is confusing, as it is expressed from the perspective of the mixer or outside device rather than that of the application: 

 - MIDI 
    - transmitter - The application uses to receive MIDI messages, from a keyboard or sequencer
    - receiver - The application uses to send MIDI messages for playback, to a synth or sequencer
    
- AUDIO 
    - source - The application uses to play back sounds by sending audio data 
    - target - The application uses to receive audio data fed in from a sound or audio signal source.  
 
For the purposes of the app, I am sticking as close to the original labeling as possible, while at the same time doing my best to abstract away some of the busy work.  



