package com.fulan.flume.avro;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
//测试命令：flume-ng agent -c /opt/flume/conf/ -f /data/flume/test/netcat.flm -n a1 -Dflume.root.logger=INFO,console
//使用flume监听44444端口
//模拟向服务器发送数据
public class TcpClient {
    public static void main(String[] args) throws Exception {
        Socket client = new Socket ("learn", 44444);
        try {
            PrintWriter output =
                    new PrintWriter(client.getOutputStream(), true);
            Scanner cin = new Scanner(System.in);
            String words;
            while (cin.hasNext()) {
                words = cin.nextLine();

                output.println(words);

                System.out.println( "写出了数据:" + words);
            }

            cin.close();
        } finally {
            client.close();
        }
    }
}
