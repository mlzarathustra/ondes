
# Controller 7 (Volume Controller)


For voices that output in the `Voice` context (rather than the `Channel` context) a volume pedal transmitting changes for controller 7 (standard volume controller) will automatically adjust the volume on that channel. 

However, if the output is going to a `Channel` context output (say, an Echo) the volume pedal will have no effect, without adding a volume controller before it. So if you DON'T want the volume to adjust for that sound, send it through a Channel-context component.

If you DO want it to adjust, add a volume control as shown below (see [pwm.yaml](../program/pwm.yaml) for the full details)

Here is the YAML **before** adding the volume control:

```yaml

#  no volume control

env:
  type: env
  exit: true
  points:
    - 100 100 re-trigger
    - 25 92 hold
    - 300 0 release
    - 20000 0 alt-release
  out: echo

echo:
  type: echo
  context: channel
  amount: 40
  time: 124
  out: echo2




```

And here is where we can insert the volume control between the Voice-context component `env` and the Channel-context component `echo`: 

```yaml

env:
  type: env
  exit: true
  points:
    - 100 100 re-trigger
    - 25 92 hold
    - 300 0 release
    - 20000 0 alt-release
  out: ctrl7

#   A dynamic mix listening to midi volume
#   will be a volume control

ctrl7:
  type: dynamic-mix
  context: channel
  midi: volume
  out: echo

echo:
  type: echo
  context: channel
  amount: 40
  time: 124
  out: echo2



```

Again, for the full patch see [pwm.yaml](../program/pwm.yaml)

