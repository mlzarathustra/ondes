
/*
  ramp-down: every harmonic, amplitude = 1/n
  square: odd harmonics, amplitude = 1/n
  triangle: odd harmonics, amplitude = 1/n^2
      inverting the phase of every other one 
 */

//  triangle wave 
boolean dash=false
[*1..64].each { 
  if (it%2) { 
    println " - $it ${dash?'-':''}${it*it}" 
    dash = !dash
  }
}