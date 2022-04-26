package utils;

import io.netty.channel.group.ChannelGroup;
import p2p.client.P2PClientProcessor;
import pojo.msg.RawMsg;

public class SendUtils {

    public static ChannelGroup channelGroup = P2PClientProcessor.getInstance().getChannelGroup();

    /**
     * 作为client，向所有peer广播消息
     * @param rawMsg 需要广播的消息
     */
    public static void publishToServer(RawMsg rawMsg) {
        channelGroup.writeAndFlush(rawMsg);
    }

}
