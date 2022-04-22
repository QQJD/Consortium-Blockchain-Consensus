package p2p;

import p2p.client.P2PClient;
import p2p.server.P2PServer;
import java.util.Scanner;

public class P2PInitialization {

    public static void initP2P() {

        try {
            P2PServer.run();
            P2PClient.run();
            new Scanner(System.in).next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            P2PServer.stop();
            P2PClient.stop();
        }

    }

    /**
     * 节点作为client反向连接时，调用该函数
     * @param ip peer的ip
     * @param port peer的port
     */
    public static void reconnect(String ip, short port) {
        P2PClient.reconnect(ip, port);
    }

}
