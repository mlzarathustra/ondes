package ondes.audio;

// import mlz.synth.ToneGen;

import javax.sound.sampled.*;

import java.util.*;

import static java.lang.System.out;
import static java.util.stream.Collectors.*;

public class Speak {
/*
    static void slide(ToneGen gen) throws Exception {
        gen.setWaveGen("square");

        for (int i=660; i>0; i--) {
            gen.noteON(220+i);
            Thread.sleep(10);
        }
        gen.setWaveGen("pwm");
        gen.setEnvGen("clavier");
        gen.noteON(220);
        Thread.sleep(3000);
        gen.noteOFF();

    }
    static void bells(ToneGen gen) throws Exception {
        gen.setWaveGen("bell");
        gen.setEnvGen("clavier");
        gen.noteON(440);
        Thread.sleep(4000);
        gen.noteOFF();
    }
    static void sample(ToneGen gen) throws Exception {
        gen.setWaveGen("sample");
        gen.setEnvGen("fade");
        gen.noteON(440);
        Thread.sleep(4000);
        gen.noteOFF();
    }

    public static void main(String[] args) {
        Mixer.Info[] info= AudioSystem.getMixerInfo();
        List<Mixer.Info> list = Arrays.stream(info)
            .filter(i -> {
                String id=i.toString().toLowerCase();
                if (!id.contains("realtek") || !id.contains("speaker"))
                    return false;
                Mixer m=AudioSystem.getMixer(i);
                return m.getSourceLineInfo().length > 0;
            })
            .collect(toList());

        out.println(list);
        if (list.isEmpty()) return;

        Mixer m = AudioSystem.getMixer(list.get(0));
        out.println(m);

        Line.Info[] lineInfo = m.getSourceLineInfo();
        out.println(Arrays.toString(lineInfo));

        try {
            SourceDataLine line = (SourceDataLine)m.getLine(lineInfo[0]);
            out.println(line);
            // format: PCM_SIGNED 44100.0 Hz, 16 bit, stereo,
            // 4 bytes/frame, little-endian

            ToneGen gen = new ToneGen(line);
            // slide(gen);
            //bells(gen);
            sample(gen);


        }
        catch (Exception ex) {
            out.println("Exception Caught: "+ex);
            ex.printStackTrace();
        }
    }

 */
}
