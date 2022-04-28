package p2p.server;

import com.google.gson.Gson;
import honeybadger.msg.*;
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
import pojo.msg.ReqMsg;
import utils.CryptoUtils;
import pojo.msg.MsgType;
import utils.LocalUtils;

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
            synchronized (P2PServerProcessor.class) {
                if(processor == null) {
                    processor = new P2PServerProcessor();
                    return processor;
                }
                return processor;
            }
        }
        return processor;
    }

    /**
     * 请求路由
     * @param ctx channel连接的上下文对象
     * @param msgType 收到的消息类型
     * @param json json化的消息内容
     */
    public void route(ChannelHandlerContext ctx, MsgType msgType, String json) {
        switch (msgType) {
            case CONN:
                ConnMsg connMsg = gson.fromJson(json, ConnMsg.class);
                conn(ctx, connMsg);
                break;
            case REQ:
                ReqMsg reqMsg = gson.fromJson(json, ReqMsg.class);
                req(ctx, reqMsg);
                break;
            case VAL:
                ValMsg valMsg = gson.fromJson(json, ValMsg.class);
                val(ctx, valMsg);
                break;
            case ECHO:
                EchoMsg echoMsg = gson.fromJson(json, EchoMsg.class);
                echo(ctx, echoMsg);
                break;
            case READY:
                ReadyMsg readyMsg = gson.fromJson(json, ReadyMsg.class);
                ready(ctx, readyMsg);
                break;
            case BVAL:
                BvalMsg bvalMsg = gson.fromJson(json, BvalMsg.class);
                bval(ctx, bvalMsg);
                break;
            case AUX:
                AuxMsg auxMsg = gson.fromJson(json, AuxMsg.class);
                aux(ctx, auxMsg);
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

        log.info(String.format("[%s->%s][RECEIVE][CONN]: %s", connMsg.getIndex(),node.getIndex(), connMsg));

        // 根据peer的临时公钥和自己的临时私钥，生成对称密钥
        byte[] byteTempPublicKey = LocalUtils.hex2Bytes(connMsg.getTempPublicKey());
        DHPublicKey tempPublicKey = (DHPublicKey) CryptoUtils.parseEncodedPublicKey("DH", byteTempPublicKey);
        KeyPair keyPair = CryptoUtils.generateKeyPairWithParams(tempPublicKey);
        DHPrivateKey tempPrivateKey = (DHPrivateKey) keyPair.getPrivate();
        SecretKey secretKey = CryptoUtils.generateSecretKey(node.getDigestAlgorithm(),
                node.getSymmetricAlgorithm(),
                tempPublicKey,
                tempPrivateKey);

        // channel的密钥协商完成，存入channelGroup和NetworkInfo
        byte[] bytePublicKey = LocalUtils.hex2Bytes(connMsg.getPublicKey());
        PublicKey publicKey = CryptoUtils.parseEncodedPublicKey(node.getAsymmetricAlgorithm(), bytePublicKey);
        ChannelInfo channelInfo = new ChannelInfo(connMsg.getIndex(), null, null, secretKey, publicKey);
        log.debug(String.format("[SERVER-%s][LOCAL][CHANNEL_ADD]: %s", node.getIndex(), channelInfo));
        Channel channel = ctx.channel();
        if (!NetworkInfo.addServerPeer(connMsg.getIndex(), channel.id())) return;
        channelGroup.add(channel);
        channel.attr(channelInfoKey).set(channelInfo);

        // 自己的永久公钥用于签名/验签，临时公钥用于密钥协商
        // byte[]转换为String传输，降低json化后消息大小
        String myTempPublicKey = LocalUtils.bytes2Hex(keyPair.getPublic().getEncoded());
        String myPublicKey = LocalUtils.bytes2Hex(node.getKeyPair().getPublic().getEncoded());
        ConnMsg connReplyMsg = new ConnMsg(node.getIndex(), myTempPublicKey, myPublicKey);
        String connReplyMsgJson = gson.toJson(connReplyMsg);
        RawMsg rawMsg = new RawMsg(MsgType.CONN_REPLY, connReplyMsgJson, null);
        ctx.writeAndFlush(rawMsg);
        log.info(String.format("[%s->%s][SEND][CONN_REPLY]: %s", node.getIndex(), connMsg.getIndex(), connReplyMsg));

        // 反向作为client连接peer，以生成反向channel的对称加密密钥
        // 因为不能只生成A作为client，B作为server的channel，有可能共识中存在B作为client向A发送消息的情况
        // if (!NetworkInfo.containsClientPeer(connMsg.getIndex())) {
        //     P2PInitialization.reconnect(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName(), node.getPort());
        // }
        // 单机环境测试用
        if (node.getIndex() != connMsg.getIndex() && !NetworkInfo.containsClientPeer(connMsg.getIndex())) {
            String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
            short port = (short) (8080 + connMsg.getIndex());
            P2PInitialization.reconnect(ip, port);
        }

        log.debug(String.format("channel数量：%s", channelGroup.size()));

    }

    /**
     * REQ请求路由（不同共识算法对REQ请求的处理可能不同）
     * @param ctx channel连接的上下文对象
     * @param reqMsg 收到的REQ消息，包含请求序号、具体内容
     */
    public void req(ChannelHandlerContext ctx, ReqMsg reqMsg) {
        log.info(String.format("[user->%s][RECEIVE][REQ]: %s", node.getIndex(), reqMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.req(reqMsg);
                break;
            default:
                break;
        }
    }

    /**
     * VAL请求路由
     * @param ctx channel连接的上下文对象
     * @param valMsg 收到的VAL消息，包含消息序号、proposal、merkle root、merkle proof
     */
    public void val(ChannelHandlerContext ctx, ValMsg valMsg) {
        byte index = ctx.channel().attr(channelInfoKey).get().getIndex();
        log.info(String.format("[%s->%s][RECEIVE][VAL]: %s", index, node.getIndex(), valMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.val(valMsg, index);
                break;
            default:
                break;
        }
    }

    /**
     * ECHO请求路由
     * @param ctx channel连接的上下文对象
     * @param echoMsg 收到的ECHO消息，包含消息序号、proposal、merkle root、merkle proof
     */
    public void echo(ChannelHandlerContext ctx, EchoMsg echoMsg) {
        byte index = ctx.channel().attr(channelInfoKey).get().getIndex();
        log.info(String.format("[%s->%s][RECEIVE][ECHO]: %s", index, node.getIndex(), echoMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.echo(echoMsg, index);
                break;
            default:
                break;
        }
    }

    /**
     * READY请求路由
     * @param ctx channel连接的上下文对象
     * @param readyMsg 收到的READY消息，包含消息序号、merkle root
     */
    public void ready(ChannelHandlerContext ctx, ReadyMsg readyMsg) {
        byte index = ctx.channel().attr(channelInfoKey).get().getIndex();
        log.info(String.format("[%s->%s][RECEIVE][READY]: %s", index, node.getIndex(), readyMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.ready(readyMsg, index);
                break;
            default:
                break;
        }
    }

    /**
     * BVAL请求路由
     * @param ctx channel连接的上下文对象
     * @param bvalMsg 收到的BVAL消息，包含消息序号、proposal节点、BA轮次、是否同意
     */
    public void bval(ChannelHandlerContext ctx, BvalMsg bvalMsg) {
        byte index = ctx.channel().attr(channelInfoKey).get().getIndex();
        log.info(String.format("[%s->%s][RECEIVE][BVAL]: %s", index, node.getIndex(), bvalMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.bval(bvalMsg, index);
                break;
            default:
                break;
        }
    }

    /**
     * AUX请求路由
     * @param ctx channel连接的上下文对象
     * @param auxMsg 收到的AUX消息，包含消息序号、proposal节点、BA轮次、是否同意
     */
    public void aux(ChannelHandlerContext ctx, AuxMsg auxMsg) {
        byte index = ctx.channel().attr(channelInfoKey).get().getIndex();
        log.info(String.format("[%s->%s][RECEIVE][AUX]: %s", index, node.getIndex(), auxMsg));
        switch (node.getConsensusAlgorithm()) {
            case "HoneyBadger":
                honeybadger.protocol.MsgProcessor.aux(auxMsg, index);
                break;
            default:
                break;
        }
    }

}
