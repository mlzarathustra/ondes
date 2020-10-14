package ondes.tools;

import static java.lang.System.out;

class WaveController {
    Waves waves; // contains all the waves
    boolean DB=false;
    int id; // index into wave array

    String waveName;
    int harmonic;
    float level;

    SynthConnection synthConn;

    /**
     * Don't call directly. Use Waves.getWaveController()
     */
    WaveController(
        int id,
        Waves w,
        String waveName,
        int harmonic,
        float level,
        SynthConnection sc) {
            this.id = id;
            synthConn = sc; // set... fns below need this
            waves = w;
            synthConn.addWave(this); // this is uninitialized
            setWaveName(waveName);
            setHarmonic(harmonic);
            setLevel(level);
    }

    void setWaveName(String wn) {
        waveName = wn;
        synthConn.setWaveName(id, wn);
    }
    void setHarmonic(int h) {
        harmonic = h;
        synthConn.setHarmonic(id, h);
    }
    void setLevel(float l) {
        level = l;
        synthConn.setLevel(id, l);
    }

    public String toString() {
        return "WaveController { waveName: "+waveName+"; harmonic: "+harmonic+
            "; level: "+level+" } ";
    }


}
