package pojo;

import lombok.Getter;
import lombok.ToString;
import utils.CryptoUtils;
import utils.LocalUtils;

import java.io.*;
import java.security.KeyPair;
import java.util.Properties;

/**
 * 单例对象，存储节点自身配置信息
 */
@ToString
public class Node {

    @Getter
    private byte index;
    @Getter
    private long address;
    @Getter
    private short port;
    @Getter
    private String asymmetricAlgorithm;
    @Getter
    private String symmetricAlgorithm;
    @Getter
    private String digestAlgorithm;
    @Getter
    private String consensusAlgorithm;
    @Getter
    private KeyPair keyPair;

    private static Node node;

    private Node() {
        Properties prop = new Properties();
        String propUrl = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "node.properties";
        Reader resource;
        try {
            resource = new FileReader(propUrl);
            prop.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.index = Byte.parseByte(prop.getProperty("index"));
        this.address = LocalUtils.ipStr2Long(prop.getProperty("ip"));
        this.port = Short.parseShort(prop.getProperty("port"));
        this.asymmetricAlgorithm = prop.getProperty("asymmetricAlgorithm");
        this.symmetricAlgorithm = prop.getProperty("symmetricAlgorithm");
        this.digestAlgorithm = prop.getProperty("digestAlgorithm");
        this.consensusAlgorithm = prop.getProperty("consensusAlgorithm");
        this.keyPair = CryptoUtils.generateKeyPair(asymmetricAlgorithm);
    }

    public static Node getInstance() {
        if(node == null) {
            synchronized (Node.class) {
                if(node == null) {
                    node = new Node();
                    return node;
                }
                return node;
            }
        }
        return node;
    }

}
