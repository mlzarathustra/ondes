package ondes.synth.filter.iir;

import java.util.Arrays;

import static java.lang.Math.*;
import static java.lang.System.out;

public class LowPassQFilter {

    /*

From dspprims.lsp

F:\shiva\Development\music\extermal\audacity\audacity\nyquist\dspprims.lsp


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

     */
    /*
            b[0] and b[2] match the butterworth values,
            but the middle term does not.
     */
    static void makeFilter(double freq, double Q) {

        double sampleRate = 44100;

        double[] a = new double[3], b = new double[3];
        double omega = 2 * PI * (freq / sampleRate);
        double alpha = sin(omega) * sinh(0.5 / Q);

        a[0] = 1.0 + alpha;  //  todo - what happens to a[0]?
        a[1] = -2.0 * cos(omega);
        a[2] = 1.0 - alpha;

        b[1] = 1 - cos(omega);
        b[0] = b[2] = 0.5 * b[1];

        out.println("freq: "+freq+" Q:"+Q);
        out.println("omega: "+omega+" alpha: "+alpha);
        out.println("a="+ Arrays.toString(a));
        out.println("b="+ Arrays.toString(b));
    }

    public static void main(String[] args) {

        /*  seems to max out at about a 25dB boost.
                Q=... (all are estimates) at 1000hz
                    .5 - no resonance
                     1 -   2dB
                     2 -   5
                     3 -   8
                     20 -  25
                     40 -  27
                     100 - 30
                     1000 - about the same
         */

        makeFilter(1000, 1000);


    }






}
