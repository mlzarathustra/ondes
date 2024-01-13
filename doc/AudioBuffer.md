## audio buffer size

Why the keyboard is so sluggish to respond: it's inherent in the design of computer sound, which is geared for playback of already-existing signals, e.g. a CD. In such applications, a delay is unimportant, so long as everything is delayed by the same amount. However, when the system has to respond to timed input, it can do nothing until the current buffer is emptied. Hence the delay. 

You can set the buffer size with the following argument to the ondes.App class:
  -buffer-size <size>
  
If you get nothing but clicks or sound with breaks in it, the audio buffer needs to be bigger. Default is 2048, which should work with most systems.

For computer-based sound, the signal needs to be sent in big chunks, namely the buffer. If the buffer is too small, the audio system can't send the bytes fast enough and you get clicks or breaks as described above.

The problem with making the buffer bigger is that whenever you trigger a note, the system needs to wait for the current buffer to be processed before anything new can emerge. Since a buffer of about 2048 samples seems to be generally required, that means a delay of up to 2048/44100 seconds before the note begins to sound. How long that delay is will depend on where it happens to be in filling the buffer when you hit the note. 

The delay is called "latency." The variation in the delay is called "jitter." 

2048/44100 is about  0.0464399 seconds or 46 milliseconds. It's quite palpable. 

It's possible ASIO might help with this problem, but JavaSound offers no official support. Also, I've had the same trouble with ASIO, so it may not even help. For more on the gory details, see: http://jsresources.sourceforge.net/faq_misc.html#asio  
  