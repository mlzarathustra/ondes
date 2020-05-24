# Ondes synth - to do

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


 