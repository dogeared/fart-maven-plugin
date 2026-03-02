package dev.dogeared.maven.fart;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Plays fart sounds at random intervals while your Maven build scrolls by.
 * Inspired by fartscroll.js — "Everyone farts. And now your builds can too."
 */
@Mojo(name = "fart", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class FartMojo extends AbstractMojo {

    static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    static volatile Thread fartThread;

    // Package-private for testing — allows swapping in a mock player
    static Consumer<Float> playerFunction = FartPlayer::play;

    /**
     * Skip fart playback. Use -Dfart.skip=true to silence the build.
     */
    @Parameter(property = "fart.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Minimum interval in milliseconds between farts.
     */
    @Parameter(property = "fart.minInterval", defaultValue = "500")
    private int minInterval;

    /**
     * Maximum interval in milliseconds between farts.
     */
    @Parameter(property = "fart.maxInterval", defaultValue = "500")
    private int maxInterval;

    /**
     * Volume level from 0.0 (silent) to 1.0 (full volume).
     */
    @Parameter(property = "fart.volume", defaultValue = "0.8")
    private float volume;

    @Override
    public void execute() {
        if (skip) {
            getLog().info("Farts skipped. How boring.");
            return;
        }

        if (RUNNING.getAndSet(true)) {
            getLog().debug("Fart thread already running.");
            return;
        }

        getLog().info("\uD83D\uDCA8 fartscroll activated — your build now farts as it scrolls!");

        final float vol = volume;
        final Random random = new Random();
        final int intervalRange = Math.max(1, maxInterval - minInterval);

        Thread thread = new Thread(() -> {
            // Play one immediately so short builds still get a fart
            playerFunction.accept(vol);

            while (RUNNING.get()) {
                try {
                    int delay = minInterval + random.nextInt(intervalRange);
                    Thread.sleep(delay);

                    if (RUNNING.get()) {
                        playerFunction.accept(vol);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "fart-maven-plugin");

        thread.setDaemon(true);
        thread.start();
        fartThread = thread;
    }
}
