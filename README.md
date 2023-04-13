# Repository g-bvr/pub-sub

This repository defines a plugin that can be used to enhance the built-in set of commands
to receive messages from pubsub subscriptions and trigger serialized script execution.

## Activation

This plugin can be integrated into the [core docker image](https://hub.docker.com/r/gitbeaver/core/tags)
by executing the following beaver script:

```
GIT CLONE https://github.com/g-bvr pub-sub main
PLUGIN COMPILE sub-sub/src/main/java
PLUGIN ENABLE org.jkube.gitbeaver.PubSubPlugin
```

A more convenient way to build a gitbeaver release with multiple
plugins (based on a tabular selection)
is provided by E. Breuninger GmbH & Co. in the public repository
[e-breuninger/git-beaver](https://github.com/e-breuninger/git-beaver).

## Documentation of defined commands

A list of all commands defined by this plugin can be found in this [automatically generated documentation](https://htmlpreview.github.io/?https://raw.githubusercontent.com/g-bvr/pub-sub/main/doc/PubSubPlugin.html).

## Operation hints

The PubSubListenCommand may be executed multiple times, in order to receive messages from different subscriptions.
Before any PubSubListenCommand can be executed, the project in which subscriptions are located must be specified using the PubSubSetProjectCommand.

When one or multiple pubsub subscriptions are activated (using the PubSubListenCommand), the jvm remains running, even after the main thread has exited.
By calling the PubSubStopCommand all active receivers are stopped and the JVM will be terminated (unless other plugins prevent that).

## Hints for operating as a cloud run service

An example setup of a gitbeaver cloud run service is illustrated by [this terraform file](https://raw.githubusercontent.com/e-breuninger/git-beaver-gcp/main/terraform/main.tf) in the public repository
[e-breuninger/git-beaver-gcp](https://github.com/e-breuninger/git-beaver-gcp) (kindly provided by E. Breuninger GmbH & Co.).
