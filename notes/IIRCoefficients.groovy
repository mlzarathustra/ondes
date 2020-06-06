
//  MatLab:  
//
// These are all 6th order butterworth filters.
// [b,a] = butter(6, cutoff/(sampleRate/2)); freqz(b,a)
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

// [b,a] = butter(6, 10000/(44100/2)); freqz(b,a)
a[10000]='''
   1.0000000
  -0.5517678
   0.8913623
  -0.2718709
   0.1442227
  -0.0188095
   0.0023745
'''

b[10000]='''
   0.018680
   0.112079
   0.280198
   0.373597
   0.280198
   0.112079
   0.018680   
'''

// [b,a] = butter(6, 15000/(44100/2)); freqz(b,a)
a[15000]='''
   1.000000
   2.144005
   2.505855
   1.690254
   0.699779
   0.162362
   0.016504
'''

b[15000]='''
   0.12842
   0.77051
   1.92627
   2.56836
   1.92627
   0.77051
   0.12842
''' 

//  [b,a] = butter(6, 20000/(44100/2)); freqz(b,a)
a[20000]='''
    1.00000
    4.87228
    9.97981
   10.98805
    6.85320
    2.29419
    0.32187
'''

b[20000]='''
    0.56733
    3.40401
    8.51002
   11.34669
    8.51002
    3.40401
    0.56733
'''
  
   
//println "$a\n$b"   

a.keySet().each { k->

   println '{'
   println '{'+ a[k].split(/\s+/).findAll{it}.join(', ') +'},'
   println '{'+ b[k].split(/\s+/).findAll{it}.join(', ') +'}'
   println '},'

}






