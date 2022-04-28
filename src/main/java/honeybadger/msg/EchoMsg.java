package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor @ToString
public class EchoMsg {

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

    public EchoMsg(int seq, String data, String root, String[] proof) {
        this.seq = seq;
        this.data = data;
        this.root = root;
        this.proof = proof;
    }

}
