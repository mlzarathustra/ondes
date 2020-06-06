package ondes.synth.filter.iir;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

import static java.lang.System.out;

/**
 * <p>
 *     Convert the Wn parameter from MatLab's butterworth
 *     function into frequencies. Wn = fc/(fs/2) where fc
 *     is the cutoff frequency and fs is sample rate.
 * </p>
 * <p>
 *     It's some multiple of the angular frequency and pi,
 * </p>
 * <p>
 *     For now, just display. This could get more official,
 *     for when we want to change sample rates.
 * </p>
 */

public class SampleRateConversion {

    static double getCutoff(double wn, double sampleRate) {
        return wn * (sampleRate / 2);
    }

    static List<Double> getCutoffs(double fs, List<Double> WN) {
        List<Double> rs = new ArrayList<>();
        WN.forEach( wn -> rs.add( wn * fs/2) );
        return rs;
    }

    public static void main(String[] args) {
        List<Double> wn = Stream.of(10,15,20)
            .map(n -> (1000.0 * n)/(44100/2) )
            .collect(toList());

        //  MatLab labels:
        //  fs = sampleRate; fc = cutoff freq
        List<Double> sampleRates = new ArrayList<>();
        Arrays.asList(44100, 48000, 96000)
            .forEach(n -> sampleRates.add(n.doubleValue()));

        sampleRates.forEach( fs ->
            out.println("fs="+fs+" fc="+
                getCutoffs(fs, wn)
                    .stream()
                    .map( n -> String.format("%,6.3f ", n))
                    .collect(toList())
            )
        );

    }
}
