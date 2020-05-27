# Ondes synth - to do

 - review WiredIntSupplierMaker logic - so far it only is used for "main" so make sure the other cases will work (e.g. when hooking an env generator in) 
  
 - multi-voice polyphony - could have a mode that re-uses the first available voice (for portamento &c.) 
 
 - Look into clicks at the end of sine waves. Taper off? 


 - Multi-channel (e.g. Stereo) components
   The input and output would be an int array 
   or a `long` for just two channels.
   
   ```java    
       Supplier<int[]> getOutputs() {
           
       }
 
       Consumer<int[]> getInputs() {
           
       }
   ```     

  
 ----------- 
 - EnvGen needs to be hooked to a "multiply" component (like a VCA) 
 
 - noteOFF routing / voice management - 
    - default: free the voice on note-OFF (as it is now)
    - If there's a designated envelope, free the voice when it is finished.
    - for now, only one envelope determines (rather than an AND or OR logic for several)
 
 -----------
  - Latency is a problem generally with computer sound, on account of needing to have a large buffer that needs to clear before it can respond.
   
    Look into using an ASIO MixerProvider `com.groovemanager.spi.asio`     referred from [here](http://jsresources.sourceforge.net/faq_misc.html#asio)
    
    The JSResources site looks informative in general:
    http://jsresources.sourceforge.net/  N.B. source code is dated 2003.
    
    [Dr. Dobbs on "latency" and "jitter."](https://djtechtools.com/2008/09/26/is-your-midi-controller-late/)
    Ableton also has a link to [ASIO for ALL](http://www.asio4all.org/)
    
    ASIO on wikipedia: "Audio Stream Input/Output (ASIO) is a computer sound card driver protocol for digital audio specified by **Steinberg,** providing a low-latency and high fidelity interface between a software application and a computer's sound card. Whereas Microsoft's DirectSound is commonly used as an intermediary signal path for non-professional users, ASIO allows musicians and sound engineers to access external hardware directly."
    
    Here's a project that promises ASIO for JavaSound: https://github.com/mhroth/jasiohost
         
    
 ----------- 

- Add a hierarchy of namespaces so we can create components at different levels 
    - global
    - channel 
    
    Currently, only the main mix is global, and there is no facility for channel-level components (e.g. LFO, reverb and other effects, ???)

 - I think the best policy on voice construction is to give a warning rather than halting. That opinion may well change with experience.  

 
