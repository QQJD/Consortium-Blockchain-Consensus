package p2p;

import io.netty.channel.ChannelId;
import io.netty.util.AttributeKey;
import lombok.Getter;
import pojo.ChannelInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储节点连接到的peers信息
 */
public class NetworkInfo {

    /**
     * 节点作为client连接到的peers，为方便向某个节点发送消息时，查询通过哪个channel发送，存储节点index到channelId的映射
     */
    private static Map<Byte, ChannelId> clientPeers = new HashMap<>();
    /**
     * 节点作为server连接到的peers
     */
    private static Map<Byte, ChannelId> serverPeers = new HashMap<>();
    @Getter
    private static AttributeKey<ChannelInfo> channelInfoKey = AttributeKey.valueOf("channelInfo");

    synchronized public static boolean addClientPeer(byte index, ChannelId id) {
        if (clientPeers.containsKey(index)) {
            return false;
        } else {
            clientPeers.put(index, id);
            return true;
        }
    }

    public static boolean removeClientPeer(byte index) {
        if (!clientPeers.containsKey(index)) {
            return false;
        } else {
            clientPeers.remove(index);
            return true;
        }
    }

    public static boolean containsClientPeer(byte index) {
        return clientPeers.containsKey(index);
    }

    synchronized public static boolean addServerPeer(byte index, ChannelId id) {
        if (serverPeers.containsKey(index)) {
            return false;
        } else {
            serverPeers.put(index, id);
            return true;
        }
    }

    public static boolean removeServerPeer(byte index) {
        if (!serverPeers.containsKey(index)) {
            return false;
        } else {
            serverPeers.remove(index);
            return true;
        }
    }

    public static boolean containsServerPeer(byte index) {
        return serverPeers.containsKey(index);
    }

    /**
     * @return peer总数
     */
    public static byte getN() {
        return (byte) clientPeers.size();
    }

    /**
     * @return 容错上限
     */
    public static byte getF() {
        return (byte) (clientPeers.size() / 3);
    }

}
