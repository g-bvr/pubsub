package org.jkube.gitbeaver.pubsub;

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.*;
import org.jkube.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PubSubSync {

    private static final int MAX_NUM_MESSAGES = 10;
    private static final int MAX_MESSAGE_SIZE_MB = 20;

    public static final SubscriberStubSettings SUBSCRIBER_SETTINGS =
            Log.onException(() -> SubscriberStubSettings.newBuilder()
                    .setTransportChannelProvider(
                            SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                                    .setMaxInboundMessageSize(MAX_MESSAGE_SIZE_MB * 1024 * 1024) // 20MB (maximum message size).
                                    .build())
                    .build()).fail("Could not initialize subscriber settings");


    public static List<PubsubMessage> pullMessages(String projectId, String subscriptionId) {
        try (SubscriberStub subscriber = GrpcSubscriberStub.create(SUBSCRIBER_SETTINGS)) {
            String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
            PullRequest pullRequest =
                    PullRequest.newBuilder()
                            .setMaxMessages(MAX_NUM_MESSAGES)
                            .setSubscription(subscriptionName)
                            .build();
            PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
            // Stop the program if the pull response is empty to avoid acknowledging
            // an empty list of ack IDs.
            if (pullResponse.getReceivedMessagesList().isEmpty()) {
                return Collections.emptyList();
            }
            List<String> ackIds = new ArrayList<>();
            List<PubsubMessage> messages = new ArrayList<>();
            for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
                if (message.hasMessage()) {
                    messages.add(message.getMessage());
                } else {
                    Log.warn("No message contained in pubsub response: "+message);
                }
                ackIds.add(message.getAckId());
            }

            // Acknowledge received messages.
            AcknowledgeRequest acknowledgeRequest =
                    AcknowledgeRequest.newBuilder()
                            .setSubscription(subscriptionName)
                            .addAllAckIds(ackIds)
                            .build();

            // Use acknowledgeCallable().futureCall to asynchronously perform this operation.
            subscriber.acknowledgeCallable().call(acknowledgeRequest);
            return messages;
        } catch (IOException e) {
            Log.exception(e, "Exception in pubsub pull");
            return null;
        }
    }
}