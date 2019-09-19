package com.fulan.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//提交任务到yarn集群：spark-submit  --master yarn --deploy-mode cluster --class org.apache.spark.examples.SparkPi /opt/spark/examples/jars/spark-examples_2.11-2.4.3.jar 10
//spark-submit  --master yarn --deploy-mode cluster --class com.fulan.spark.SparkTest  Spark-1.0-SNAPSHOT.jar
public class SparkTest {
    public static void main(String[] args) {
        fun ();
    }
    public static void fun(){
        SparkConf conf = new SparkConf ().setAppName("WorldCount").setMaster("yarn");
        JavaSparkContext sc = new JavaSparkContext (conf);
        JavaRDD<String> lines = sc.textFile("hdfs://learn:9000/data/input/wordcount.txt");
        JavaRDD<String> flatMapRdd = lines.flatMap(new FlatMapFunction<String, String> () {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterator<String> call(String s) throws Exception {
                // 拆分字符串 为一个数组
                String[] word = s.split(" ");
                // 把数组转换成List集合
                List<String> list = Arrays.asList(word);
                // 把list集合转换成Iterator集合
                Iterator<String> it = list.iterator();
                return it;
            }
        });
        // 使用mapToPair进行map操作 形如： （word，1）
        JavaPairRDD<String, Integer> mapRdd = flatMapRdd.mapToPair(new PairFunction<String, String, Integer> () {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s, 1);
            }
        });
        // 使用reduceByKey进行单词统计 返回 （word，CountSum）
        JavaPairRDD<String, Integer> res;
        res = mapRdd.reduceByKey(new Function2<Integer, Integer, Integer> () {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer a, Integer b) throws Exception {
                return a + b;
            }
        });
        res.saveAsTextFile("hdfs://learn:9000/data/out/res_yarn_wordcount");
        // 把最后的 rdd输出
        res.foreach(new VoidFunction<Tuple2<String, Integer>> () {
            private static final long serialVersionUID = 1L;
            @Override
            public void call(Tuple2<String, Integer> tuple2) throws Exception {
                System.out.println(tuple2._1+" "+tuple2._2);
            }
        });
    }
}
