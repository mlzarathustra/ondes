package ondes.synth.envelope;

import java.util.HashMap;

import static ondes.mlz.Util.*;

/**
 * Supplies an envelope from the key given
 *
 * TODO - allow for a "release" point to jump to on Note-OFF
 *          (simply an index into the set of points)
 *
 * @see Env
 * </p>
 */
public class EnvMaker {

    static String DEFAULT_ENV = "organ";

    private static HashMap<String, Env>envs = new HashMap<>();

    static {
        try {
            //  pairs are { rate, level } (@see Step)
            //  levels are from 0 to 100
            //
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

    public static Env getEnv(String shape) {
        Env env=envs.get(keyStrip(shape));
        if (env != null) return env;
        return envs.get(DEFAULT_ENV);
    }


}
