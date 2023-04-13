package org.jkube.gitbeaver.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.jkube.gitbeaver.ExecutionQueue;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.jkube.logging.Log.onException;

public class PubSub {

    public static final SimpleDateFormat RUN_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final static PubSub SINGLETON = new PubSub();

    private final ExecutionQueue queue = new ExecutionQueue();
    private final List<Subscriber> subscriberList = new ArrayList<>();
    private String project;

    public static void init(String project) {
        SINGLETON.project = project;
    }

    public static void startReceiving(String subscription, WorkSpace scriptWorkspace, String script, List<String> arguments, Map<String, String> variables) {
        SINGLETON.createReceiver(subscription, scriptWorkspace, script, arguments, variables);
    }

    public void createReceiver(String subscription, WorkSpace scriptWorkspace, String script, List<String> arguments, Map<String, String> variables) {
        Expect.notNull(project).elseFail("PUBSUB SET PROJECT must be called before calling PUBSUB LISTEN");
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(project, subscription);

        // clone the variables twice, first to decouple from input, second to decouple separate runs
        Map<String, String> clonedVariables = clone(variables);

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver = (message, consumer) -> handleMessage(message, consumer, scriptWorkspace, script, subscription, arguments, clonedVariables);
        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        // Start the subscriber.
        subscriber.startAsync().awaitRunning();
        System.out.printf("Listening for messages on %s:\n", subscriptionName);
        // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
        //subscriber.awaitTerminated();
        //System.out.printf("Done listening");
        subscriberList.add(subscriber);
    }

    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer, WorkSpace scriptWorkspace, String script, String subscription, List<String> arguments, Map<String, String> variables) {
        onException(() -> tryHandleMessage(message, consumer, scriptWorkspace, script, subscription, arguments, variables)).fail("Failure in handling message "+message.getData().toStringUtf8());
    }

    private void tryHandleMessage(PubsubMessage message, AckReplyConsumer consumer, WorkSpace scriptWorkspace, String script, String subscription, List<String> arguments, Map<String, String> variables) {
        // Handle incoming message, then ack the received message.
        String messageText = message.getData().toStringUtf8();
        Log.log("Message Id: " + message.getMessageId());
        Log.log("Message Data: " + message.getData().toStringUtf8());
        // second cloning of variables here
        String result = invokeScript(scriptWorkspace, script, subscription, messageText.replaceAll(" ", "-"), cloneAndCombine(variables, getArguments(messageText, arguments)));
        Log.log("Script invocation returned: " + result);
        consumer.ack();
    }

    private Map<String, String> getArguments(String message, List<String> arguments) {
        String[] values = message.split(" ");
        Expect.size(arguments, values.length).elseFail("Illegal number of arguments in message: "+message+" expected "+arguments);
        Map<String,String> res = new LinkedHashMap<>();
        int i = 0;
        for (String arg : arguments) {
            res.put(arg, values[i++]);
        }
        return res;
    }

    public static void shutdown() {
        SINGLETON.subscriberList.forEach(PubSub::stopReceiving);
    }

    private static void stopReceiving(Subscriber s) {
        Log.log("Stopping subscriber "+s.getSubscriptionNameString());
        s.stopAsync();
    }

    private String invokeScript(WorkSpace workspace, String script, String subscription, String messageText, Map<String, String> variables) {
        String callId = createCallId(subscription, messageText);
        String runId = createRunId(callId);
        GitBeaver.applicationLogHandler().createRun(runId);
        Log.log("Triggering run "+runId+" of script "+script);
        variables.put(GitBeaver.RUN_ID_VARIABLE, runId);
        return GitBeaver.scriptExecutor().execute(script, null, variables, workspace);
    }

    private static Map<String, String> clone(Map<String, String> variables) {
        return new LinkedHashMap<>(variables);
    }

    private static Map<String, String> cloneAndCombine(Map<String, String> variables, Map<String, String> urlparams) {
        Map<String, String> result = clone(variables);
        result.putAll(urlparams);
        return result;
    }

    private static String createRunId(String callId) {
        return callId+"-"+RUN_TIME_FORMAT.format(new Date());
    }

    private static String createCallId(String subscription, String messageText) {
        return subscription+"-"+messageText.replaceAll(" ", "-");
    }

}
