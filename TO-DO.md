# Ondes synth - to do

 - reset visited flag on all WiredIntSuppliers for each sample. That means (you guessed it) a `WiredIntSupplierMaker` class at the Synth level that will keep track of all of them. 
 
 - at the start, create a frequency table on the fly (may as well - it's fast)
  
 - and of course, use it to set the note frequency.
 
 
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


 
