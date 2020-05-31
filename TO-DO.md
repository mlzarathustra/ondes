# Ondes synth - to do

 - PWM input should be separate from main input, which would be for frequency modulation.
 
     To that end, configure should be able to interpret dot-notation:    
     ```    
         out: osc1
         
         #  should be out: osc1.pwm 
         #  so we can have a separate input for frequency
     ```

 - velocity 
    - review all amplitude adjustments
    - is the pitch scaling right? it sounds about right.
    - figure out good adjustments for velocity sensitivity.
        - amount (0-100)
        - base (0-100) 
        - min(100, base + (vel * (amt/100)))
        - consider: the minimum the VS puts out is about 11, and it almost never gets to 128. So maybe find a way to spread out the limited set of values? (so that 11 might map to zero) 
        

 - wave gens 
     - need a level control; 
     - distinguish between it, and the ampOverride set using output-amp. 
    
 - add a low-pass filter near 20khz to reduce aliasing
   
 - pitch bend
 - LFO 
    - pitch mod
    - envelope mod (create an `OpAmp` component)

 - create a component that simply maps midi key to a value
    with an option for linear or logarithmic. 
   
 
 -----------
 - EnvGen needs to be hooked to a "multiply" component (`OpAmp`)
 
 - noteOFF routing / voice management - 
    - default: free the voice on note-OFF (as it is now)
    - If there's a designated envelope, free the voice when it is finished.
    - for now, only one envelope determines (rather than an AND or OR logic for several)

 - Taper off to avoid clicks at the end of sine waves.
 - Sustain pedal
 
 ------
 
 Channel components e.g. LFO
 Add a channel-global: on/off flag to components
 Keep them in ChannelVoicePool
  - It can hand them to VoiceMaker.getVoice() as a HashMap<String,MonoComponent>
  - which in turn, it passes to the Voice() constructor for the configure step.
 
  
 *****
 
 - review WiredIntSupplierMaker logic - so far it only is used for "main" so make sure the other cases will work (e.g. when hooking an env generator in)

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
----------------
 
 - migrate test case t8() from ondes.Test to wherever it should go.
 
 - (optional) manage pool from a separate thread

 
----------- 
 
 Use java.lang.StackWalker to implement "friend" restriction 
 for Component constructors.
 Each component type will have its own "friend," and nobody else
 should be allowed to call the constructor.
 
   - WaveGen - Wavemaker
   - EnvGen - EnvMaker
   - Junction, Limiter - ComponentMaker
 
  
 ```
     StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
     Optional<Class<?>> callerClass = walker.walk(s ->
         s.map(StackFrame::getDeclaringClass)
          .filter(interestingClasses::contains)
          .findFirst());
``` 
 
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

 
