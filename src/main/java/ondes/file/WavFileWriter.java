package ondes.file;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import java.io.*;

public class WavFileWriter {

    /**
     * An audio format, assuming a few things:
     * signed 16-bit little-endian, 1 channel
     */
    public static AudioFormat getAudioFormat(int sampleRate) {
        final boolean bigEndian = false;
        final boolean signed = true; // It seems to ignore this! And assume TRUE!

        final int bits = 16;
        final int channels = 1;

        return new AudioFormat(sampleRate, bits, channels, signed, bigEndian);
    }

    /**
     * Write to (WAV) file named, using specified sampleRate
     * signed 16-bit little-endian, 1 channel
     *
     */
    public static boolean writeBuffer(
        int[] samples,
        int sampleRate,
        File outFile
    )
        throws IOException {

        byte[] byteBuffer = new byte[samples.length * 2];

        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = samples[bufferIndex++];

            byteBuffer[i++] = (byte)x;
            byteBuffer[i] = (byte)(x >>> 8);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        AudioInputStream audioInputStream =
            new AudioInputStream(bais, getAudioFormat(sampleRate), samples.length);

        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outFile);
            audioInputStream.close();
            return true;
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return false;
    }

}
