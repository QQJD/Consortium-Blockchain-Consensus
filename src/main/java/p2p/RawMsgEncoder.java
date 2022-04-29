package p2p;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import pojo.ChannelInfo;
import pojo.Node;
import pojo.msg.MsgType;
import pojo.msg.RawMsg;
import utils.CryptoUtils;

import javax.crypto.SecretKey;

public class RawMsgEncoder extends MessageToByteEncoder<RawMsg> {

    public AttributeKey<ChannelInfo> channelInfoKey = NetworkInfo.getChannelInfoKey();
    public Node node = Node.getInstance();

    @Override
    protected void encode(ChannelHandlerContext ctx, RawMsg rawMsg, ByteBuf byteBuf) {

        // synchronized (Node.class) {

            // 对于CONN、CONN_REPLY消息，建立连接时双方密钥未协商完成，无法加密/签名
            // 对于REQ消息，暂不考虑用户与P2P网络之间的通信加密
            MsgType msgType = rawMsg.getMsgType();
            if (msgType == MsgType.CONN || msgType == MsgType.CONN_REPLY || msgType == MsgType.REQ) {
                byte[] json = rawMsg.getJson().getBytes();
                int jsonLen = json.length;
                int fullLen = 12 + jsonLen;
                byteBuf.writeInt(fullLen);
                byteBuf.writeInt(msgType.ordinal());
                byteBuf.writeInt(jsonLen);
                byteBuf.writeBytes(json);
                return;
            }

            // 加密，签名
            SecretKey secretKey = ctx.channel().attr(channelInfoKey).get().getSecretKey();
            byte[] enc = CryptoUtils.encrypt(node.getSymmetricAlgorithm(), rawMsg.getJson().getBytes(CharsetUtil.UTF_8), secretKey);
            byte[] sig = CryptoUtils.sign(node.getDigestAlgorithm(), node.getAsymmetricAlgorithm(), rawMsg.getJson().getBytes(CharsetUtil.UTF_8), node.getKeyPair().getPrivate());

            // 在消息中附加byte[]字段的长度，以便解码
            assert enc != null;
            int encLen = enc.length;
            assert sig != null;
            int sigLen = sig.length;

            // 输出，在包头附加总长度，以解决拆包问题
            int fullLen = 16 + encLen + sigLen;
            byteBuf.writeInt(fullLen);
            byteBuf.writeInt(msgType.ordinal());
            byteBuf.writeInt(encLen);
            byteBuf.writeInt(sigLen);
            byteBuf.writeBytes(enc);
            byteBuf.writeBytes(sig);

        // }

    }

}
