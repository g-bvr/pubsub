package org.jkube.gitbeaver.pubsub.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.pubsub.PubSub;

import java.util.List;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;
import static org.jkube.logging.Log.log;

public class PubSubListenCommand extends AbstractCommand {

    private static final String SUBSCRIPTION = "subscription";
    private static final String SCRIPT = "script";
    private static final String FOLDER = "folder";

    public PubSubListenCommand() {
        super("Set up a message receiver fot specified pubsub subscription that triggers execution of a script");
        commandlineVariant("SUBSCRIBE "+SUBSCRIPTION+" "+SCRIPT+" "+REST, "triggers script in current workspace");
        commandlineVariant("SUBSCRIBE "+SUBSCRIPTION+" IN "+FOLDER+" "+SCRIPT+" "+REST, "triggers script in sub-workspace specified by given folder");
        argument(SUBSCRIPTION, "The name of the subscription from which messages will be received");
        argument(SCRIPT, "The path to the script (relative to current workspace, not the execution workspace) that gets executed when trigger receives a GET request");
        argument(FOLDER, "The path to a folder (relative to current workspace) that serves as execution workspace (the executed script can be located outside this folder)");
        argument(REST, "Names of arguments expected in messages");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String subscription = arguments.get(SUBSCRIPTION);
        String script = arguments.get(SCRIPT);
        List<String> argNames = List.of(arguments.get(REST).split(" "));
        WorkSpace scriptWorkspace = arguments.containsKey(FOLDER) ? workSpace.getSubWorkspace(arguments.get(FOLDER)) : workSpace;
        PubSub.startReceiving(subscription, scriptWorkspace, script, argNames, variables);
        log("Receiving pubsub messages from subscription "+subscription+" triggering script "+script+" in workspace "+workSpace.getWorkdir()+" with arguments "+argNames);
    }
}
