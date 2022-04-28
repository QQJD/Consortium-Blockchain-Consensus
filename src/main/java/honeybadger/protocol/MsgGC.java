package honeybadger.protocol;

import honeybadger.msg.AuxMsg;
import honeybadger.msg.BvalMsg;
import honeybadger.msg.EchoMsg;
import honeybadger.msg.ReadyMsg;
import honeybadger.status.ConsensusStatus;
import honeybadger.status.StatusSetUtils;
import pojo.Node;

public class MsgGC {

    /**
     * 处理REQ请求后，移除暂存在map中的REQ请求
     * 一般情况下REQ请求不会被暂存，但在收到REQ请求的序列号超前（即当前节点需要先处理之前的REQ请求）时，会被暂存
     */
    public static void afterReq() {
        ConsensusStatus.reqs.remove(ConsensusStatus.currSeq);
    }

    public static void afterEnoughEchos(EchoMsg echoMsg) {

        // TODO: 合并echo中的消息，持久化

        int seq = echoMsg.getSeq();
        String root = echoMsg.getRoot();

        // 将seq->merkle root加入rejectEchos，不再接收
        StatusSetUtils.addRejectEchos(seq, root);

    }

    public static void afterEnoughReadys(ReadyMsg readyMsg) {

        int seq = readyMsg.getSeq();
        String root = readyMsg.getRoot();

        StatusSetUtils.addRejectReadys(seq, root);

    }

    public static void afterEnoughBvals0(BvalMsg bvalMsg) {

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        StatusSetUtils.addRejectBvals0(seq, src, round);

    }

    public static void afterEnoughBvals1(BvalMsg bvalMsg) {

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        StatusSetUtils.addRejectBvals1(seq, src, round);

    }

    public static void afterEnoughAuxs(AuxMsg auxMsg) {

        int seq = auxMsg.getSeq();
        byte src = auxMsg.getSrc();
        byte round = auxMsg.getRound();

        StatusSetUtils.addRejectAuxs(seq, src, round);

    }

    public static void afterSendReady(ReadyMsg readyMsg) {

        int seq = readyMsg.getSeq();
        String root = readyMsg.getRoot();

        StatusSetUtils.addSendReadys(seq, root);

    }

    public static void afterSendBval0(BvalMsg bvalMsg) {

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        StatusSetUtils.addSendBvals0(seq, src, round);

    }

    public static void afterSendBval1(BvalMsg bvalMsg) {

        int seq = bvalMsg.getSeq();
        byte src = bvalMsg.getSrc();
        byte round = bvalMsg.getRound();

        StatusSetUtils.addSendBvals1(seq, src, round);

    }

    public static void afterSendAux(AuxMsg auxMsg) {

        int seq = auxMsg.getSeq();
        byte src = auxMsg.getSrc();
        byte round = auxMsg.getRound();

        StatusSetUtils.addSendAuxs(seq, src, round);

    }

    public static void afterFinishSeq() {

        // TODO: 完成一轮Honey Badger共识后，应将结果持久化

        int discardSeq = ConsensusStatus.currSeq - Node.getInstance().getWaterMark();

        ConsensusStatus.reqs.remove(discardSeq);
        ConsensusStatus.rejectVals.remove(discardSeq);
        ConsensusStatus.vals.remove(discardSeq);
        ConsensusStatus.root2Index.remove(discardSeq);
        ConsensusStatus.rejectEchos.remove(discardSeq);
        ConsensusStatus.echos.remove(discardSeq);
        ConsensusStatus.sendReadys.remove(discardSeq);
        ConsensusStatus.rejectReadys.remove(discardSeq);
        ConsensusStatus.readys.remove(discardSeq);
        ConsensusStatus.sendBvals0.remove(discardSeq);
        ConsensusStatus.sendBvals1.remove(discardSeq);
        ConsensusStatus.rejectBvals0.remove(discardSeq);
        ConsensusStatus.rejectBvals1.remove(discardSeq);
        ConsensusStatus.bvals0.remove(discardSeq);
        ConsensusStatus.bvals1.remove(discardSeq);
        ConsensusStatus.sendAuxs.remove(discardSeq);
        ConsensusStatus.rejectAuxs.remove(discardSeq);
        ConsensusStatus.auxs.remove(discardSeq);
        ConsensusStatus.binValues.remove(discardSeq);
        ConsensusStatus.outputs.remove(discardSeq);

        ConsensusStatus.currSeq++;

    }

}
