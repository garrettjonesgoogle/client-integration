/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.combiner;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.TopicName;

public class PubsubHello {
  public static void doHello(String projectId) throws Exception {
    System.out.println("=== Pub/Sub: creating topic ===");

    String topicId = "my-topic-" + System.currentTimeMillis();

    // Create a new topic
    TopicName topic = TopicName.of(projectId, topicId);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topic);
    }

    System.out.printf("Topic %s:%s created.\n", topic.getProject(), topic.getTopic());

  }
}

