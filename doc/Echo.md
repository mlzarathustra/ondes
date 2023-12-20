# Echo

Echo exists currently at the voice level. Which means that the echo will end (and be reset) when the note stops playing. So for now, it works to create a longer envelope for the echo separate from the shorter one for the note. Be sure to mark the first one with "exit: true"

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