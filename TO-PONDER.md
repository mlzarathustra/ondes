

 --- 
 - optimize the limiter by clearing the buffer when we reach a new max. Verify that it's actually faster that way before installing, though. 
 - manage ChannelVoicePool from a separate thread - add voices when the available reserve gets low


-------------
 
 - amplitude gets cast to int. Should it? 
 
 - skip phase clock for noise? NOTE: without MIDI note-ON, it still plays, but does not process velocity.
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

 
