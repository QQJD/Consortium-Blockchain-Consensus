package p2p.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import p2p.RawMsgDecoder;
import p2p.RawMsgEncoder;
import pojo.Node;

public class P2PServer {

    // 生成服务端的NIO-连接管理selector
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    // 生成服务端的NIO-请求管理selector
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    // 生成引导配置类
    private static ServerBootstrap bootstrap = new ServerBootstrap();

    /**
     * 启动P2P服务端
     */
    public static void run() {

        // 配置客户端，RawMsgEncoder+RawMsgDecoder完成消息加解密、签名/验签功能，并解决拆包问题
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65536)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 64))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new RawMsgEncoder());
                        pipeline.addLast(new RawMsgDecoder());
                        pipeline.addLast(new P2PServerHandler());
                    }
                });
        ChannelFuture cf = null;
        try {
            cf = bootstrap.bind(Node.getInstance().getPort()).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cf.channel().closeFuture();

    }

    /**
     * 客户端运行过程中出现未捕获的异常，则关闭
     */
    public static void stop() {

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

    }

}
