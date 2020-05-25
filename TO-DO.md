# Ondes synth - to do

 - and of course, use FreqTable.getFreq(int midiNum) to set the note frequency. 
 - Does the "midi: true" flag make sense? Not all WG's will want the frequency set to the note. Maybe instead: 
 
        freq: midi # or frequency: midi 
 
 - noteOFF routing / voice management - 
    - default: free the voice on note-OFF 
    - If there's a designated envelope, free the voice when it is finished.
    - for now, only one envelope determines (rather than an AND or OR logic for several)
       
 ----------- 
 - EnvGen needs to be hooked to a "multiply" component (like a VCA) 
    
 ----------- 
 - multi-voice polyphony - could have a mode that re-uses the first available voice (for portamento &c.) 


 - Multi-channel (e.g. Stereo) components
    The input and output would be an int array 
    or a `long` for just two channels.
    
    ```java    
        Supplier<int[]> getOutputs() {
            
        }

        Consumer<int[]> getInputs() {
            
        }
    ```     

- Add a hierarchy of namespaces so we can create components at different levels 
    - global
    - channel 
    
    Currently, only the main mix is global, and there is no facility for channel-level components (e.g. LFO, reverb and other effects, ???)

 - I think the best policy on voice construction is to give a warning rather than halting. That opinion may well change with experience.  

 
