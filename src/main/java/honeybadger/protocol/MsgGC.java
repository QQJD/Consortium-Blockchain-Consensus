package honeybadger.protocol;

import honeybadger.status.ConsensusStatus;

public class MsgGC {

    /**
     * 处理REQ请求后，移除暂存在map中的REQ请求
     * 一般情况下REQ请求不会被暂存，但在收到REQ请求的序列号超前（即当前节点需要先处理之前的REQ请求）时，会被暂存
     */
    public static void afterReq() {
        ConsensusStatus.reqs.remove(ConsensusStatus.currSeq);
    }

}
