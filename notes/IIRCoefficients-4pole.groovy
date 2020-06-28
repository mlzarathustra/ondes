
//  MatLab:  
//
// These are all 4th order butterworth filters.
// [b,a] = butter(4, cutoff/(sampleRate/2)); freqz(b,a)
// 
//  below: a[cutoff] = (the result pasted in) 
//    same with b[cutoff]
//
//  The MatLab call is included. freqz plots the frequency
//  response, but is not required for the calculation.
// 
//  For IIRSpecLib they need to be pairs of arrays
//
//  { { a values... }, { b values } }, ...


def a=[:], b=[:]

a[220]='''
   1.00000
  -3.91809
   5.75762
  -3.76088
   0.92135'''

b[220]='''
   0.000000057928
   0.000000231713
   0.000000347570
   0.000000231713
   0.000000057928
'''







//////////////////////////////////////////////////




a[440]='''
   1.00000
  -3.83620
   5.52188
  -3.53454
   0.84887
'''

b[440]='''
   0.00000089052
   0.00000356210
   0.00000534315
   0.00000356210
   0.00000089052
'''


a[1000] = '''
   1.00000
  -3.62784
   4.95123
  -3.01192
   0.68889
'''
b[1000] = '''
   0.000021521
   0.000086084
   0.000129126
   0.000086084
   0.000021521
'''

a[10000]='''
   1.000000
  -0.363164
   0.527744
  -0.078017
   0.020241
'''

b[10000]='''
   0.069175
   0.276701
   0.415052
   0.276701
   0.069175
'''
   
//println "$a\n$b"   

a.keySet()sort().each { k->

   println '{'
   println '{'+ a[k].split(/\s+/).findAll{it}.join(', ') +'},'
   println '{'+ b[k].split(/\s+/).findAll{it}.join(', ') +'}'
   println '},'

}


