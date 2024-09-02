package org.jkube.gitbeaver.pubsub;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.jkube.gitbeaver.logging.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PubSubPublish {

    public static void publishMessage(String projectId, String topicId, String message) {
        Log.onException(() -> tryPublishMessage(projectId, topicId, message)).fail("Could not publish message");
    }

    public static void tryPublishMessage(String projectId, String topicId, String message) throws IOException, InterruptedException, ExecutionException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;
        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            String result = publisher.publish(pubsubMessage).get();
            Log.log("Message publish result: "+result);
        }  finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
