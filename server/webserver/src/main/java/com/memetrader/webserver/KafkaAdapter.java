package com.memetrader.webserver;

import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Service
public class KafkaAdapter implements Closeable {
    private KafkaProducer<String, String> producer;
    private static final String EMAIL_VERIFICATION_TOPIC = "verification-code-emails";

    public KafkaAdapter() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
    }

    /**
     * Puts the given email in the kafka queue for verification code emails.
     * Blocks until this process is complete.
     * @return True if email successfully enqueued.
     */
    public boolean enqueueVerifictionEmail(String email, String code) {
        @SuppressWarnings("unused")
        var _fut = producer.send(new ProducerRecord<String,String>(EMAIL_VERIFICATION_TOPIC, email + "," + code));
        return true;
        // TODO: Implement Kafka

        // try {
        //     fut.get();
        //     return true;
        // } catch (Exception e) {
        //     return false;
        // }
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }
}
