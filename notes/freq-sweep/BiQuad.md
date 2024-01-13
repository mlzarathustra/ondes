
# BiQuad filter thoughts

bi-quad = two quadratics, quadratics being a second order equation.

There are a couple of new PDF's I downloaded on filtering. 
The general consensus seems to be that BiQuads perform poorly
at low frequencies, whereas state-variable filters perform 
poorly at high frequencies. 

State-variable filters have many fewer coefficients, thus should
be easier to sweep. 

## Nyquist code


From [dspprims.lsp](F:\shiva\Development\music\extermal\audacity\audacity\nyquist\dspprims.lsp)

```lisp
; two-pole lowpass
(defun lowpass2 (x hz &optional (q 0.7071) (source "LOWPASS2"))
  (multichan-expand source #'nyq:lowpass2
    '(((SOUND) "snd") ((POSITIVE) "hz") ((POSITIVE) "q") ((STRING) "source"))
    x hz q source))
;; NYQ:LOWPASS2 -- operates on single channel
(defun nyq:lowpass2 (x hz q source)
  (if (or (> hz (* 0.5 (snd-srate x)))
          (< hz 0))
      (error "cutoff frequency out of range" hz))
  (let* ((w (* 2.0 Pi (/ hz (snd-srate x))))
         (cw (cos w))
         (sw (sin w))
         (alpha (* sw (sinh (/ 0.5 q))))
         (a0 (+ 1.0 alpha))
         (a1 (* -2.0 cw))
         (a2 (- 1.0 alpha))
         (b1 (- 1.0 cw))
         (b0 (* 0.5 b1))
         (b2 b0))
    (nyq:biquad-m x b0 b1 b2 a0 a1 a2 source)))
```
```
;  a0 gets normalized, and eventually ignored.
;
(defun nyq:biquad-m (x b0 b1 b2 a0 a1 a2 &optional (source "BIQUAD-M"))
  (nyq:biquad x b0 b1 b2 a0 (- a1) (- a2)))
. . .
(defun nyq:biquad (x b0 b1 b2 a0 a1 a2)
  (ny:typecheck (<= a0 0.0)
    (error (format nil "In BIQUAD, a0 < 0 (unstable parameter a0 = ~A)" a0)))
  (let ((a0r (/ (float a0))))
    (setf a1 (* a0r a1) 
          a2 (* a0r a2))
    (ny:typecheck (or (<= a2 -1.0) (<= (- 1.0 a2) (abs a1)))
        (error (format nil 
         "In BIQUAD, (a2 <= -1) or (1 - a2 <= |a1|) (~A a1 = ~A, a2 = ~A)"
         "unstable parameters" a1 a2)))
    (snd-biquad x (* a0r b0) (* a0r b1) (* a0r b2) 
                  a1 a2 0 0)))
```
```
; [comment found in file:]

;;; fixed-parameter filters based on snd-biquad
;;; note: snd-biquad is implemented in biquadfilt.[ch],
;;; while BiQuad.{cpp,h} is part of STK
```
```
;  Above, biquadfilt.[ch] means the two files (~.c and ~.h)
;  in biquadfilt.c, we find the below. Note that, sure enough,
;  they are ADDING the 'a' coefficients rather than subtracting.
; 
; Below is using direct form 2, but assuming that the "a" coefficients
; are already negated. 
...
	if (n) do { /* the inner sample computation loop */
            double z0;            z0 = *s_ptr_reg++ + a1_reg*z1_reg + a2_reg*z2_reg;
            *out_ptr_reg++ = (sample_type) (z0*b0_reg + z1_reg*b1_reg + z2_reg*b2_reg);
            z2_reg = z1_reg; z1_reg = z0;
	} while (--n); /* inner loop */
...
```

---

## Earlevel code 
The below is from the 
[earlevel biquad source in C](https://www.earlevel.com/main/2012/11/26/biquad-c-source-code/)

Note that he swaps A and B.

Quotes:
"The direct forms are terrible choices if you want a
sweepable synthesizer filter, for instance..."

He advocates the "Chamberlin state-variable filter
"I don’t suggest the Chamberlin SVF as-is—it’s only adequate
with oversampling, and there are much-improved versions..."
and he promises a blog post on them (but so far doesn't seem to have)

```c++
    double norm;
    double V = pow(10, fabs(peakGain) / 20.0);
    double K = tan(M_PI * Fc);

    //  (was part of a switch - lowpass)

    norm = 1 / (1 + K / Q + K * K);
    a0 = K * K * norm;                  // NOTE! this author
    a1 = 2 * a0;                        // SWAPS A and B!!    
    a2 = a0;
    b1 = 2 * (K * K - 1) * norm;
    b2 = (1 - K / Q + K * K) * norm;
    break;


    inline float Biquad::process(float in) {
        double out = in * a0 + z1;
        z1 = in * a1 + z2 - b1 * out;
        z2 = in * a2 - b2 * out;
        return out;
    }
```
