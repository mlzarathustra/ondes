package ondes.tools;

import static ondes.tools.WaveEditor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.lang.Math.*;

/**
 * Connection to the the collection of waves in the synth
 *
 */
public class Waves {

    boolean DB=false;

    //  1/10th of the slider = 3 decibels
    public static final float THREE_DB = .1f;

    public static final String template =
        "\n\n"+
        "osc1:\n" +
        "  midi: note-on\n" +
        "  type: wave\n" +
        "  shape: harmonic\n" +
        "  out: main\n" +
        "  waves:\n";
    public static final String linePrefix = "    - ";

    ArrayList<WaveController> waveList = new ArrayList<>();
    SynthConnection synthConn;

    Waves(SynthConnection sc) { synthConn = sc; }

    // synchronized to avoid adding waves while getHarmonics is looping through waveList.
    //
    synchronized WaveController getWaveController(String waveName, int harmonic, float level) {
        WaveController wc =
            new WaveController(waveList.size(),
                this, waveName, harmonic, level,
                synthConn);

        waveList.add(wc);
        return wc;
    }

    float logScale(float level) {
        if (level == 0 || level == 1) return level;
        float exp = -(1 - level) / THREE_DB;
        return (float)pow(2,exp);
    }

    /**
     * Note that this array of harmonics gives the MULTIPLIER
     * NOT the DIVISOR!
     *
     * @return An array of multipliers for this wave, each
     *      corresponding to the respective harmonic.
     *      The 0 element corresponds with the fundamental,
     *      harmonics[1] is the first overtone, &c.
     */
    @SuppressWarnings("rawtypes")
    synchronized float[] getHarmonics() {
        float[] harmonics = new float[N_HARMONICS];
        for (WaveController wc : waveList) {
            if (DB) out.println(wc);
            Map osc = WaveFile.getWaveProgram(wc.waveName);
            List points = (List) ((Map) osc.get("osc1")).get("waves");
            if (points == null) continue;
            for (Object p : points) {
                try {
                    String[] point = p.toString().trim().split("\\s+");
                    int harmonic = Integer.parseInt(point[0]);
                    float divisor = Float.parseFloat(point[1]);
                    int idx = harmonic * wc.harmonic - 1;
                    if (idx >= N_HARMONICS) continue;
                    harmonics[idx] += logScale(wc.level) / divisor;
                } catch (Exception ex) {
                    err.println("Error parsing line " + p);
                }
            }
        }
        return harmonics;
    }

    String save() {
        float []harmonics = getHarmonics();
        StringBuilder sb=new StringBuilder(template);
        for (int i=0; i<harmonics.length; ++i) {
            if (harmonics[i] != 0) {
                sb.append(linePrefix);
                sb.append(i+1);
                sb.append(" ");
                sb.append(1.0f / harmonics[i]);
                sb.append("\n");
            }
        }
        if (DB) out.println(sb);
        return WaveFile.writeNextFile(sb.toString());
    }


}
