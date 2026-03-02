package dev.dogeared.maven.fart;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stops fart playback. Bind to a late phase (e.g., verify) or invoke directly.
 */
@Mojo(name = "stop-fart", threadSafe = true)
public class StopFartMojo extends AbstractMojo {

    @Override
    public void execute() {
        FartMojo.RUNNING.set(false);
        Thread thread = FartMojo.fartThread;
        if (thread != null) {
            thread.interrupt();
            FartMojo.fartThread = null;
            getLog().info("Farts silenced.");
        }
    }
}
