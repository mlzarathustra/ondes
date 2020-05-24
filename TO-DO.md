# Ondes synth - to do

- Program name matching.... the use of 'contains' is problematic when one voice name contains another one... there may be no way to select the  voice with the shorter name.


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


 
