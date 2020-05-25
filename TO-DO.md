# Ondes synth - to do

 - reset visited flag on all WiredIntSuppliers for each sample. The list is currently at the Voice level, so `OndesSynth` needs to get all the currently sounding voices and call `resetWires();` Rather than churn through all of the 2048 possibilities in OndesSynth.programs, let's maintain a set with the active ones parallel to the matrix.
  
 - at the start, create a frequency table on the fly (may as well - it's fast)
  
 - and of course, use it to set the note frequency. Does the "midi: true" flag make sense? 
 
 
 ----------- 
  
 - I think the best policy on voice construction is to give a warning rather than halting. That opinion may well change with experience.  



 - Multi-channel (e.g. Stereo) components
    The input and output would be an int array
    
    ```java    
        Supplier<int[]> getOutputs() {
            
        }

        Consumer<int[]> getInputs() {
            
        }
    ```     

- Add a hierarchy of name spaces so we can components at different levels 
    - global
    - channel 
    
Currently, only the main mix is global, and there is no facility for channel-level components (e.g. LFO, reverb and other effects, ???)


 
