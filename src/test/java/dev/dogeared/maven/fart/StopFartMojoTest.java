package dev.dogeared.maven.fart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopFartMojoTest {

    private StopFartMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new StopFartMojo();
        FartMojo.RUNNING.set(false);
        FartMojo.fartThread = null;
    }

    @AfterEach
    void tearDown() {
        FartMojo.RUNNING.set(false);
        FartMojo.fartThread = null;
    }

    @Test
    void stopSetsRunningToFalse() {
        FartMojo.RUNNING.set(true);

        mojo.execute();

        assertFalse(FartMojo.RUNNING.get(), "RUNNING should be false after stop");
    }

    @Test
    void stopInterruptsThread() throws Exception {
        FartMojo.RUNNING.set(true);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "test-fart-thread");
        thread.setDaemon(true);
        thread.start();
        FartMojo.fartThread = thread;

        mojo.execute();

        thread.join(2000);
        assertFalse(thread.isAlive(), "Thread should have been interrupted and stopped");
        assertNull(FartMojo.fartThread, "fartThread should be null after stop");
        assertFalse(FartMojo.RUNNING.get());
    }

    @Test
    void stopWithNoThreadDoesNotThrow() {
        FartMojo.RUNNING.set(true);
        FartMojo.fartThread = null;

        assertDoesNotThrow(() -> mojo.execute());
        assertFalse(FartMojo.RUNNING.get());
        assertNull(FartMojo.fartThread);
    }

    @Test
    void stopWhenAlreadyStopped() {
        FartMojo.RUNNING.set(false);
        FartMojo.fartThread = null;

        assertDoesNotThrow(() -> mojo.execute());
        assertFalse(FartMojo.RUNNING.get());
    }
}
