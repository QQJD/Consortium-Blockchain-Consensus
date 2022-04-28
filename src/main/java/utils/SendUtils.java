package utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import p2p.client.P2PClientProcessor;
import pojo.msg.RawMsg;

import java.util.Iterator;

public class SendUtils {

    public static ChannelGroup channelGroup = P2PClientProcessor.getInstance().getChannelGroup();

    /**
     * 作为client，向所有peer广播消息
     * @param rawMsg 需要广播的消息
     */
    public static void publishToServer(RawMsg rawMsg) {
        Iterator<Channel> iterator = channelGroup.iterator();
        while (iterator.hasNext()) {
            Channel ch = iterator.next();
            ch.writeAndFlush(rawMsg).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    System.out.println(future.cause().getMessage());
                }
            });
        }
    }

}
