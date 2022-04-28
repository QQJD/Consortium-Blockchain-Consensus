package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * 密钥协商阶段节点间需要交互的信息
 */
@NoArgsConstructor @ToString
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
    private String tempPublicKey;
    /**
     * peer用于后续签名/验签的永久公钥
     */
    @Getter
    private String publicKey;

    public ConnMsg(byte index, String tempPublicKey, String publicKey) {
        this.index = index;
        this.tempPublicKey = tempPublicKey;
        this.publicKey = publicKey;
    }

}
