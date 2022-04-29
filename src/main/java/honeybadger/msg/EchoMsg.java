package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor
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

    @Override
    public String toString() {
        return "EchoMsg{" +
                "seq=" + seq +
                ", data=char[" + data.length() + "]" +
                ", root='" + root + '\'' +
                ", proof=" + Arrays.toString(proof) +
                '}';
    }
}
