package com.fulan.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * java实现Kafka消费者的示例
 * @author lisg
 *
 */
//启动kafka的命令  kafka-server-start.sh /opt/kafka/config/server.properties
//创建kafka的topic命令 kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
// 生产者：kafka-console-producer.sh --broker-list learn:9092 --topic test
// 消费者：kafka-console-consumer.sh --bootstrap-server learn:9092 --topic test --from-beginning

public class KafkaConsumerTest {
    private static final String TOPIC = "test";
    private static final int THREAD_AMOUNT = 1;

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("zookeeper.connect", "learn:2181");
        props.put("group.id", "group1");
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");;

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        //每个topic使用多少个kafkastream读取, 多个consumer
        topicCountMap.put(TOPIC, THREAD_AMOUNT);
        //可以读取多个topic
//      topicCountMap.put(TOPIC2, 1);
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        Map<String, List<KafkaStream<byte[], byte[]>>> msgStreams = consumer.createMessageStreams(topicCountMap );
        List<KafkaStream<byte[], byte[]>> msgStreamList = msgStreams.get(TOPIC);

        //使用ExecutorService来调度线程
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_AMOUNT);
        for (int i = 0; i < msgStreamList.size(); i++) {
            KafkaStream<byte[], byte[]> kafkaStream = msgStreamList.get(i);
            executor.submit(new HanldMessageThread(kafkaStream, i));
        }


        //关闭consumer
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (consumer != null) {
            consumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
    }

}

/**
 * 具体处理message的线程
 * @author Administrator
 *
 */
class HanldMessageThread implements Runnable {

    private KafkaStream<byte[], byte[]> kafkaStream = null;
    private int num = 0;

    public HanldMessageThread(KafkaStream<byte[], byte[]> kafkaStream, int num) {
        super();
        this.kafkaStream = kafkaStream;
        this.num = num;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> iterator = kafkaStream.iterator();
        while(iterator.hasNext()) {
            String message = new String(iterator.next().message());
            System.out.println("Thread no: " + num + ", message: " + message);
        }
    }

}
//props.put("auto.commit.interval.ms", "1000");