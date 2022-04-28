package p2p.server;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pojo.msg.MsgType;
import pojo.msg.RawMsg;

public class P2PServerHandler extends SimpleChannelInboundHandler<RawMsg> {

    int cnt = 0;

    private P2PServerProcessor processor = P2PServerProcessor.getInstance();

    /**
     * 收到消息时，交给P2PServerProcessor对象进行路由处理
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
