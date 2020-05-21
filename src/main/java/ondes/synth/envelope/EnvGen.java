package ondes.synth.envelope;

import ondes.synth.Component;

import java.util.HashMap;
import static ondes.mlz.Util.*;

/**
 * <p>
 * A facade facing the ToneGen class.
 * </p><p>
 * It addresses messages regarding setting the envelope type,
 * as well as note-on's and off's. This class contains an
 * Env object, that deals with the gritty details of the
 * state machine.
 *
 * @see Env
 * </p>
 */
public class EnvGen extends Component {

    private static HashMap<String, Env>envs = new HashMap<>();

    static {
        try {
            envs.put("organ", new Env(8,100, 8,0));
            //envs.put("clavier", new Env(0, 100, 2,75, 25,40, 40,30, 100,0, 10,0));
            envs.put("clavier", new Env(0, 100, 2,75, 25,50, 50,0, 10,0));
            envs.put("fade", new Env(15,75, 25,100, 35,0));
            //
            envs.put("test", new Env(0,100, 2,50, 3,0));
            envs.put("test1", new Env(2,100, 3,50, 3,75, 3,0));
        }
        catch (InstantiationException ex) {
            System.out.println("Creating envelopes: "+ex);
            System.exit(-1);
        }
    }

    private Env env;
    private String envKey;

    public String getEnvKey() { return envKey; }

    public EnvGen() { this(null); }

    public EnvGen(String envKey) {
        env=envs.get(keyStrip(envKey));
        if (env == null) {
            envKey = "fade";
            env=envs.get(envKey);
        }
        this.envKey = envKey;
    }


    public void noteON() { env.noteON(); }
    public void noteOFF() { env.noteOFF(); }
    public double nextVal() { return env.nextVal(); }
    boolean isComplete() { return env.isComplete(); }


    public void update() {

        // TODO - implement

    }

}
