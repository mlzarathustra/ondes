
## JavaSound naming convention


The naming Java Sound employs is confusing, as it is expressed from the perspective of the mixer or outside device rather than that of the application: 

 - MIDI 
    - **transmitter** - The application uses to receive MIDI messages, from a keyboard or sequencer
    - **receiver** - The application uses to send MIDI messages for playback, to a synth or sequencer
    
- AUDIO 
    - **source** - The application uses to play back sounds by sending audio data 
    - **target** - The application uses to receive audio data fed in from a sound or audio signal source.  
 
For the purposes of the app, I am sticking as close to the original labeling as possible, while at the same time doing my best to abstract away some of the busy work.  
