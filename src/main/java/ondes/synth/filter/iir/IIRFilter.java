package ondes.synth.filter.iir;

import ondes.synth.filter.Filter;
import ondes.synth.wire.WiredIntSupplier;

import java.util.Arrays;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class IIRFilter extends Filter {

    float[] a,b;

    float [] x,y;
    // pointers into circular buffers x,y
    int x0 = 0, y0 = 0;

    float levelScale = 1;

    void setKey(String key) {
        IIRSpec spec = IIRSpecLib.get(key);
        if (spec == null) {
            err.println("Unrecognized filter key: "+key);
            setNoFilter();
            return;
        }
        a=spec.a;
        b=spec.b;
        x=new float[a.length];
        y=new float[b.length];

        out.println(" IIR : a="+Arrays.toString(a));
        out.println("       b="+Arrays.toString(b));

    }

    void setNoFilter() {
        a = new float[] { 1 };
        b = new float[] { 1 };
        x = new float[1];
        y = new float[1];
    }

    @Override
    public int currentValue() {
        int X_n = 0;
        for (WiredIntSupplier in : inputs) X_n += in.getAsInt();

        //out.println("X_n: "+X_n);
        float Sigma = 0;

        x[x0] = X_n;
        for (int i=0; i<b.length; ++i) {
            Sigma += b[i] * x[(x.length + x0 - i) % x.length];
        }
        x0 = (x0 + 1) % x.length;

        y[y0] = Sigma;
        for (int i=1; i<a.length; ++i) {
            Sigma -= a[i] * y[(y.length + y0 - i) % y.length];
        }
        y0 = (y0 + 1) % y.length;

        //out.println("Sigma * levelScale="+Sigma * levelScale);

        return (int)( Sigma * levelScale );
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        super.configure(config, components);
        String key = config.get("key").toString();
        if (key == null) {
            err.println("'key' must be defined for IIR filter.");
            setNoFilter();
            return;
        }
        setKey(key);

        Float fltInp;
        String levelScaleErr =
            "'level-scale' must be between 0 and 11. (floating) " +
                "Yes, it goes to 11! \n" +
                "(but you probably want it much lower than that)";
        fltInp = getFloat(config.get("level-scale"), levelScaleErr);
        if (fltInp != null) {
//            if (fltInp < 0 || fltInp >11) err.println(levelScaleErr);
            //else
                levelScale = fltInp;
        }
    }


    @Override
    public void pause() {
        Arrays.fill(x,0);
        Arrays.fill(y,0);
    }

    @Override
    public void resume() {
        //  see 'pause'
    }

}
