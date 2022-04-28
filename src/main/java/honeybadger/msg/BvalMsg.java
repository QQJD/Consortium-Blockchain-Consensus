package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor @ToString
public class BvalMsg {

    /**
     * 请求序号
     */
    @Getter
    private int seq;
    /**
     * 对于哪个节点在RBC阶段的proposal（也就是merkle root对应的内容）进行共识
     */
    @Getter
    private byte src;
    /**
     * BA共识轮次（对于一个seq中某个src的proposal，BA可能需要共识多个轮次才能完成）
     */
    @Getter
    private byte round;
    /**
     * 1-同意节点proposal，0-拒绝节点proposal
     */
    @Getter
    private boolean est;

    public BvalMsg(int seq, byte src, byte round, boolean est) {
        this.seq = seq;
        this.src = src;
        this.round = round;
        this.est = est;
    }

}
