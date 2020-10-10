# Envelope Parameters  
  
An Envelope has two outputs: 
 - a signal attenuated by the envelope.
 - the envelope level itself 

The latter gives the level of the envelope in a specified range, and the former is the result of a set of incoming signals attenuated by the envelope, in other words the mathematical product of a set of incoming signals and the envelope level.  The former is for audio output, the latter for modulating other components. 

At this point, envelopes automatically listen to MIDI **note-on**, **note-off**, and **sustain**, as there is currently no other way to trigger them.  
  
## A few examples:

Very simple:
```yaml
env1: 
  type: env
  exit: true
  preset: clavier
```
The presets are more for getting a quick result than being flexible. 

For flexibility, you want to give the points of the curve, as a set of rates and levels. The rate is in milliseconds for a full sweep from 100 to 0 or vice versa. A less-than-full sweep won't last as long, but it's logarithmic rather than linear. The first part of the transit goes by more quickly.

A classic ADSR envelope can be as simple as a few elements:

```yaml
env2:
  type: env
  exit: true
  points:
    # columns: rate level [option]
    - 30 100 
    - 50 75  hold
    - 40 0
```

which might look like this: 

![](images/env-simple.png)

Above, the simplest OFF represents the first moment where both the key is up and the sustain pedal is up. It starts the transit to the point after `hold`. If there is no point after `hold`, it goes to zero immediately.  If the Note-OFF arrives before the envelope reaches to `hold` the behavior depends on the sustain pedal:
  - pedal is up: jump to the point after `hold` (or [0,0] if there is none)
  - pedal is down: jump to `alt-release` if it exists, or to `hold` if not.

---

You can add as many points as you like:

```yaml
env2:
  type: env
  exit: true
  points:
    # columns: rate level [option]
    - 7 100 
    - 6 75
    - 6 100
    - 4 25 
    - 10 50  hold 
    - 20 60
    - 10 30 
    - 40 0

``` 
It could look like this: 

![](images/env-complex.png)


Here's an example showing all of the keywords, **re-trigger**, **hold**, **release** and **alt-release**. If any of these appear, they _must_ appear in the exact order given. The dash in re-trigger and alt-release is ignored. In fact, dashes in keywords will always be ignored. So **retrigger** is the same as **re-trigger**. 

Remember that **#** indicates comments: 
```yaml
env3:
  type: env
  exit: true

  points:
    # columns: rate level [option]
    - 7 100 re-trigger # if not specified, it goes back to 0,0
    - 8 75
    - 10 60
    - 25 50 hold
    - 50 25 
    - 10 10 
    - 20 0
    # if the note releases before we get to hold, jump to here:
    - 50 10 release 
    - 20 0
    # Jump to "alt-release" after release, if sustain pedal is down
    - 100 0 alt-release

  out: main

  out-level: osc1.pwm
  out-level-amp: -1000,1000 
```

Two qualifiers may appear on the same step. For example, it can make sense for the **hold** and the **retrigger** to both be the same point, e.g.: 

```yaml
env1:
  type: env
  exit: true
  points:
    - 1000 100 hold retrigger
    - 1000 50
    - 5000 0
    - 1e9 0 alt-release

  out: main
```
 

## Two definitions used below:

 - **ON state** - The last Note message received was Note-ON, or the sustain pedal is down.
 - **OFF state** - The last note message was Note-OFF, and the sustain pedal is up 

OndeSynth will not layer two notes at the same pitch on the same channel. If you hold down sustain and repeat the note, or send two Note-ONs before a Note-OFF, it will retrigger the same voice. 


## The parameters:
  - **type** - for everything on this page, must be `env`
    
  - **exit** - If set, when this envelope finishes its cycle, it stops the voice from playing any longer. Default is false. Note that in YAML, the words 'on' and 'yes' also indicate 'true', as 'off' and 'no' indicate false.
    
  - **out** - the output of the signal attenuated by the envelope
  - **out-level** - the output of the envelope level, in a range given by the next parameter:
  - **out-level-amp** - the range of the output level. Can be either one or two numbers. If it's only one, the other limit defaults zero. The limits can be specified in either order. The 0-100 standard level of the envelope will be scaled to match the min and max levels given here. 
---
##Preset and Points
  - **preset** - either "preset" or "points" should be present, but not both. (so above, preset: is commented out.)  Current presets are: 
    - **clavier**
    - **organ**
    - **fade**
    
    The presets are mostly to simplify initial creation, so they're not meant to be flexible.
    
  - **points** - a set of points in three columns, the third being an optional qualifier. Either **points** or **preset** should be present, but not both. Each point should contain the following:
   
    - _rate_ - milliseconds to execute a full sweep from 0 to 100 or vice versa. See the discussion of rate below. 
    - _level_ (decimal) - a level from 0 to 100, inclusive. 0 &le; level &le; 100.
    - _qualifier_ (optional) - additional information about this point.
    
Qualifiers are one of: 
 - **re-trigger**     
 - **hold** 
 - **release**     
 - **alt-release** 
 
 They are all optional, but the ones that appear must be in the order given above. 
 
 Again, the dash in re-trigger and alt-release is ignored. In fact, dashes in keywords will always be ignored. So **retrigger** is the same as **re-trigger**.      
    
## how the envelope works    
The envelope may be divided into two phases, based on the **release** point, whether or not it's labeled. The release point is determined as follows: 

- if it's labeled, that point.    
- If it's not labeled, but there is a **hold** point, then the **release** is the point after it. 
- If neither is present, the **release** is the last point, or if **alt-release** exists, the last point before **alt-release**. If the last point given does not go to zero, a point will be added with rate and level zero, meaning an immediate cutoff.

The course of the envelope can thus be divided into two stages:
     
- _pre-release_ - everything up to the **release** point if it exists.     
- _release_ - starting with the transit to the **release** point, and everything after.

The way the sustain pedal works depends on which phase you are in. 
       
Starting with the note-ON, the envelope will traverse points up to the **hold** so long as either the note or the sustain pedal is down.       
        
If the voice reaches an OFF state (note UP and pedal UP) before the **hold** it will jump to the release point as described above.

During the _release_ phase, if **alt-release** exists, it will jump between **release** and **alt-release** according to whether the sustain pedal is UP or DOWN respectively.    
    
The most obvious use for **alt-release** is for the release to fade out, but the alt-release it to be a single point that goes to zero more slowly:
    
 ```yaml  
        - 5000 0 release # 5 second release
        - 1e9 0 alt-release # essentially, infinite hold
 ```    
So that with the pedal down, the decay will pause, but with the pedal up the decay will resume again.

##Qualifiers  
Here they are again, in order. Note that embedded dashes (-) are ignored, and included only for readability. 
     
- **re-trigger** - by default, a re-trigger (Note-ON arriving before this envelope has finished) will jump back to 0,0. If a re-trigger flag appears, it will jump back here instead.
- **hold** - if the envelope reaches this point and the note is still in an "ON" state it will remain at this point until we reach an "OFF" state (note UP and pedal UP)
- **release** - if the note reaches an OFF state before the hold (or there is no hold) we jump to here.  
- **alt-release** - In the _release_ phase, we jump to here if the pedal is DOWN, and to **release** if the pedal is UP. 

   
## Rate, meaning of

The curves are logarithmic, calculated by a simple formula that yields a logarithmic wave (weighting the attraction to the next value by how far away we are from it)

What that means is, the first half of the curve will go faster than the second half, and so on. 

A "rate" of 1000 means it takes 1000 milliseconds (1 second) to traverse from level 100 level 0. However, if you are traversing instead from 50 to 0 at a "rate" of 1000, it will take longer than a half a second, since the rate slows down as the level converges with its destination. 

 




