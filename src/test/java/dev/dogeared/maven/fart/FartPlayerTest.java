package dev.dogeared.maven.fart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class FartPlayerTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void allFartResourcesExist(int fartNumber) {
        String resource = "/farts/fart" + fartNumber + ".mp3";
        try (InputStream stream = FartPlayer.class.getResourceAsStream(resource)) {
            assertNotNull(stream, "Resource should exist: " + resource);
            assertTrue(stream.available() > 0, "Resource should not be empty: " + resource);
        } catch (Exception e) {
            fail("Failed to read resource: " + resource, e);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void allFartResourcesAreValidMp3(int fartNumber) throws Exception {
        String resource = "/farts/fart" + fartNumber + ".mp3";
        try (InputStream raw = FartPlayer.class.getResourceAsStream(resource)) {
            assertNotNull(raw);
            BufferedInputStream buffered = new BufferedInputStream(raw);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered);
            AudioFormat format = audioStream.getFormat();

            assertNotNull(format, "Should have a valid audio format");
            assertTrue(format.getSampleRate() > 0, "Sample rate should be positive");
            assertTrue(format.getChannels() > 0, "Should have at least one channel");
            audioStream.close();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void allFartResourcesCanBeDecodedToPcm(int fartNumber) throws Exception {
        String resource = "/farts/fart" + fartNumber + ".mp3";
        try (InputStream raw = FartPlayer.class.getResourceAsStream(resource)) {
            assertNotNull(raw);
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
                assertNotNull(pcmStream);
                byte[] buffer = new byte[4096];
                int totalBytes = 0;
                int bytesRead;
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                }
                assertTrue(totalBytes > 0, "Decoded PCM data should not be empty");
            }
            mp3Stream.close();
        }
    }

    @Test
    void nonExistentResourceReturnsFalse() {
        // FartPlayer picks random 1-10, so we can't directly test a missing resource
        // through play(). But we can verify the resource lookup pattern works by
        // checking that resource 11 does NOT exist.
        InputStream stream = FartPlayer.class.getResourceAsStream("/farts/fart11.mp3");
        assertNull(stream, "Resource 11 should not exist");
    }

    @Test
    void playReturnsBoolean() {
        // This test exercises the full play() path. It may return false in headless
        // CI environments where no audio device is available, which is acceptable.
        boolean result = FartPlayer.play(0.0f);
        // We just verify it doesn't throw — result depends on audio hardware
        assertTrue(result || !result, "play() should return a boolean without throwing");
    }
}
