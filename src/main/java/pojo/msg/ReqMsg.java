package pojo.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor @ToString
public class ReqMsg {

    @Getter
    private int seq;
    @Getter
    private String body;

    public ReqMsg(int seq, String body) {
        this.seq = seq;
        this.body = body;
    }

}
