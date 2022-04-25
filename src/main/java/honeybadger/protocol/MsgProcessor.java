package honeybadger.protocol;

import honeybadger.msg.ValMsg;
import io.netty.util.CharsetUtil;
import p2p.NetworkInfo;
import pojo.Node;
import pojo.msg.ReqMsg;
import utils.ErasureCodeUtils;
import utils.MerkleTree;

public class MsgProcessor {

    public static Node node = Node.getInstance();

    public static void req(ReqMsg reqMsg) {

        if (!MsgValidator.isReqValid(reqMsg)) {
            return;
        }

        String req = reqMsg.getBody();

        // 为保证性能，发送val消息时，每个节点仅广播所有消息的一部分，最后取所有节点proposal的并集作为共识结果
        String proposed = req.substring(0, req.length() / NetworkInfo.getN());

        // TODO：进行纠删编码，保证其他只要收到N-2f个val消息就能恢复内容
        byte[][] erasureEncoded = ErasureCodeUtils.encode(proposed.getBytes(CharsetUtil.UTF_8));

        // TODO：阈值加密，保证隐私性

        // 构造merkle树
        MerkleTree merkleTree = new MerkleTree(erasureEncoded, node.getDigestAlgorithm());

        // 生成proof
        byte[] data = erasureEncoded[node.getIndex()];
        byte[] root = merkleTree.getRoot();
        byte[][] proof = merkleTree.getProof(node.getIndex());

        ValMsg valMsg = new ValMsg(data, root, proof);


    }

}
