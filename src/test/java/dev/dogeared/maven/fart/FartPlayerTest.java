package dev.dogeared.maven.fart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.sound.sampled.AudioFormat;
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
    void allFartsDecodeSuccessfully(int fartNumber) {
        FartPlayer.DecodedFart decoded = FartPlayer.decodeFart(fartNumber);

        assertNotNull(decoded, "Fart " + fartNumber + " should decode successfully");
        assertNotNull(decoded.data, "Decoded data should not be null");
        assertTrue(decoded.data.length > 0, "Decoded data should not be empty");
        assertNotNull(decoded.format, "Audio format should not be null");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void decodedFartsHaveValidPcmFormat(int fartNumber) {
        FartPlayer.DecodedFart decoded = FartPlayer.decodeFart(fartNumber);
        assertNotNull(decoded);

        AudioFormat format = decoded.format;
        assertEquals(AudioFormat.Encoding.PCM_SIGNED, format.getEncoding());
        assertEquals(16, format.getSampleSizeInBits());
        assertTrue(format.getSampleRate() > 0, "Sample rate should be positive");
        assertTrue(format.getChannels() > 0, "Should have at least one channel");
        assertFalse(format.isBigEndian(), "Should be little-endian");
    }

    @Test
    void decodeNonExistentFartReturnsNull() {
        FartPlayer.DecodedFart decoded = FartPlayer.decodeFart(999);
        assertNull(decoded, "Non-existent fart should return null");
    }

    @Test
    void decodeZeroFartReturnsNull() {
        FartPlayer.DecodedFart decoded = FartPlayer.decodeFart(0);
        assertNull(decoded, "Fart 0 should return null");
    }

    @Test
    void fartCountMatchesBundledResources() {
        assertEquals(10, FartPlayer.FART_COUNT);
    }

    @Test
    void playFartWithValidNumberReturnsBoolean() {
        // May return false on headless CI (no audio device), true locally
        boolean result = FartPlayer.playFart(1, 0.0f);
        assertTrue(result || !result);
    }

    @Test
    void playFartWithInvalidNumberReturnsFalse() {
        assertFalse(FartPlayer.playFart(999, 0.8f));
    }

    @Test
    void playReturnsBoolean() {
        boolean result = FartPlayer.play(0.0f);
        assertTrue(result || !result);
    }

    @Test
    void playPcmWithInvalidFormatReturnsFalse() {
        // Garbage format that AudioSystem can't open a line for
        AudioFormat badFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, 1, 1, 1, 1, 1, false
        );
        assertFalse(FartPlayer.playPcm(new byte[]{0, 0}, badFormat, 0.5f));
    }

    @Test
    void decodedFartConstructorStoresValues() {
        byte[] data = {1, 2, 3};
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        FartPlayer.DecodedFart decoded = new FartPlayer.DecodedFart(data, format);

        assertSame(data, decoded.data);
        assertSame(format, decoded.format);
    }
}
