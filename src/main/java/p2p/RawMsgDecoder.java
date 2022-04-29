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

        // synchronized (Node.class) {

            // 解决拆包问题（tcp/udp协议限制传输包长小于约2k字节）：包头记录总长度，若收到的byteBuf中可读长度小于总长度，则等待下个包
            if (byteBuf.readableBytes() < byteBuf.getInt(byteBuf.readerIndex())) return;

            // System.out.println(byteBuf);
            // byteBuf.markReaderIndex();
            // byteBuf.readerIndex(0);
            // while (byteBuf.readableBytes() > 0 && byteBuf.readableBytes() >= byteBuf.getInt(byteBuf.readerIndex())) {
            //     System.out.println("fullLen: " + byteBuf.readInt());
            //     int msgType = byteBuf.readInt();
            //     System.out.println("msgType: " + msgType);
            //     if (msgType == 0 || msgType == 1 || msgType == 2) {
            //         int encLen = byteBuf.readInt();
            //         byte[] enc = new byte[encLen];
            //         System.out.println("encLen: " + encLen);
            //         byteBuf.readBytes(enc);
            //     } else {
            //         int encLen = byteBuf.readInt();
            //         int sigLen = byteBuf.readInt();
            //         byte[] enc = new byte[encLen];
            //         byte[] sig = new byte[sigLen];
            //         System.out.println("encLen: " + encLen);
            //         System.out.println("sigLen: " + sigLen);
            //         System.out.println("read enc: " + byteBuf);
            //         byteBuf.readBytes(enc);
            //         System.out.println("read sig: " + byteBuf);
            //         byteBuf.readBytes(sig);
            //     }
            //     System.out.println("--------------------------------");
            // }
            // byteBuf.resetReaderIndex();

            byteBuf.skipBytes(4);

            // 对于CONN、CONN_REPLY消息，建立连接时双方密钥未协商完成，无法解密/验签
            // 对于REQ消息，暂不考虑用户与P2P网络之间的通信解密
            MsgType msgType = MsgType.int2Enum(byteBuf.readInt());
            if (msgType == MsgType.CONN || msgType == MsgType.CONN_REPLY || msgType == MsgType.REQ) {
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

        // }

    }

}
