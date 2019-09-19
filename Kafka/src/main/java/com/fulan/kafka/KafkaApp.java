package com.fulan.kafka;


import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * Test kafka
 */
public class KafkaApp {
    private final static String URL = "learn:2181";
    private final static String NAME = "test";

    // 创建主题
    private static void createTopic() {
        ZkUtils zkUtils = ZkUtils.apply(URL, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 创建一个单分区单副本名为t1的topic
        AdminUtils.createTopic(zkUtils, NAME, 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
        zkUtils.close();
        System.out.println("创建成功!");
    }

    // 删除主题(未彻底删除)
    private static void deleteTopic() {
        ZkUtils zkUtils = ZkUtils.apply(URL, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 删除topic 't1'
        AdminUtils.deleteTopic(zkUtils, NAME);
        zkUtils.close();
        System.out.println("删除成功!");
    }

    // 修改主题
    private static void editTopic() {
        ZkUtils zkUtils = ZkUtils.apply(URL, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), NAME);
        // 增加topic级别属性
        props.put("min.cleanable.dirty.ratio", "0.3");
        // 删除topic级别属性
        props.remove("max.message.bytes");
        // 修改topic 'test'的属性
        AdminUtils.changeTopicConfig(zkUtils, NAME, props);
        zkUtils.close();
    }

    // 主题读取
    private static void queryTopic() {
        ZkUtils zkUtils = ZkUtils.apply(URL, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 获取topic 'test'的topic属性属性
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), NAME);
        // 查询topic-level属性
        Iterator it = props.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " = " + value);
        }
        zkUtils.close();
    }

    /**
     * @Description: 生产者
     */
    private static void producer() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", "learn:2181");//声明zk
        properties.put("serializer.class", StringEncoder.class.getName());
        properties.put("metadata.broker.list", "learn");// 声明kafka broker

        Producer producer = new Producer<Integer, String>(new ProducerConfig(properties));
        int i=0;
        while(true){
            producer.send(new KeyedMessage<Integer, String>(NAME, "message: " + i++));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Description: 消费者
     */
    private static void customer() {
        try {
            Properties properties = new Properties();
            properties.put("zookeeper.connect", URL);
            properties.put("auto.commit.enable", "true");
            properties.put("auto.commit.interval.ms", "60000");
            properties.put("group.id", "test");

            ConsumerConfig consumerConfig = new ConsumerConfig(properties);

            ConsumerConnector javaConsumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

            // topic的过滤器
            Whitelist whitelist = new Whitelist("test");
            List<KafkaStream<byte[], byte[]>> partitions = javaConsumerConnector
                    .createMessageStreamsByFilter(whitelist);

            if (partitions == null) {
                System.out.println("empty!");
                TimeUnit.SECONDS.sleep(1);
            }

            System.out.println("partitions:"+partitions.size());

            // 消费消息
            for (KafkaStream<byte[], byte[]> partition : partitions) {

                ConsumerIterator<byte[], byte[]> iterator = partition.iterator();
                while (iterator.hasNext()) {
                    MessageAndMetadata<byte[], byte[]> next = iterator.next();
                    System.out.println("partiton:" + next.partition());
                    System.out.println("offset:" + next.offset());
                    System.out.println("接收到message:" + new String(next.message(), "utf-8"));
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static void main(String[] args) {
        createTopic();
//		 deleteTopic();
        // editTopic();
        // queryTopic();

        //producer();

		customer();
    }
}
