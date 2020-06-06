package ondes.synth.filter.iir;

public class IIRSpec {
    String key;
    float[] a, b;
    IIRSpec(String key, double[][]ab) {
        this.key = key;

        // cast the double array as a float array, because otherwise
        // you have to put the silly 'f' after every number.
        //
        a=new float[ab[0].length];
        b=new float[ab[1].length];
        for (int i=0; i<a.length; ++i) {
            a[i] = (float) ab[0][i];
        }
        for (int i=0; i<b.length; ++i) {
            b[i] = (float) ab[1][i];
        }
    }
}