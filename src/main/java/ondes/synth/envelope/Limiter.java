package ondes.synth.envelope;


import ondes.mlz.MaxTracker;
import ondes.synth.component.MonoComponent;

import java.util.Map;

import static java.lang.System.err;

public class Limiter extends MonoComponent {

    long maxIn, maxOut, threshold;
    int delayMs;

    MaxTracker maxTracker;


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
        maxTracker = new MaxTracker((int)(
            ( ((float) delayMs)/1000.0 ) * synth.getSampleRate()
        ));

    }

    @Override
    public void release() {

    }

    @Override
    public int currentValue() {

        // TODO - make sure our latch will get reset

        return 0;
    }

}
