
# Controller 

A very simple component that lets you direct output from a MIDI controller to the input of another component. 

The following example should be self-explanatory:

```
ctrl13:
  type: controller
  number: 13
  amp: 1000
  out: lpf.Q
```

 - **ctrl13** - the arbitrary label
 - **number** - the number of the MIDI controller to listen for.
 - **amp** - the amplitude of the output. Can be a single number, in which case the range is from 0 to <n>. Or two numbers, in which case the range will be from the min to the max.
 - **out** - where to direct the output.
 
# Common Controllers
 - 0 - bank select MSB
 - 1 - mod wheel
 - 7 - volume
 - 10 - pan
 - 32 - bank select LSB
 - 64 - sustain pedal
 
(#13 listed above is mapped to a slider on my keyboard controller) 
