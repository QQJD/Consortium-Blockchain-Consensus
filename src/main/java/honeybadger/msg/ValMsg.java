package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor @ToString
public class ValMsg {

    @Getter
    byte[] data;
    @Getter
    byte[] root;
    @Getter
    byte[][] proof;

    public ValMsg(byte[] data, byte[] root, byte[][] proof) {
        this.data = data;
        this.root = root;
        this.proof = proof;
    }

}
