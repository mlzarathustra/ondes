package ondes.synth.envelope;


import ondes.mlz.MaxTrackerPQ;
import ondes.synth.component.MonoComponent;
import ondes.synth.wire.WiredIntSupplier;

import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class Limiter extends MonoComponent {

    long maxIn, maxOut, threshold;
    double slope;
    int delayMs;

    // it's expensive, so we don't start it until we need it.
    boolean bypass=true;

    MaxTrackerPQ maxTracker;

    long hexOrInt(String s) {
        if (s.toLowerCase().startsWith("0x")) {
            return Integer.parseInt(s.substring(2),16);
        }
        return Integer.parseInt(s);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map config, Map components) {
        try {
            maxIn = hexOrInt( config.get("max-in").toString() );
            maxOut = hexOrInt( config.get("max-out").toString() );
            threshold = hexOrInt( config.get("threshold").toString() );
            delayMs = (int)hexOrInt( config.get("delay-ms").toString() );
        }
        catch (Exception ex) {
            err.println("Could not configure limiter "+config+
                "\n"+ex);
        }
        if (maxIn <= threshold) {
            err.println("Limiter "+config.get("name")+" is configured " +
                "such that the maximum input is lower than the threshold, " +
                "so it won't be doing anything.");
            return; // leave maxTracker null.
            // n.b. maxIn==threshold will cause divide by 0 below
        }

        slope = ((double)(maxOut - threshold)) /
            ((double)(maxIn - threshold));

        maxTracker = new MaxTrackerPQ((int)(
            ( ((float) delayMs)/1000.0 ) * synth.getSampleRate()
        ));
    }

    @Override
    public void pause() {
        // no phase clocks here, so no need to pause() or resume()
    }

    @Override
    public void resume() {
        // no phase clocks here, so no need to pause() or resume()
    }

    int ct=0;
    boolean first=true;

    @Override
    public int currentValue() {
        int sum=0;
        for (WiredIntSupplier input : inputs) {
            sum += input.getAsInt();
        }
        if ((bypass && sum < threshold) ||
            maxTracker == null) return sum;

        // it may help to limit the delta of currentMax
        // on the other hand, funky sound when you're overloading
        // is probably tough to avoid.
        //
        maxTracker.accept(sum);
        double max = maxTracker.getCurrentMax();
        if (max < threshold) {
            bypass = true;
            maxTracker.reset();
            return sum;
        }

        bypass = false;
        if (first) {
            out.println("<> = OVERLOAD!");
            first = false;
        }
        if (ct++%10_000 == 0) out.print("<>");

        double adjusted = ((slope * (max-threshold)) + threshold);
        return (int)((adjusted/max) * sum);
    }

}
