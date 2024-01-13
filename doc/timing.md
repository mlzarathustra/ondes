

# Timing

If you want to understand how long the process is taking versus the time the `srcLine.write()` function is blocking,
you can set the `VERBOSE` flag in MonoMainMix to true, and it will generate (overwrite) a log file called
`update.log`

A sample (note - the format may change slightly in the future, to make it clearer): 
```
srcLine.write() rs=8192   delta >> 43,463,900 ns    write took 21,977,300 ns    process 21,486,600 ns
srcLine.write() rs=8192   delta >> 46,493,200 ns    write took 32,959,800 ns    process 13,533,400 ns
srcLine.write() rs=8192   delta >> 47,670,700 ns    write took 33,484,300 ns    process 14,186,400 ns
srcLine.write() rs=8192   delta >> 45,964,600 ns    write took 22,008,700 ns    process 23,955,900 ns
srcLine.write() rs=8192   delta >> 44,999,500 ns    write took 33,585,000 ns    process 11,414,500 ns
srcLine.write() rs=8192   delta >> 45,954,200 ns    write took 21,470,600 ns    process 24,483,600 ns
srcLine.write() rs=8192   delta >> 44,015,700 ns    write took 33,574,100 ns    process 10,441,600 ns
srcLine.write() rs=8192   delta >> 45,978,200 ns    write took 21,635,000 ns    process 24,343,200 ns
srcLine.write() rs=8192   delta >> 49,024,200 ns    write took 34,930,000 ns    process 14,094,200 ns
srcLine.write() rs=8192   delta >> 45,707,400 ns    write took 32,859,900 ns    process 12,847,500 ns
srcLine.write() rs=8192   delta >> 44,992,200 ns    write took 33,802,000 ns    process 11,190,200 ns
srcLine.write() rs=8192   delta >> 56,318,300 ns    write took 45,157,000 ns    process 11,161,300 ns
srcLine.write() rs=8192   delta >> 36,272,800 ns    write took 10,651,800 ns    process 25,621,000 ns
srcLine.write() rs=8192   delta >> 44,103,500 ns    write took 32,651,200 ns    process 11,452,300 ns
srcLine.write() rs=8192   delta >> 55,994,900 ns    write took 44,613,300 ns    process 11,381,600 ns
srcLine.write() rs=8192   delta >> 40,560,800 ns    write took 34,574,800 ns    process 5,986,000 ns
srcLine.write() rs=8192   delta >> 50,446,400 ns    write took 44,899,000 ns    process 5,547,400 ns
srcLine.write() rs=8192   delta >> 50,566,900 ns    write took 44,607,500 ns    process 5,959,400 ns
```

 - `rs=` the number of bytes written, as returned from `srcLine.write()` 
 It seems to always write the whole buffer.
 - `delta` - the time in nanoseconds since the last write
 - `write took` - how long `srcLine.write()` blocked
 - `process` - how long it took the synthesizer to fill the buffer

The `delta` should equal the `write took` number plus the `process` number.

 
## How bufferSize translates to elapsed time between writes

Let's imagine a buffer of size 1024 points. Each point takes up
one "frame," which is the number of bytes sent to the source line 
for each time-slice. For 16-bit stereo, that means 4 bytes per slice,
thus the buffer size in bytes is 

    4 * 1024 = 4096. 

(Each point is an integer, of which we only use 16 bits
copied into stereo for 32 bits).

The `rs=8192` above indicates a buffer of 2048, which seems about 
the minimum for reliability.

Let's imagine instead, a buffer
holding 2048 samples: 
 
    44100 / 2048 ==> 21.53 buffer writes per second
    1 / 21.53 ==> 0.046446 seconds
     
so, with a buffer of 2048, it has about 46 milliseconds
to compute the entire next buffer between buffer writes.

The timings above all look fairly safe, as none of the process 
cycles takes more than about 26 milliseconds, and you can see it
blocking longer when the process returns more quickly. 


However if the process starts taking 80 ms between slices,
it can upset the whole system, and then you'll get a bunch of gaps.
And with the 1K buffer it's much more likely that the process 
will hang somewhere long enough to miss the deadline. (for example,
TreeMap management?)


  


