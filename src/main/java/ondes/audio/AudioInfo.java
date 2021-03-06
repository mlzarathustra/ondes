/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package ondes.audio;


import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import java.util.*;

import static java.lang.System.out;
import static ondes.mlz.Util.getResourceAsString;

public class AudioInfo {

    static void all() {
        Mixer.Info[] info= AudioSystem.getMixerInfo();
        Arrays.stream(info)
            .sorted(Comparator.comparing(Object::toString))
            .forEach( mi -> {
                out.println("LABEL: "+mi); // the first line is the label

                Mixer m = AudioSystem.getMixer(mi);

                out.print("Source lines:");
                Line.Info[] srcLineInfo = m.getSourceLineInfo();
                Arrays.stream(srcLineInfo).forEach(s -> out.println("  "+s));
                out.println("Target lines:");
                Line.Info[] trgLineInfo = m.getTargetLineInfo();
                Arrays.stream(trgLineInfo).forEach(s -> out.println("  "+s));

                out.println("  --");
            });

        out.println("\nShowing all mixers. Say 'in' or 'out' \n" +
            " to see Source or Target mixers respectively");

    }

    /**
     * A "Target Line" is for input.
     *
     * To the mixer, this is a line it transmits to, hence "target."
     * To the app, this is a source of input.
     */
    static void in() {
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        Arrays.stream(info)
            .sorted(Comparator.comparing(Object::toString))
            .forEach(mi -> {
                Mixer m = AudioSystem.getMixer(mi);
                Line.Info[] srcLineInfo = m.getSourceLineInfo();
                Line.Info[] trgLineInfo = m.getTargetLineInfo();
                if (trgLineInfo.length == 0) return;

                out.println("LABEL: "+mi); // the first line is the label

                out.println("Source lines:");
                Arrays.stream(srcLineInfo).forEach(s -> out.println("  " + s));
                out.println("Target lines:");
                Arrays.stream(trgLineInfo).forEach(s -> out.println("  " + s));

                out.println("  --");
            });
    }

    /**
     * It's the "Source Line" that's for outputting to.
     *
     * According to the Javadoc: "It acts as a source to the mixer."
     * In any event it's confusing.
     */
    public static void out() {
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        Arrays.stream(info)
            .sorted(Comparator.comparing(Object::toString))
            .forEach(mi -> {
                Mixer m = AudioSystem.getMixer(mi);
                Line.Info[] srcLineInfo = m.getSourceLineInfo();
                Line.Info[] trgLineInfo = m.getTargetLineInfo();
                if (srcLineInfo.length == 0) return;

                out.println("LABEL: "+mi); // the first line is the label

                out.println("Source lines:");
                Arrays.stream(srcLineInfo).forEach(s -> out.println("  " + s));
                out.println("Target lines:");
                Arrays.stream(trgLineInfo).forEach(s -> out.println("  " + s));

                out.println("  --");
            });
    }

    static void usage() {
        out.println(getResourceAsString("usage/AudioInfo.txt"));
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            all();
        }
        else {
            if (args[0].equals("in")) in();
            else if (args[0].equals("out")) out();
            else all();
        }
        usage(); // at the bottom so it doesn't scroll off

    }

}
