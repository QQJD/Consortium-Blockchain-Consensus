package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * Honey Badger共识的VAL消息
 */
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "ValMsg{" +
                "seq=" + seq +
                ", data=char[" + data.length() + "]" +
                ", root='" + root + '\'' +
                ", proof=" + Arrays.toString(proof) +
                '}';
    }
}
