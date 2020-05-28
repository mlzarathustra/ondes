package ondes.synth.envelope;


import ondes.synth.component.MonoComponent;

import java.util.Map;

public class Limiter extends MonoComponent {

    @Override
    public void configure(Map config, Map components) {

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
