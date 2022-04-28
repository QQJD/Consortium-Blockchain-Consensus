package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor @ToString
public class ReadyMsg {

    /**
     * 请求序号（共识轮次）
     */
    @Getter
    private int seq;
    /**
     * merkle root
     */
    @Getter
    private String root;

    public ReadyMsg(int seq, String root) {
        this.seq = seq;
        this.root = root;
    }

}
