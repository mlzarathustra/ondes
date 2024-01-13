
# OpAmp 

The OpAmp basically multiplies together its inputs. 

That means you can input an envelope level (say from an LFO) and a signal, and it will attenuate the signal using the envelope.

That also means you can use it as a ring modulator. If two or more audio signals are both signed, the result will be a ring modulation.

An `op-amp` has three parameters, as follows:

```yaml
vca:
  type: op-amp
  level-scale: 1.5
  out: lpf 
```

 - **type** - must be op-amp. 
 - **level-scale** - a floating point number by which the output is multiplied. 
 - **out** - the label of the output component, same as with any component. 