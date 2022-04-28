package honeybadger.status;

import cn.hutool.core.collection.ConcurrentHashSet;
import honeybadger.msg.EchoMsg;
import pojo.msg.ReqMsg;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ConsensusStatus {

    /**
     * 当前正在进行共识的请求序号
     * 小于该序号的请求直接丢弃；大于该序号的请求缓存到reqs中，等待之后处理
     */
    public static int currSeq = 0;

    /**
     * 创建时机（添加某个key的时机）：第一次收到序号为seq的REQ请求时，若大于currSeq，则将seq作为key放入
     * 回收时机（删除某个key的时机）：处理完某个REQ请求时，将其序号从key中移除
     * key：请求序号（共识轮次）
     * value：请求消息对象
     */
    public static ConcurrentHashMap<Integer, ReqMsg> reqs = new ConcurrentHashMap<>();

    /**
     * 创建时机：Honey Badger共识的BA阶段，当有N-f个节点的proposal被共识为1（也就是N-f个节点的proposal完成了共识）时
     * key：请求序号，表示不再接收key对应的VAL消息
     */
    public static ConcurrentHashSet<Integer> rejectVals = new ConcurrentHashSet<>();

    /**
     * 创建时机：第一次收到时
     * 回收时机：Honey Badger共识的BA阶段，当有N-f个节点的proposal被共识为1时（也就是rejectVals的创建时机）
     * key：请求序号
     * value：节点集合，表示已经收到集合中节点发送的key序号下的VAL消息，用来防止重放攻击
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashSet<Byte>> vals = new ConcurrentHashMap<>();

    /**
     * 需要记录某个merkle root是哪个节点提出的，否则BA阶段消息中需要merkle root，而不是节点索引（这样可以降低通信开销）
     * key：请求序号
     * value：merkle root与节点索引的对应关系
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, Byte>> root2Index = new ConcurrentHashMap<>();

    /**
     * 创建时机：某个请求序号->merkle root的set包含N-f个以上节点
     * key：请求序号
     * value：merkle root集合，表示不再接收key->value对应的ECHO消息
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashSet<String>> rejectEchos = new ConcurrentHashMap<>();

    /**
     * 创建时机：第一次收到时（包括第一次收到请求序号，和第一次收到请求序号->merkle root）
     * key：请求序号
     * value：merkle root->所有发送了key序号下该merkle root对应ECHO消息的节点集合
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<Byte, EchoMsg>>> echos = new ConcurrentHashMap<>();

    /**
     * 自己已发送的READY消息集合，表示自己已经发送过key序号下merkle root对应的READY消息
     * 因为触发发送READY消息可能有多种情况（收到f+1个READY或收到2f+1个ECHO），所以需要（尽量）避免重复发送
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashSet<String>> sendReadys = new ConcurrentHashMap<>();

    /**
     * 类似echo
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashSet<String>> rejectReadys = new ConcurrentHashMap<>();

    /**
     * 类似echo
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashSet<Byte>>> readys = new ConcurrentHashMap<>();

    /**
     * （尽量）避免重复发送BVAL
     * 由于Honey Badger协议中允许est为0和1的BVAL消息都发送（不是只能发一个），因此用两个集合来记录
     * key：请求序号
     * value：要共识的proposal对应的节点索引->发送过Bval的轮次集合
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> sendBvals0 = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> sendBvals1 = new ConcurrentHashMap<>();

    /**
     * 类似echo，由于BA共识可能多轮因此数据结构多嵌套了一层
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> rejectBvals0 = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> rejectBvals1 = new ConcurrentHashMap<>();

    /**
     * 类似echo
     * seq->src->round->index集合
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>>> bvals0 = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>>> bvals1 = new ConcurrentHashMap<>();

    /**
     * 类似bval，由于AUX消息每轮只允许发送est=0/1其中一个值，不需要2个集合
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> sendAuxs = new ConcurrentHashMap<>();

    /**
     * 类似bval
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashSet<Byte>>> rejectAuxs = new ConcurrentHashMap<>();

    /**
     * 类似bval，由于一个集合中同时记录了0/1的情况，因此多嵌套了一层
     * seq->src->round->est->index集合
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashMap<Byte, ConcurrentHashMap<Boolean, ConcurrentHashSet<Byte>>>>> auxs = new ConcurrentHashMap<>();

    /**
     * 对某个src在某轮BA共识中的bin_values（也就是收到了2f+1个BVAL消息的est集合）
     * seq->src->round->bin_values
     * bin_values=0：空集；1：只包含0；2：只包含1；3：包含0和1
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, ConcurrentHashMap<Byte, Byte>>> binValues = new ConcurrentHashMap<>();

    /**
     * key：请求序号
     * value：该请求序号下哪些节点的提议（merkle root）已经完成BA共识
     */
    public static ConcurrentHashMap<Integer, ConcurrentHashSet<Byte>> outputs = new ConcurrentHashMap<>();

}
