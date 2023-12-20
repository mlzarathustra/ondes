# Mix


A mix is simply a target for a series of outputs, with level scaling. It's about the simplest component you could have.

It's not very exciting, but if you want to direct an output to two different places with different levels at each, a mix could be helpful.

```yaml
osc1: 
  type: wave
  midi: note-on
  shape: saw
  out: mix1, main

mix1:
  type: mix
  level-scale: .5
  out: osc2.linear

...


```