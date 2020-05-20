package ondes.mlz;


/**
 * <p>
 * Everything this application needs to know about a given note:
 * its name (in sharps and flats), its base frequency, and whether
 * it appears to be black or white on a piano keyboard.
 * </p>
 * <p>
 * In this context, a "Note" is different from the same pitch at
 * an octave.
 * </p>
 *
 */
class Note {
    String flatName, sharpName;
    double freq;
    boolean white;

    // generated by tones/notes/NoteObjects.groovy
    //
    static final Note[] notes={

            new Note("C","C",2.378414230005442, true),
            new Note("B","B",2.2449240966706148, true),
            new Note("B\u266D","A#",2.118926188669633, false),
            new Note("A","A",2, true),
            new Note("A\u266D","G#",1.8877486254070033, false),
            new Note("G","G",1.7817974362395104, true),
            new Note("G\u266D","F#",1.681792830507429, false),
            new Note("F","F",1.5874010520048762, true),
            new Note("E","E",1.4983070768420632, true),
            new Note("E\u266D","D#",1.4142135623730951, false),
            new Note("D","D",1.3348398542008757, true),
            new Note("D\u266D","C#",1.2599210498657627, false),
            new Note("C","C",1.189207115002721, true),
            new Note("B","B",1.1224620483353074, true),
            new Note("B\u266D","A#",1.0594630943348164, false),
            new Note("A","A",1, true)

    };

    private Note(String fn, String sn, double f, boolean w) {
        flatName=fn; sharpName=sn; freq=f; white=w; }

    public String toString() {
        return "Note {"+flatName+"; "+freq+" }";
    }

}
