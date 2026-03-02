package dev.dogeared.maven.fart;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * Plays fart MP3 sounds from the bundled fartscroll.js collection.
 */
public class FartPlayer {

    static final int FART_COUNT = 10;
    private static final Random RANDOM = new Random();

    /**
     * Play a random fart sound synchronously (blocks until playback completes).
     *
     * @param volume volume level from 0.0 to 1.0
     * @return true if playback succeeded
     */
    public static boolean play(float volume) {
        int fartNumber = 1 + RANDOM.nextInt(FART_COUNT);
        return playFart(fartNumber, volume);
    }

    /**
     * Play a specific fart sound by number (1-10).
     */
    static boolean playFart(int fartNumber, float volume) {
        DecodedFart decoded = decodeFart(fartNumber);
        if (decoded == null) {
            return false;
        }
        return playPcm(decoded.data, decoded.format, volume);
    }

    /**
     * Load and decode a fart MP3 from classpath resources to PCM data.
     *
     * @param fartNumber fart number (1-10)
     * @return decoded PCM data and format, or null if resource not found
     */
    static DecodedFart decodeFart(int fartNumber) {
        String resource = "/farts/fart" + fartNumber + ".mp3";

        try (InputStream raw = FartPlayer.class.getResourceAsStream(resource)) {
            if (raw == null) {
                return null;
            }

            BufferedInputStream buffered = new BufferedInputStream(raw);
            AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(buffered);

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
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return new DecodedFart(out.toByteArray(), decodedFormat);
            } finally {
                mp3Stream.close();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Play raw PCM data through the system audio output.
     */
    static boolean playPcm(byte[] pcmData, AudioFormat format, float volume) {
        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);

            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(Math.max(0.0001, volume)));
                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                gainControl.setValue(dB);
            }

            line.start();
            line.write(pcmData, 0, pcmData.length);
            line.drain();
            line.stop();
            line.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Holds decoded PCM audio data and its format.
     */
    static class DecodedFart {
        final byte[] data;
        final AudioFormat format;

        DecodedFart(byte[] data, AudioFormat format) {
            this.data = data;
            this.format = format;
        }
    }
}
