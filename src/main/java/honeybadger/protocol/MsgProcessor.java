package honeybadger.protocol;

import com.google.gson.Gson;
import honeybadger.msg.*;
import honeybadger.status.ConsensusStatus;
import honeybadger.status.StatusSetUtils;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import p2p.NetworkInfo;
import pojo.Node;
import pojo.msg.MsgType;
import pojo.msg.RawMsg;
import pojo.msg.ReqMsg;
import utils.ErasureCodeUtils;
import utils.LocalUtils;
import utils.MerkleTree;
import utils.SendUtils;

import static honeybadger.status.StatusSetUtils.isInOutputs;

@Slf4j
public class MsgProcessor {

    public static Node node = Node.getInstance();
    public static Gson gson = new Gson();

    public static void req(ReqMsg reqMsg) {

        if (!MsgValidator.isReqValid(reqMsg)) {
            return;
        }

        String req = reqMsg.getBody();

        // 为保证性能，发送val消息时，每个节点仅广播所有消息的一部分，最后取所有节点proposal的并集作为共识结果
        int seg = (int) Math.ceil(req.length() / NetworkInfo.getN());
        String proposed = req.substring(node.getIndex() * seg, node.getIndex() * seg + seg);

        // TODO：进行纠删编码，保证其他只要收到N-2f个val消息就能恢复内容
        byte[][] erasureEncoded = ErasureCodeUtils.encode(proposed.getBytes(CharsetUtil.UTF_8));

        // TODO：阈值加密，保证隐私性

        // 构造merkle树
        MerkleTree merkleTree = new MerkleTree(erasureEncoded, node.getDigestAlgorithm());

        // 生成proof
        byte[] data = erasureEncoded[node.getIndex()];
        byte[] root = merkleTree.getRoot();
        byte[][] proof = merkleTree.getProof(node.getIndex());

        // 生成VAL消息
        String dataStr = LocalUtils.bytes2Hex(data);
        String rootStr = LocalUtils.bytes2Hex(root);
        String[] proofStr = new String[proof.length];
        for (int i = 0; i < proof.length; i++) {
            proofStr[i] = LocalUtils.bytes2Hex(proof[i]);
        }
        ValMsg valMsg = new ValMsg(reqMsg.getSeq(), dataStr, rootStr, proofStr);
        String json = gson.toJson(valMsg);
        RawMsg rawMsg = new RawMsg(MsgType.VAL, json, null);

        // 广播消息（包括自己）
        SendUtils.publishToServer(rawMsg);

        // 回收ConsensusStatus中不再需要的记录
        MsgGC.afterReq();

    }

    public static void val(ValMsg valMsg, byte index) {

        if (!MsgValidator.isValValid(valMsg, index)) {
            return;
        }

        int seq = valMsg.getSeq();
        String root = valMsg.getRoot();

        // 记录每个merkle root是哪个节点提出的
        StatusSetUtils.addRoot2Index(seq, root, index);

        EchoMsg echoMsg = new EchoMsg(valMsg.getSeq(), valMsg.getData(), valMsg.getRoot(), valMsg.getProof());
        String json = gson.toJson(echoMsg);
        RawMsg rawMsg = new RawMsg(MsgType.ECHO, json, null);

        // 广播消息（包括自己）
        SendUtils.publishToServer(rawMsg);

    }

    public static void echo(EchoMsg echoMsg, byte index) {

        if (!MsgValidator.isEchoValid(echoMsg, index)) {
            return;
        }

        int seq = echoMsg.getSeq();
        String root = echoMsg.getRoot();

        // 收到N-f个ECHO消息，且READY消息还未发送，则发送READY消息
        int received = ConsensusStatus.echos.get(seq).get(root).size();
        if (received >= NetworkInfo.getN() - NetworkInfo.getF()) {
            // 广播消息
            if (!StatusSetUtils.isSendReadys(seq, root)) {
                // 记录ECHO消息已足够，多余的不再处理
                MsgGC.afterEnoughEchos(echoMsg);
                ReadyMsg readyMsg = new ReadyMsg(seq, root);
                // 记录已发送READY，防止重复发送
                MsgGC.afterSendReady(readyMsg);
                String json = gson.toJson(readyMsg);
                RawMsg rawMsg = new RawMsg(MsgType.READY, json, null);
                SendUtils.publishToServer(rawMsg);
            }
        }

    }

    public static void ready(ReadyMsg readyMsg, byte index) {

        if (!MsgValidator.isReadyValid(readyMsg, index)) {
            return;
        }

        int seq = readyMsg.getSeq();
        String root = readyMsg.getRoot();

        int received = ConsensusStatus.readys.get(seq).get(root).size();
        // 收到f+1个READY消息，且READY消息还未发送，则发送READY消息
        if (received >= NetworkInfo.getF() + 1) {
            if (!StatusSetUtils.isSendReadys(seq, root)) {
                ReadyMsg myReadyMsg = new ReadyMsg(seq, root);
                // 记录已发送READY，防止重复发送
                MsgGC.afterSendReady(myReadyMsg);
                String json = gson.toJson(myReadyMsg);
                RawMsg rawMsg = new RawMsg(MsgType.READY, json, null);
                SendUtils.publishToServer(rawMsg);
            }
            // 收到2f+1个READY消息后，发送BVAL的条件是：
            // 1、有可能已经有N-f个BA共识完成了，这样在那个地方的代码会发送est为0的BVAL消息，因此这里不能发送
            // 2、有可能因为收到f+1个BVAL消息而中继过，那么如果那个中继的BVAL的est是0，与这里要发的est为1的BVAL不重复，那么这里可以发送
            if (received >= 2 * NetworkInfo.getF() + 1) {
                byte src = ConsensusStatus.root2Index.get(seq).get(root);
                if (!StatusSetUtils.isSendBvals1(seq, src, (byte) 0)) {
                    // 记录READY消息已足够，多余的不再处理
                    MsgGC.afterEnoughReadys(readyMsg);
                    BvalMsg bvalMsg = new BvalMsg(seq, src, (byte) 0, true);
                    // 记录已发送BVAL(est=1)，防止重复发送
                    MsgGC.afterSendBval1(bvalMsg);
                    String json = gson.toJson(bvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                }
            }
        }

    }

    public static void bval(BvalMsg bvalMsg, byte index) {

        if (!MsgValidator.isBvalValid(bvalMsg, index)) {
            return;
        }

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        // 如果收到的是est=0的BVAL消息
        if (!bvalMsg.isEst()) {
            int received = ConsensusStatus.bvals0.get(seq).get(src).get(round).size();
            // 收到f+1个BVAL消息，且BVAL(est=1)消息还未发送，则发送BVAL消息
            if (received >= NetworkInfo.getF() + 1) {
                if (!StatusSetUtils.isSendBvals0(seq, src, round)) {
                    BvalMsg myBvalMsg = new BvalMsg(seq, src, round, false);
                    // 记录已发送BVAL(est=0)，防止重复发送
                    MsgGC.afterSendBval0(myBvalMsg);
                    String json = gson.toJson(myBvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                }
                // 收到2f+1个BVAL消息，且AUX消息还未发送，则发送AUX消息
                if (received >= 2 * NetworkInfo.getF() + 1) {
                    if (!StatusSetUtils.isSendAuxs(seq, src, round)) {
                        // 将当前BVAL消息的est添加到bin_values集合中
                        StatusSetUtils.addBinValues(seq, src, round, false);
                        // 记录BVAL(est=0)已足够，多余的不再处理
                        MsgGC.afterEnoughBvals0(bvalMsg);
                        AuxMsg auxMsg = new AuxMsg(seq, src, round, false);
                        // 记录已发送AUX消息，防止重复发送
                        MsgGC.afterSendAux(auxMsg);
                        String json = gson.toJson(auxMsg);
                        RawMsg rawMsg = new RawMsg(MsgType.AUX, json, null);
                        SendUtils.publishToServer(rawMsg);
                    }
                }
            }
        }
        // 如果收到的是est=1的BVAL消息，处理同上
        else {
            int received = ConsensusStatus.bvals1.get(seq).get(src).get(round).size();
            if (received >= NetworkInfo.getF() + 1) {
                if (!StatusSetUtils.isSendBvals1(seq, src, round)) {
                    BvalMsg myBvalMsg = new BvalMsg(seq, src, round, true);
                    MsgGC.afterSendBval1(myBvalMsg);
                    String json = gson.toJson(myBvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                }
                if (received >= 2 * NetworkInfo.getF() + 1) {
                    if (!StatusSetUtils.isSendAuxs(seq, src, round)) {
                        StatusSetUtils.addBinValues(seq, src, round, true);
                        MsgGC.afterEnoughBvals1(bvalMsg);
                        AuxMsg auxMsg = new AuxMsg(seq, src, round, true);
                        MsgGC.afterSendAux(auxMsg);
                        String json = gson.toJson(auxMsg);
                        RawMsg rawMsg = new RawMsg(MsgType.AUX, json, null);
                        SendUtils.publishToServer(rawMsg);
                    }
                }
            }
        }

        // 检查是否完成一轮BA共识，即收到足够的AUX消息（注意该情况可能由两种情况触发：1、收到BVAL消息导致bin_values改变，2、收到AUX消息）
        if (StatusSetUtils.isEnoughAux(seq, src, round)) {
            MsgGC.afterEnoughAuxs(new AuxMsg(seq, src, round, false));
            finishRound(seq, src, round);
        }

    }

    public static void aux(AuxMsg auxMsg, byte index) {

        if (!MsgValidator.isAuxValid(auxMsg, index)) {
            return;
        }

        int seq = auxMsg.getSeq();
        byte src = auxMsg.getSrc();
        byte round = auxMsg.getRound();

        // 检查是否完成一轮BA共识
        if (StatusSetUtils.isEnoughAux(seq, src, round)) {
            MsgGC.afterEnoughAuxs(auxMsg);
            finishRound(seq, src, round);
        }

    }

    synchronized public static void finishRound(int seq, byte src, byte round) {

        Byte binValue = ConsensusStatus.binValues.get(seq).get(src).get(round);

        // TODO: 公共随机硬币算法
        boolean coin = (round % 2) == 1;

        if (StatusSetUtils.isInOutputs(seq, src) && ConsensusStatus.outputs.get(seq).get(src) == coin) {
            return;
        }

        switch (binValue) {

            // bin_values中没有元素（正常情况下不会走到这里，因为已经被StatusSetUtils.isEnoughAux过滤掉）
            case 0:
                return;

            // bin_values中只有0
            case 1:
                // 如果公共硬币也是0，那么可以output
                if (!coin && !StatusSetUtils.isInOutputs(seq, src)) {
                    log.info(String.format("[OUTPUT]: seq=%s, src=%s, est=false", seq, src));
                    StatusSetUtils.addOutputs(seq, src, false);
                    // 如果一轮Honey Badger共识中，所有节点提议对应的BA共识都完成，则可以进入下一轮Honey Badger共识
                    if (ConsensusStatus.outputs.get(seq).size() == NetworkInfo.getN()) {
                        MsgGC.afterFinishSeq();
                        if (ConsensusStatus.reqs.containsKey(ConsensusStatus.currSeq)) {
                            req(ConsensusStatus.reqs.get(ConsensusStatus.currSeq));
                        }
                    }
                }
                // 不管公共硬币是几，都进行下一轮BA共识，est保持0不变
                if(!StatusSetUtils.isSendBvals0(seq, src, (byte) (round + 1))) {
                    BvalMsg bvalMsg = new BvalMsg(seq, src, (byte) (round + 1), false);
                    MsgGC.afterSendBval0(bvalMsg);
                    String json = gson.toJson(bvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                }
                return;

            // bin_values中只有1，类似case 1
            case 2:
                if (coin && !StatusSetUtils.isInOutputs(seq, src)) {
                    log.info(String.format("[OUTPUT]: seq=%s, src=%s, est=true", seq, src));
                    StatusSetUtils.addOutputs(seq, src, true);
                    // 与case 1不同的部分，有一个BA共识output了1，那可能使output了1的BA共识总数超过N-f
                    // 这时其他的BA共识如果还没有开始，那么设置est为0并强制开始（为了防止有f个恶意静默节点的情况）
                    if (ConsensusStatus.outputs.get(seq).size() >= NetworkInfo.getN() - NetworkInfo.getF()) {
                        for (byte i = 0; i < NetworkInfo.getN(); i++) {
                            if (!ConsensusStatus.outputs.get(seq).contains(i)
                                    && !StatusSetUtils.isSendBvals0(seq, i, (byte) 0)
                                    && !StatusSetUtils.isSendBvals1(seq, i, (byte) 0)) {
                                BvalMsg bvalMsg = new BvalMsg(seq, i, (byte) 0, false);
                                MsgGC.afterSendBval0(bvalMsg);
                                String json = gson.toJson(bvalMsg);
                                RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                                SendUtils.publishToServer(rawMsg);
                            }
                        }
                    }
                    if (ConsensusStatus.outputs.get(seq).size() == NetworkInfo.getN()) {
                        MsgGC.afterFinishSeq();
                        if (ConsensusStatus.reqs.containsKey(ConsensusStatus.currSeq)) {
                            req(ConsensusStatus.reqs.get(ConsensusStatus.currSeq));
                        }
                    }
                }
                if(!StatusSetUtils.isSendBvals1(seq, src, (byte) (round + 1))) {
                    BvalMsg bvalMsg = new BvalMsg(seq, src, (byte) (round + 1), true);
                    MsgGC.afterSendBval1(bvalMsg);
                    String json = gson.toJson(bvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                }
                return;

            // bin_values中0和1都有，那么继续下一轮BA共识，est设置为公共硬币的值
            case 3:
                if (!coin && !StatusSetUtils.isSendBvals0(seq, src, (byte) (round + 1))) {
                    BvalMsg bvalMsg = new BvalMsg(seq, src, (byte) (round + 1), false);
                    MsgGC.afterSendBval0(bvalMsg);
                    String json = gson.toJson(bvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                    return;
                }
                if (coin && !StatusSetUtils.isSendBvals1(seq, src, (byte) (round + 1))) {
                    BvalMsg bvalMsg = new BvalMsg(seq, src, (byte) (round + 1), true);
                    MsgGC.afterSendBval1(bvalMsg);
                    String json = gson.toJson(bvalMsg);
                    RawMsg rawMsg = new RawMsg(MsgType.BVAL, json, null);
                    SendUtils.publishToServer(rawMsg);
                    return;
                }

            default:
                return;

        }

    }

}
