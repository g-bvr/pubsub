package org.jkube.gitbeaver.pubsub.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.pubsub.PubSubPublish;
import org.jkube.gitbeaver.util.FileUtil;

import java.util.Map;

public class PubSubPushCommand extends AbstractCommand {

    private static final String TOPIC = "topic";
    private static final String PROJECT = "project";
    private static final String MESSAGE = "message";

    public PubSubPushCommand() {
        super("Pull messages from pubsub and store into files");
        commandline("PUBSUB PUSH "+MESSAGE+" TO "+TOPIC+" IN PROJECT "+PROJECT);
        argument(TOPIC, "The name of the topic to which messages will be pushed");
        argument(PROJECT, "The id of the project in which the subscription is located");
        argument(MESSAGE, "The path to a file (relative to current workspace) with the message to be pushed");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String topic = arguments.get(TOPIC);
        String project = arguments.get(PROJECT);
        String message = String.join("\n",FileUtil.readLines(workSpace.getAbsolutePath(arguments.get(MESSAGE))));
        PubSubPublish.publishMessage(project, topic, message);
    }

}
