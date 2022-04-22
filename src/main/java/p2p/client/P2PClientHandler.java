package p2p.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pojo.msg.RawMsg;

public class P2PClientHandler extends SimpleChannelInboundHandler<RawMsg> {

    private P2PClientProcessor processor = P2PClientProcessor.getInstance();

    /**
     * 节点启动并连接到peer时，直接通过CONN消息开始密钥协商
     * @param ctx channel连接的上下文对象
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        processor.conn(ctx);
    }

    /**
     * 收到消息时，交给P2PClientProcessor对象进行路由处理
     * @param ctx channel连接的上下文对象
     * @param rawMsg 收到的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RawMsg rawMsg) {
        processor.route(ctx, rawMsg.getMsgType(), rawMsg.getJson());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

}
