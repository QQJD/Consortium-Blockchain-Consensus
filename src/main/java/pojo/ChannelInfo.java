package pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import java.security.PublicKey;

/**
 * channel对象绑定的信息，用于密钥协商、通信对称加解密、签名/验签
 */
@NoArgsConstructor
public class ChannelInfo {

    @Getter @Setter
    private byte index;
    /**
     * 用于密钥协商阶段，之后丢弃
     */
    @Getter @Setter
    private DHPrivateKey tempPublicKey;
    /**
     * 用于密钥协商阶段，之后丢弃
     */
    @Getter @Setter
    private DHPrivateKey tempPrivateKey;
    /**
     * 用于对称加解密
     */
    @Getter @Setter
    private SecretKey secretKey;
    /**
     * 用于签名/验签
     */
    @Getter @Setter
    private PublicKey publicKey;

    public ChannelInfo(byte index, DHPrivateKey tempPublicKey, DHPrivateKey tempPrivateKey, SecretKey secretKey,
                       PublicKey publicKey) {
        this.index = index;
        this.tempPublicKey = tempPublicKey;
        this.tempPrivateKey = tempPrivateKey;
        this.secretKey = secretKey;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                "index=" + index +
                ", tempPublicKey=" + (tempPublicKey == null ? null : tempPublicKey.getAlgorithm()) +
                ", tempPrivateKey=" + (tempPrivateKey == null ? null : tempPrivateKey.getAlgorithm()) +
                ", secretKey=" + (secretKey == null ? null : secretKey.getAlgorithm()) +
                ", publicKey=" + (publicKey == null ? null : publicKey.getAlgorithm()) +
                '}';
    }

}
