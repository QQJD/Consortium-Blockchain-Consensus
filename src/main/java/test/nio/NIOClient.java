package test.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOClient {

    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        if(!socketChannel.connect(inetSocketAddress)) {
            while(!socketChannel.finishConnect()) {
                System.out.println("连接需要时间，客户端不会阻塞");
            }
        }
        String str = "hello Nio!";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        System.out.println(buffer);
        socketChannel.write(buffer);
        System.in.read();
    }

}
