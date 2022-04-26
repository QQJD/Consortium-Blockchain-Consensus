package honeybadger.msg;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Honey Badger共识的VAL消息
 */
@NoArgsConstructor
public class ValMsg {

    /**
     * proposal
     */
    @Getter
    byte[] data;
    /**
     * merkle root
     */
    @Getter
    byte[] root;
    /**
     * merkle proof
     */
    @Getter
    byte[][] proof;

    public ValMsg(byte[] data, byte[] root, byte[][] proof) {
        this.data = data;
        this.root = root;
        this.proof = proof;
    }

    @Override
    public String toString() {
        return "ValMsg{" +
                "data=" + "byte[" + data.length + "]" +
                ", root=" + "byte[" + root.length + "]" +
                ", proof=" + "byte[" + proof.length + "]" + "[" + proof[0].length + "]" +
                '}';
    }
}
