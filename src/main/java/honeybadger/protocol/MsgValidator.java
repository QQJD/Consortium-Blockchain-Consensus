package honeybadger.protocol;

import honeybadger.msg.*;
import honeybadger.status.ConsensusStatus;
import honeybadger.status.StatusSetUtils;
import pojo.Node;
import pojo.msg.ReqMsg;
import utils.LocalUtils;
import utils.MerkleTree;

public class MsgValidator {

    public static Node node = Node.getInstance();

    /**
     * 1.小于currSeq：丢弃
     * 2.大于currSeq：暂存
     * 3.等于currSeq：不做处理并返回true
     * @param reqMsg 收到的REQ消息
     * @return 是否有效
     */
    public static boolean isReqValid(ReqMsg reqMsg) {

        int seq = reqMsg.getSeq();

        // 小于当前序号的请求直接丢弃
        if (seq < ConsensusStatus.currSeq) {
            return false;
        }

        // 大于当前序号的请求缓存到reqs中，等待之后处理
        if (seq > ConsensusStatus.currSeq) {
            ConsensusStatus.reqs.put(seq, reqMsg);
            return false;
        }

        return true;

    }

    /**
     * 1.已经在rejectVals中则丢弃
     * 2.同一个节点重复发送的VAL消息（seq相同）丢弃
     * @param valMsg 收到的VAL消息
     * @param index 发送方节点索引
     * @return 是否有效
     */
    public static boolean isValValid(ValMsg valMsg, byte index) {

        int seq = valMsg.getSeq();

        // 如果ConsensusStatus中记录seq序号下的VAL消息不再需要，则直接丢弃
        if (StatusSetUtils.isRejectVals(seq)) {
            return false;
        }

        // 如果已经收到该节点的seq序号对应的VAL消息，则直接丢弃，防止重放攻击
        if (StatusSetUtils.isInVals(seq, index)) {
            return false;
        }

        // 如果ConsensusStatus尚未记录该序号下VAL集合，则需要创建一个
        // 需要保证同时只有一个线程创建集合，加锁
        // eg.线程1进入if-线程2进入if-线程1创建、add-线程2创建，这种情况下会丢失线程1add的节点
        StatusSetUtils.addVals(seq, index);

        return true;

    }

    /**
     * 1.已经在rejectEchos中则丢弃
     * 2.同一个节点重复发送的ECHO消息（seq相同+merkle root相同）丢弃
     * 3.merkle proof无效则丢弃
     * @param echoMsg 收到的ECHO消息
     * @param index 发送方节点索引
     * @return 是否有效
     */
    public static boolean isEchoValid(EchoMsg echoMsg, byte index) {

        int seq = echoMsg.getSeq();
        String rootStr = echoMsg.getRoot();

        // 若merkle proof无效,则丢弃请求
        byte[] data = LocalUtils.hex2Bytes(echoMsg.getData());
        byte[] root = LocalUtils.hex2Bytes(rootStr);
        String[] proofStr = echoMsg.getProof();
        byte[][] proof = new byte[proofStr.length][];
        for (int i = 0; i < proof.length; i++) {
            proof[i] = LocalUtils.hex2Bytes(proofStr[i]);
        }
        if (!MerkleTree.isValidProof(node.getDigestAlgorithm(), data, root, proof)) {
            return false;
        }

        // 是否还需要
        if (StatusSetUtils.isRejectEchos(seq, rootStr)) {
            return false;
        }

        // 防止重放
        if (StatusSetUtils.isInEchos(seq, rootStr, index)) {
            return false;
        }

        // 添加到集合中，需要加锁
        StatusSetUtils.addEchos(seq, rootStr, index, echoMsg);

        return true;

    }

    /**
     * 1.已经在rejectReadys中则丢弃
     * 2.同一个节点重复发送的READY消息（seq相同+merkle root相同）丢弃
     * @param readyMsg 收到的READY消息
     * @param index 发送方节点索引
     * @return 是否有效
     */
    public static boolean isReadyValid(ReadyMsg readyMsg, byte index) {

        int seq = readyMsg.getSeq();
        String root = readyMsg.getRoot();

        if (StatusSetUtils.isRejectReadys(seq, root)) {
            return false;
        }
        if (StatusSetUtils.isInReadys(seq, root, index)) {
            return false;
        }
        StatusSetUtils.addReadys(seq, root, index);

        return true;

    }

    /**
     * 1.已经在rejectBvals中则丢弃
     * 2.同一个节点重复发送的BVAL消息（seq相同+src相同+round相同+est相同）丢弃
     * @param bvalMsg 收到的BVAL消息
     * @param index 发送方节点索引
     * @return 是否有效
     */
    public static boolean isBvalValid(BvalMsg bvalMsg, byte index) {

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        // 如果收到的是est=0的BVAL消息
        if(!bvalMsg.isEst()) {
            if (StatusSetUtils.isRejectBvals0(seq, src, round)) {
                return false;
            }
            if (StatusSetUtils.isInBvals0(seq, src, round, index)) {
                return false;
            }
            StatusSetUtils.addBvals0(seq, src, round, index);
            return true;
        }
        // 如果收到的是est=1的BVAL消息
        else {
            if (StatusSetUtils.isRejectBvals1(seq, src, round)) {
                return false;
            }
            if (StatusSetUtils.isInBvals1(seq, src, round, index)) {
                return false;
            }
            StatusSetUtils.addBvals1(seq, src, round, index);
            return true;
        }

    }

    /**
     * 1.已经在rejectAuxs中则丢弃
     * 2.同一个节点重复发送的AUX消息（seq相同+src相同+round相同）丢弃
     * @param auxMsg 收到的READY消息
     * @param index 发送方节点索引
     * @return 是否有效
     */
    public static boolean isAuxValid(AuxMsg auxMsg, byte index) {

        int seq = auxMsg.getSeq();
        byte src = auxMsg.getSrc();
        byte round = auxMsg.getRound();
        boolean est = auxMsg.isEst();

        if (StatusSetUtils.isRejectAuxs(seq, src, round)) {
            return false;
        }
        if (StatusSetUtils.isInAuxs(seq, src, round, index)) {
            return false;
        }
        StatusSetUtils.addAuxs(seq, src, round, est, index);

        return true;

    }

}
