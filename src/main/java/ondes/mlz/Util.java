package ondes.mlz;

/**
 * a place to keep static utility functions
 *
 */
public class Util {
    public static String keyStrip(String s) {
        return s.toLowerCase().replaceAll("[^a-z]","");
    }
}
