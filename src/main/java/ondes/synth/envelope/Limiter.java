package ondes.synth.envelope;


import ondes.synth.component.MonoComponent;

import java.util.Map;

import static java.lang.System.err;

public class Limiter extends MonoComponent {

    long maxIn, maxOut, threshold;


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
        }
        catch (Exception ex) {
            err.println("Could not configure limiter "+config+
                "\n"+ex);
        }
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
