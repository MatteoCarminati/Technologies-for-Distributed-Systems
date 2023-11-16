import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class C2Forw {
    private static final String defaultGroupId = "groupC";
    private static final String defaultTopic = "topicA";
    private static final String outputTopic = "topicB";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        String topic = args.length >= 2 ? args[1] : defaultTopic;

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        final Properties propsProd = new Properties();
        propsProd.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        propsProd.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProd.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProd.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, String> producer = new KafkaProducer<>(propsProd);

        consumer.subscribe(Collections.singletonList(topic));

        //we keep track with a map the counter of each key produced by the producer
        Map<Integer,Integer> keyCount = new HashMap<>();
        while (true) {

            //first we obtain the record from the Producer component
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, String> record : records) {
                
                //update the Map or otherwise add a new tuple if the key was not already saved
                int currentKey = Integer.parseInt(record.key());
                boolean containsKey = keyCount.containsKey(currentKey);
                int currentCount = 0;
                if (containsKey){
                    currentCount = keyCount.get(currentKey);
                }
                keyCount.put(currentKey, currentCount+1);

                //publish the tuple of the key with the new topicB for the listener C1
                final ProducerRecord<String, String> recordProd = new ProducerRecord<>(outputTopic, record.key(), keyCount.get(currentKey).toString());
                final Future<RecordMetadata> future = producer.send(recordProd);

                System.out.println(
                        "Topic: " + recordProd.topic() +
                                "\tKey: " + recordProd.key() +
                                "\tValue: " + recordProd.value());
            }
        }
    }
}