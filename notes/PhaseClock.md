
#PhaseClock

PhaseClock is geared towards wavetable playback rather than sample playback. 

It doesn't keep track of the instant in time; However, if the sample is expressed in wave cycles, it can still work by allowing the phase to exceed the range 0<phase<1


Keeping track of the absolute time will lose precision eventually. For example, after a day: 
```
jshell> double inc=1.0/44100
inc ==> 2.2675736961451248E-5

jshell> long day=44100L * 60 * 60 * 24
day ==> 3810240000

jshell> inc + day
$6 ==> 3.810240000000023E9
```
so $6 above has lost all of the precision of `inc`.
Whereas keeping the sample number as a long has plenty of head room: 

```
jshell> 1L<< 62 // max value of long
$26 ==> 4611686018427387904

jshell> 44100L * 60 * 60 * 24 // samples / day
$27 ==> 3810240000
```

Which will take a while to overflow.

---
So the strategy will be to simply increment the phase with delta and keep it in the range between 0 and 1. Which
 should keep the precision about as best as possible. 


