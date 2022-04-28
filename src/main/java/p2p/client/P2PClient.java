package p2p.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import p2p.RawMsgDecoder;
import p2p.RawMsgEncoder;

import java.io.*;
import java.util.Properties;

@Slf4j
public class P2PClient {

    // 生成客户端的NIO-selector
    private static NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
    // 生成引导配置类
    private static Bootstrap bootstrap = new Bootstrap();


    /**
     * 启动P2P客户端，依次尝试连接配置文件中的所有peers
     */
    public static void run() {

        // 读取peers的套接字
        Properties prop = new Properties();
        String propUrl = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "peer.properties";
        Reader resource;
        try {
            resource = new FileReader(propUrl);
            prop.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String[] ips = prop.getProperty("ip").split(",");
        String[] portStrs = prop.getProperty("port").split(",");
        int[] ports = new int[portStrs.length];
        for (int i = 0; i < portStrs.length; i++) {
            ports[i] = Integer.parseInt(portStrs[i]);
        }

        // 配置客户端，RawMsgEncoder+RawMsgDecoder完成消息加解密、签名/验签功能，并解决拆包问题
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RawMsgEncoder());
                        pipeline.addLast(new RawMsgDecoder());
                        pipeline.addLast(new P2PClientHandler());
                    }
                });

        // 依次连接peers
        for (int i = 0; i < ips.length; i++) {
            ChannelFuture cf = null;
            try {
                cf = bootstrap.connect(ips[i], ports[i]).sync();
            } catch (Exception e) {
                // A未连接到B，可以等B上线后来连接A
                log.info(String.format("未成功连接%s:%s", ips[i], ports[i]));
            }
            if (cf != null) {
                cf.channel().closeFuture();
            }
        }

    }

    /**
     * 客户端运行过程中出现未捕获的异常，则关闭
     */
    public static void stop() {

        if (eventExecutors != null) {
            eventExecutors.shutdownGracefully();
        }

    }

    /**
     * A未连接到B，B上线后来连接A时，A通过该函数反向作为客户端连接B
     * @param ip peer的ip
     * @param port peer的port
     */
    public static void reconnect(String ip, int port) {
        ChannelFuture cf = null;
        try {
            cf = bootstrap.connect(ip, port).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cf != null) {
            cf.channel().closeFuture();
        }
    }

}
