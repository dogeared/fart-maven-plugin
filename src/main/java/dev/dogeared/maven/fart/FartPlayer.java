package dev.dogeared.maven.fart;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * Plays fart MP3 sounds from the bundled fartscroll.js collection.
 */
public class FartPlayer {

    private static final int FART_COUNT = 10;
    private static final Random RANDOM = new Random();

    /**
     * Play a random fart sound synchronously (blocks until playback completes).
     *
     * @param volume volume level from 0.0 to 1.0
     * @return true if playback succeeded
     */
    public static boolean play(float volume) {
        int fartNumber = 1 + RANDOM.nextInt(FART_COUNT);
        String resource = "/farts/fart" + fartNumber + ".mp3";

        try (InputStream raw = FartPlayer.class.getResourceAsStream(resource)) {
            if (raw == null) {
                return false;
            }

            BufferedInputStream buffered = new BufferedInputStream(raw);
            AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(buffered);

            // Decode MP3 to PCM
            AudioFormat baseFormat = mp3Stream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream)) {
                SourceDataLine line = AudioSystem.getSourceDataLine(decodedFormat);
                line.open(decodedFormat);

                // Set volume
                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (20.0 * Math.log10(Math.max(0.0001, volume)));
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }

                line.start();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
                line.stop();
                line.close();
            }

            mp3Stream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
