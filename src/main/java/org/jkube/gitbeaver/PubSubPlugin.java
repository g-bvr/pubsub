package org.jkube.gitbeaver;

import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.pubsub.commands.*;

public class PubSubPlugin extends SimplePlugin {

    public PubSubPlugin() {
        super("""
                        listens to pubsub messages and triggers corresponding script executions
                        """,
                PubSubPullCommand.class,
                PubSubPushCommand.class,
                PubSubSetProjectCommand.class,
                PubSubListenCommand.class,
                PubSubStopCommand.class
        );
    }

}
