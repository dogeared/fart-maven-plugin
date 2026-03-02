package dev.dogeared.maven.fart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class FartMojoTest {

    private FartMojo mojo;
    private Consumer<Float> originalPlayer;

    @BeforeEach
    void setUp() {
        mojo = new FartMojo();
        originalPlayer = FartMojo.playerFunction;
        FartMojo.RUNNING.set(false);
        FartMojo.fartThread = null;
    }

    @AfterEach
    void tearDown() {
        FartMojo.RUNNING.set(false);
        if (FartMojo.fartThread != null) {
            FartMojo.fartThread.interrupt();
            FartMojo.fartThread = null;
        }
        FartMojo.playerFunction = originalPlayer;
    }

    @Test
    void skipPreventsPlayback() throws Exception {
        setField("skip", true);

        List<Float> played = Collections.synchronizedList(new ArrayList<>());
        FartMojo.playerFunction = played::add;

        mojo.execute();

        Thread.sleep(100);
        assertTrue(played.isEmpty(), "No farts should play when skip=true");
        assertFalse(FartMojo.RUNNING.get(), "RUNNING should remain false when skipped");
        assertNull(FartMojo.fartThread, "No thread should be created when skipped");
    }

    @Test
    void alreadyRunningPreventsSecondThread() throws Exception {
        FartMojo.RUNNING.set(true);

        List<Float> played = Collections.synchronizedList(new ArrayList<>());
        FartMojo.playerFunction = played::add;

        mojo.execute();

        Thread.sleep(100);
        assertTrue(played.isEmpty(), "No farts should play when already running");
        assertNull(FartMojo.fartThread, "No new thread should be created");
    }

    @Test
    void executeStartsDaemonThread() throws Exception {
        setField("skip", false);
        setField("minInterval", 60000);
        setField("maxInterval", 60001);

        FartMojo.playerFunction = v -> {};

        mojo.execute();

        assertNotNull(FartMojo.fartThread, "Fart thread should be created");
        assertTrue(FartMojo.fartThread.isDaemon(), "Fart thread should be a daemon");
        assertTrue(FartMojo.fartThread.isAlive(), "Fart thread should be alive");
        assertTrue(FartMojo.RUNNING.get(), "RUNNING should be true");
        assertEquals("fart-maven-plugin", FartMojo.fartThread.getName());
    }

    @Test
    void executePlaysFartImmediately() throws Exception {
        setField("skip", false);
        setField("minInterval", 60000);
        setField("maxInterval", 60001);

        List<Float> played = Collections.synchronizedList(new ArrayList<>());
        FartMojo.playerFunction = played::add;

        mojo.execute();

        // Wait for the immediate fart to play on the background thread
        Thread.sleep(500);

        assertFalse(played.isEmpty(), "At least one fart should play immediately");
    }

    @Test
    void volumeIsPassedToPlayer() throws Exception {
        setField("skip", false);
        setField("volume", 0.42f);
        setField("minInterval", 60000);
        setField("maxInterval", 60001);

        List<Float> volumes = Collections.synchronizedList(new ArrayList<>());
        FartMojo.playerFunction = volumes::add;

        mojo.execute();

        Thread.sleep(500);

        assertFalse(volumes.isEmpty());
        assertEquals(0.42f, volumes.get(0), 0.001f, "Volume should be passed through");
    }

    @Test
    void threadPlaysFartsAtIntervals() throws Exception {
        setField("skip", false);
        setField("minInterval", 100);
        setField("maxInterval", 150);

        List<Float> played = Collections.synchronizedList(new ArrayList<>());
        FartMojo.playerFunction = played::add;

        mojo.execute();

        // Wait enough for initial fart + a couple interval farts
        Thread.sleep(1000);

        assertTrue(played.size() >= 2, "Should have played multiple farts, got: " + played.size());
    }

    @Test
    void threadStopsWhenRunningSetToFalse() throws Exception {
        setField("skip", false);
        setField("minInterval", 100);
        setField("maxInterval", 150);

        FartMojo.playerFunction = v -> {};

        mojo.execute();

        Thread thread = FartMojo.fartThread;
        assertNotNull(thread);
        assertTrue(thread.isAlive());

        FartMojo.RUNNING.set(false);
        thread.interrupt();
        thread.join(2000);

        assertFalse(thread.isAlive(), "Thread should have stopped");
    }

    @Test
    void mojoExtendsAbstractMojo() {
        assertInstanceOf(org.apache.maven.plugin.AbstractMojo.class, mojo);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = FartMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(mojo, value);
    }

}
