package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 密钥协商阶段节点间需要交互的信息
 */
@NoArgsConstructor @ToString
public class ConnMsg {

    @Getter
    private byte index;
    @Getter
    private byte[] tempPublicKey;
    @Getter
    private byte[] publicKey;

    public ConnMsg(byte index, byte[] tempPublicKey, byte[] publicKey) {
        this.index = index;
        this.tempPublicKey = tempPublicKey;
        this.publicKey = publicKey;
    }

}
