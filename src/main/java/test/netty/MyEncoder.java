package test.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import pojo.msg.RawMsg;

public class MyEncoder extends MessageToByteEncoder<RawMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RawMsg msg, ByteBuf out) throws Exception {
        System.out.println(msg);
        out.writeInt(1);
    }
}
