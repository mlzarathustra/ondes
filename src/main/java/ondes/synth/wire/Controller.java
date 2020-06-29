package ondes.synth.wire;

import ondes.synth.component.MonoComponent;

/**
 * <p>
 *     Listens to a particular controller, and
 *     sends the scaled value as output.
 * </p>
 * <p>
 *     Has no inputs (aside from the controller)
 * </p>
 *
 */
public class Controller extends MonoComponent {



    @Override
    public int currentValue() {
        return 0;
    }




    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
