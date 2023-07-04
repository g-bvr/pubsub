package org.jkube.gitbeaver.pubsub.commands;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.pubsub.PubSubAsync;

import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class PubSubStopCommand extends SimpleCommand {

    public PubSubStopCommand() {
        super("PUBSUB STOP", "Shutdown pubsub (no more messages will be received server shuts down after all running executions have terminated)");
    }

    @Override
    public void execute(WorkSpace workSpace, Map<String, String> arguments) {
        log("Stop waiting for pubsub messages");
        PubSubAsync.shutdown();
    }
}
