# Midi-Note

A MIDI note component outputs either the midi note number or the frequency of that note. It will want to be scaled to what the input is expecting.

The naming may seem counter-intuitive: the logarithmic output is the note **number** whereas the linear output is the note **frequency**. It's probably the latter you want in most cases. 

For example, in the "brass" patch you'll see:

```
midi-note:
  type: midi-note
  linear-out:
    amp: 1000
    out: lpf.freq
```

to cause the filter frequency to follow the note frequency. 
