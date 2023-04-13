package org.jkube.gitbeaver.pubsub.commands;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.pubsub.PubSub;

import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class PubSubSetProjectCommand extends SimpleCommand {

    private static final String PROJECT = "project";

    public PubSubSetProjectCommand() {
        super("PUBSUB SET PROJECT ", "Set the project from which pubsub messages will be received");
        argument(PROJECT, "the project in which pubsub subscriptions are located");
    }

    @Override
    public void execute(WorkSpace workSpace, Map<String, String> arguments) {
        String project = arguments.get(PROJECT);
        PubSub.init(project);
        log("Receiving pubsub messages from project {}", project);
    }
}
