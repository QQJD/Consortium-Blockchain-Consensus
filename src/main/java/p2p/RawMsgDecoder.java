package p2p;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import pojo.ChannelInfo;
import pojo.Node;
import pojo.msg.MsgType;
import pojo.msg.RawMsg;
import utils.CryptoUtils;

import java.util.List;

public class RawMsgDecoder extends ByteToMessageDecoder {

    public AttributeKey<ChannelInfo> channelInfoKey = NetworkInfo.getChannelInfoKey();
    public Node node = Node.getInstance();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {

        // 解决拆包问题（tcp/udp协议限制传输包长小于约2k字节）：包头记录总长度，若收到的byteBuf中可读长度小于总长度，则等待下个包
        if(byteBuf.readableBytes() < byteBuf.getInt(0)) return;
        byteBuf.skipBytes(4);

        // 对于CONN、CONN_REPLY消息，建立连接时双方密钥未协商完成，无法解密/验签
        // 对于REQ消息，暂不考虑用户与P2P网络之间的通信解密
        MsgType msgType = MsgType.int2Enum(byteBuf.readInt());
        if(msgType == MsgType.CONN || msgType == MsgType.CONN_REPLY || msgType == MsgType.REQ) {
            int jsonLen = byteBuf.readInt();
            byte[] json = new byte[jsonLen];
            byteBuf.readBytes(json);
            RawMsg rawMsg = new RawMsg(msgType, new String(json, CharsetUtil.UTF_8), null);
            list.add(rawMsg);
            return;
        }

        // 读取byte[]字段的长度，并以此读取对应字段
        int encLen = byteBuf.readInt();
        int sigLen = byteBuf.readInt();
        byte[] enc = new byte[encLen];
        byte[] sig = new byte[sigLen];
        byteBuf.readBytes(enc);
        byteBuf.readBytes(sig);

        // 解密，验签
        byte[] json = CryptoUtils.decrypt(node.getSymmetricAlgorithm(), enc, ctx.channel().attr(channelInfoKey).get().getSecretKey());
        CryptoUtils.verify(node.getDigestAlgorithm(), node.getAsymmetricAlgorithm(), json, sig, ctx.channel().attr(channelInfoKey).get().getPublicKey());

        // 输出
        assert json != null;
        RawMsg rawMsg = new RawMsg(msgType, new String(json, CharsetUtil.UTF_8), null);
        list.add(rawMsg);

    }

}
