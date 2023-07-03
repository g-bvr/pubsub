package org.jkube.gitbeaver;

import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.pubsub.commands.PubSubListenCommand;
import org.jkube.gitbeaver.pubsub.commands.PubSubSetProjectCommand;
import org.jkube.gitbeaver.pubsub.commands.PubSubStopCommand;

public class PubSubPlugin extends SimplePlugin {

    public PubSubPlugin() {
        super("""
                        listens to pubsub messages and triggers corresponding script executions
                        """,
                PubSubSetProjectCommand.class,
                PubSubListenCommand.class,
                PubSubStopCommand.class
        );
    }

}
