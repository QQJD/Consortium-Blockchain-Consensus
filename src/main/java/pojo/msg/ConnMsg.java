package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * 密钥协商阶段节点间需要交互的信息
 */
@NoArgsConstructor
public class ConnMsg {

    /**
     * peer的索引
     */
    @Getter
    private byte index;
    /**
     * peer为密钥协商临时生成的公钥
     */
    @Getter
    private byte[] tempPublicKey;
    /**
     * peer用于后续签名/验签的永久公钥
     */
    @Getter
    private byte[] publicKey;

    public ConnMsg(byte index, byte[] tempPublicKey, byte[] publicKey) {
        this.index = index;
        this.tempPublicKey = tempPublicKey;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "ConnMsg{" +
                "index=" + index +
                ", tempPublicKey=" + "byte[" + tempPublicKey.length + "]" +
                ", publicKey=" + "byte[" + publicKey.length + "]" +
                '}';
    }

}
