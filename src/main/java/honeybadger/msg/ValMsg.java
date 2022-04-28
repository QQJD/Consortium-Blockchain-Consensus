package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Honey Badger共识的VAL消息
 */
@NoArgsConstructor @ToString
public class ValMsg {

    /**
     * 请求序号（共识轮次）
     */
    @Getter
    private int seq;
    /**
     * proposal
     */
    @Getter
    private String data;
    /**
     * merkle root
     */
    @Getter
    private String root;
    /**
     * merkle proof
     */
    @Getter
    private String[] proof;

    public ValMsg(int seq, String data, String root, String[] proof) {
        this.seq = seq;
        this.data = data;
        this.root = root;
        this.proof = proof;
    }

}
