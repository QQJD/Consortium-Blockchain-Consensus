package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 所有消息类型的上层封装
 */
@NoArgsConstructor @ToString
public class RawMsg {

    @Getter @Setter
    private MsgType msgType;
    /**
     * 具体消息类型的对象json化后放入RawMsg对象
     */
    @Getter @Setter
    private String json;
    /**
     * 对json进行摘要+签名，用于验证发送者
     */
    @Getter @Setter
    private String signature;

    public RawMsg(MsgType msgType, String json, String signature) {
        this.msgType = msgType;
        this.json = json;
        this.signature = signature;
    }

}
