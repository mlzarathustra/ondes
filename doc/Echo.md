# Echo

Echo exists currently at either the voice level or the channel level.

At the voice level, the effect will be per note played, and will end (and be reset) when the note stops playing.  At the channel level, it will have one instance per channel that will be constantly listening.

 - **amount** - percentage to add into the loop
 - **time** - delay time in milliseconds
 
The **input-amount** and **input-time** allow for modulation of the same.

If you change the delay time dynamically, it will silence the entire loop. 

```
echo:
  type: echo
  amount: 40
  time: 250

  input-amount:
    amp: 1000
    percent: 50

  input-time:
    amp: 1000
    ms: 3000

  out: env2

```