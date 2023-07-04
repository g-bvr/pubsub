package org.jkube.gitbeaver.pubsub.commands;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.pubsub.PubSubSync;
import org.jkube.gitbeaver.util.FileUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jkube.logging.Log.log;

public class PubSubPullCommand extends AbstractCommand {

    private static final String SUBSCRIPTION = "subscription";
    private static final String PROJECT = "project";
    private static final String FOLDER = "folder";

    public PubSubPullCommand() {
        super("Pull messages from pubsub and store into files");
        commandline("PUBSUB PULL "+SUBSCRIPTION+" IN PROJECT "+PROJECT+" INTO "+FOLDER);
        argument(SUBSCRIPTION, "The name of the subscription from which messages will be pulled");
        argument(PROJECT, "The id of the project in which the subscription is located");
        argument(FOLDER, "The path to a folder (relative to current workspace) into which messages are written as individual files");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String subscription = arguments.get(SUBSCRIPTION);
        String project = arguments.get(PROJECT);
        Path target = workSpace.getAbsolutePath(arguments.get(FOLDER));
        List<PubsubMessage> messages = PubSubSync.pullMessages(project, subscription);
        log("Pulled {} messages from subscription {} in project {}", messages.size(), subscription, project);
        store(target, messages);
    }

    private void store(Path target, List<PubsubMessage> messages) {
        int i = 0;
        for (PubsubMessage message : messages) {
            i++;
            Path subfolder = target.resolve("message-" + i);
            FileUtil.createIfNotExists(subfolder);
            storeMessage(message, subfolder);
        }
    }

    private void storeMessage(PubsubMessage message, Path folder) {
        FileUtil.store(folder.resolve("id"), message.getMessageId());
        FileUtil.store(folder.resolve("text"), message.getData().toStringUtf8());
        FileUtil.store(folder.resolve("attributes"), format(message.getAttributesMap()));
        FileUtil.store(folder.resolve("ordering-key"), message.getOrderingKey());
        FileUtil.store(folder.resolve("publish-time"), format(message.getPublishTime()));
        FileUtil.store(folder.resolve("all-fields"), format(message.getAllFields()));
        FileUtil.store(folder.resolve("unknown-fields"), format(message.getUnknownFields().asMap()));
    }

    private List<String> format(Map<?, ?> map) {
        return map.entrySet().stream().map(e -> e.getKey() + " = "+e.getValue()).collect(Collectors.toList());
    }

    private String format(Timestamp publishTime) {
        return Timestamps.toString(publishTime);
    }

    public static final void main(String... args) {
        new PubSubPullCommand().execute(null, new WorkSpace("."), Map.of(
                SUBSCRIPTION, "gitbeaver",
                PROJECT, "breuninger-core-provisioning",
                FOLDER, "pubsubtest"
        ));
    }

}
