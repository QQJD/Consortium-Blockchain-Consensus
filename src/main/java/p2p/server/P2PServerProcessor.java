package p2p.server;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import p2p.NetworkInfo;
import p2p.P2PInitialization;
import pojo.ChannelInfo;
import pojo.Node;
import pojo.msg.ConnMsg;
import pojo.msg.RawMsg;
import utils.CryptoUtils;
import pojo.msg.MsgType;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * 单例对象，用于路由/处理服务端收到的消息
 */
@Slf4j
public class P2PServerProcessor {

    AttributeKey<ChannelInfo> channelInfoKey = NetworkInfo.getChannelInfoKey();

    @Getter
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private Node node = Node.getInstance();
    private Gson gson = new Gson();

    private static P2PServerProcessor processor;

    private P2PServerProcessor() {

    }

    public static P2PServerProcessor getInstance() {
        if(processor == null) {
            synchronized (Node.class) {
                if(processor == null) {
                    processor = new P2PServerProcessor();
                    return processor;
                }
                return processor;
            }
        }
        return processor;
    }

    public void route(ChannelHandlerContext ctx, MsgType msgType, String json) {
        switch (msgType) {
            case CONN:
                ConnMsg connMsg = new Gson().fromJson(json, ConnMsg.class);
                conn(ctx, connMsg);
                break;
            default:
                break;
        }
    }

    /**
     * 1.生成临时公私钥，完成自己的密钥协商，生成用于该channel通信的对称加密密钥
     * 2.将自己的临时公钥发送给peer，以便peer完成密钥协商
     * 3.将自己的永久公钥发送给peer，用于该channel的签名/验签
     * @param ctx channel连接的上下文对象
     * @param connMsg 收到的CONN消息，包含peer临时公钥、永久公钥
     */
    public void conn(ChannelHandlerContext ctx, ConnMsg connMsg) {

        log.info(String.format("[SERVER-%s RECEIVE][CONN]: %s", node.getIndex(), connMsg));

        // 根据peer的临时公钥和自己的临时私钥，生成对称密钥
        DHPublicKey tempPublicKey = (DHPublicKey) CryptoUtils.parseEncodedPublicKey("DH", connMsg.getTempPublicKey());
        KeyPair keyPair = CryptoUtils.generateKeyPairWithParams(tempPublicKey);
        DHPrivateKey tempPrivateKey = (DHPrivateKey) keyPair.getPrivate();
        SecretKey secretKey = CryptoUtils.generateSecretKey(node.getDigestAlgorithm(), node.getSymmetricAlgorithm(), tempPublicKey, tempPrivateKey);

        // channel的密钥协商完成，存入channelGroup和NetworkInfo
        PublicKey publicKey = CryptoUtils.parseEncodedPublicKey(node.getAsymmetricAlgorithm(), connMsg.getPublicKey());
        ChannelInfo channelInfo = new ChannelInfo(null, null, secretKey, publicKey);
        log.debug(String.format("[SERVER-%s LOCAL][CHANNEL_ADD]: %s", node.getIndex(), channelInfo));
        Channel channel = ctx.channel();
        if (!NetworkInfo.addServerPeer(connMsg.getIndex(), channel.id())) return;
        channelGroup.add(channel);
        channel.attr(channelInfoKey).set(channelInfo);

        // 自己的永久公钥用于签名/验签，临时公钥用于密钥协商
        ConnMsg connReplyMsg = new ConnMsg(node.getIndex(), keyPair.getPublic().getEncoded(), node.getKeyPair().getPublic().getEncoded());
        String connReplyMsgJson = gson.toJson(connReplyMsg);
        RawMsg rawMsg = new RawMsg(MsgType.CONN_REPLY, connReplyMsgJson, null);
        ctx.writeAndFlush(rawMsg);
        log.info(String.format("[SERVER-%s SEND][CONN_REPLY]: %s", node.getIndex(), connReplyMsg));

        // 反向作为client连接peer，以生成反向channel的对称加密密钥
        // 因为不能只生成A作为client，B作为server的channel，有可能共识中存在B作为client向A发送消息的情况
        // if (!NetworkInfo.containsClientPeer(connMsg.getIndex())) {
        //     P2PInitialization.reconnect(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName(), node.getPort());
        // }
        // 单机环境测试用
        if (node.getIndex() != connMsg.getIndex() && !NetworkInfo.containsClientPeer(connMsg.getIndex())) {
            P2PInitialization.reconnect(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName(), (short) (8080 + connMsg.getIndex()));
        }

        log.debug(String.format("channel数量：%s", channelGroup.size()));

    }

}
