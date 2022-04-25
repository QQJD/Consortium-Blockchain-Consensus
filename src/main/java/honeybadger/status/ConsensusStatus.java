package honeybadger.status;

import pojo.msg.ReqMsg;

import java.util.concurrent.ConcurrentHashMap;

public class ConsensusStatus {

    /**
     * 当前正在进行共识的请求序号
     * 小于该序号的请求直接丢弃；大于该序号的请求缓存到reqs中，等待之后处理
     */
    public static int currSeq = 0;
    /**
     * key：请求序号
     * value：请求消息对象
     */
    public static ConcurrentHashMap<Integer, ReqMsg> reqs = new ConcurrentHashMap<>();

}
