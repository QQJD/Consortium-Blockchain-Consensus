package p2p.client;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import p2p.NetworkInfo;
import pojo.ChannelInfo;
import pojo.Node;
import pojo.msg.ConnMsg;
import pojo.msg.RawMsg;
import utils.CryptoUtils;
import pojo.msg.MsgType;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * 单例对象，用于路由/处理客户端收到的消息
 */
@Slf4j
public class P2PClientProcessor {

    AttributeKey<ChannelInfo> channelInfoKey = NetworkInfo.getChannelInfoKey();

    @Getter
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private Node node = Node.getInstance();
    private Gson gson = new Gson();

    private static P2PClientProcessor processor;

    private P2PClientProcessor() {

    }

    public static P2PClientProcessor getInstance() {
        if(processor == null) {
            synchronized (Node.class) {
                if(processor == null) {
                    processor = new P2PClientProcessor();
                    return processor;
                }
                return processor;
            }
        }
        return processor;
    }

    /**
     * 1.生成临时公私钥，开始密钥协商
     * 2.将自己的永久公钥发送给peer，以便后续签名/验签
     * @param ctx channel连接的上下文对象
     */
    public void conn(ChannelHandlerContext ctx) {

        // 生成临时公私钥与channel信息
        KeyPair keyPair = CryptoUtils.generateKeyPair("DH");
        ChannelInfo channelInfo = new ChannelInfo(null, (DHPrivateKey) keyPair.getPrivate(), null, null);

        // 将自己的临时私钥绑定到channel对象
        log.debug(String.format("[CLIENT-%s LOCAL][CHANNEL_INFO]: %s", node.getIndex(), channelInfo));
        ctx.channel().attr(channelInfoKey).set(channelInfo);

        // 自己的永久公钥用于签名/验签，临时公钥用于密钥协商
        ConnMsg connMsg = new ConnMsg(node.getIndex(), keyPair.getPublic().getEncoded(), node.getKeyPair().getPublic().getEncoded());
        String connMsgJson = gson.toJson(connMsg);
        RawMsg rawMsg = new RawMsg(MsgType.CONN, connMsgJson, null);
        ctx.writeAndFlush(rawMsg);
        log.info(String.format("[CLIENT-%s SEND][CONN]: %s", node.getIndex(), connMsg));

    }

    public void route(ChannelHandlerContext channelHandlerContext, MsgType msgType, String json) {
        switch (msgType) {
            case CONN_REPLY:
                ConnMsg connReplyMsg = gson.fromJson(json, ConnMsg.class);
                connReply(channelHandlerContext, connReplyMsg);
                break;
            default:
                break;
        }
    }

    /**
     * 1.生成用于该channel通信的对称加密密钥
     * 2.获取peer的永久公钥，用于该channel的签名/验签
     * @param ctx channel连接的上下文对象
     * @param connReplyMsg 收到的CONN_REPLY消息，包含peer临时公钥、永久公钥
     */
    public void connReply(ChannelHandlerContext ctx, ConnMsg connReplyMsg) {

        log.info(String.format("[CLIENT-%s RECEIVE][CONN_REPLY]: %s", node.getIndex(), connReplyMsg));

        // channel的密钥协商完成，存入channelGroup和NetworkInfo
        Channel channel = ctx.channel();
        ChannelInfo channelInfo = channel.attr(channelInfoKey).get();
        if (!NetworkInfo.addClientPeer(connReplyMsg.getIndex(), channel.id())) return;
        channelGroup.add(channel);

        // 根据peer的临时公钥和自己的临时私钥，生成对称密钥
        DHPublicKey tempPublicKey = (DHPublicKey) CryptoUtils.parseEncodedPublicKey("DH", connReplyMsg.getTempPublicKey());
        DHPrivateKey tempPrivateKey = channelInfo.getTempPrivateKey();
        SecretKey secretKey = CryptoUtils.generateSecretKey(node.getDigestAlgorithm(), node.getSymmetricAlgorithm(), tempPublicKey, tempPrivateKey);

        // 临时私钥已经不需要
        channelInfo.setTempPrivateKey(null);

        // channel绑定的对称密钥用于加密/解密，channel绑定的peer公钥用于签名/验签
        channelInfo.setSecretKey(secretKey);
        PublicKey publicKey = CryptoUtils.parseEncodedPublicKey(node.getAsymmetricAlgorithm(), connReplyMsg.getPublicKey());
        channelInfo.setPublicKey(publicKey);

        log.debug(String.format("[CLIENT-%s LOCAL][CHANNEL_ADD]: %s", node.getIndex(), channelInfo));
        log.debug(String.format("channel数量：%s", channelGroup.size()));

    }

}
