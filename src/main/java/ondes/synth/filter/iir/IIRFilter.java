package ondes.synth.filter.iir;

import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

public class IIRFilter extends MonoComponent {

    float[] a,b;

    float [] x,y;
    // pointers into circular buffers x,y
    int x0 = 0, y0 = 0;
    float xSum, ySum;

    IIRFilter(String key) {
        IIRSpec spec = IIRSpecLib.get(key);
        a=spec.a;
        b=spec.b;
        x=new float[a.length];
        y=new float[b.length];



    }

    @Override
    public int currentValue() {
        int X_n = 0;
        for (WiredIntSupplier in : inputs) X_n += in.getAsInt();

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

        return (int)Sigma;
    }


    @Override
    public void pause() {
        // probably won't happen much
    }

    @Override
    public void resume() {
        //  see 'pause'
    }

}
