package honeybadger.protocol;

import honeybadger.status.ConsensusStatus;
import pojo.msg.ReqMsg;

public class MsgValidator {

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

}
