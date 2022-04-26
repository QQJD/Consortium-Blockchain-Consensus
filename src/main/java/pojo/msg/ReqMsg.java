package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户向P2P网络发送的请求，也就是需要共识的消息
 */
@NoArgsConstructor @ToString
public class ReqMsg {

    /**
     * 请求序号
     */
    @Getter
    private int seq;
    /**
     * 请求内容（待共识的消息）
     */
    @Getter
    private String body;

    public ReqMsg(int seq, String body) {
        this.seq = seq;
        this.body = body;
    }

}
