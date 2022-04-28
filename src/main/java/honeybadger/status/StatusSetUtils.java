package honeybadger.status;

import cn.hutool.core.collection.ConcurrentHashSet;
import honeybadger.msg.EchoMsg;
import p2p.NetworkInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 主要包含6类方法：
 * 1.判断是否在需要拒绝的集合中（一般是已收到2f+1个该消息，后续的不再需要）
 * 2.将消息加入需要拒绝的集合
 * 3.判断消息是否已经在消息集合中（即防止重放）
 * 4.将消息加入集合
 * 5.判断是否在已经发送的集合中（防止重复发送）
 * 6.将消息加入已经发送的集合
 */
public class StatusSetUtils {

    private static ReentrantLock[] locks = new ReentrantLock[20];
    static {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public static boolean isRejectVals(int seq) {
        if (ConsensusStatus.rejectVals.contains(seq)) {
            return true;
        }
        return false;
    }

    public static void addRejectVals(int seq) {
        ConsensusStatus.rejectVals.add(seq);
    }

    public static boolean isInVals(int seq, byte index) {
        if (ConsensusStatus.vals.containsKey(seq)
                && ConsensusStatus.vals.get(seq).contains(index)) {
            return true;
        }
        return false;
    }

    public static void addVals(int seq, byte index) {
        locks[0].lock();
        if (!ConsensusStatus.vals.containsKey(seq)) {
            ConsensusStatus.vals.put(seq, new ConcurrentHashSet<>());
        }
        locks[0].unlock();
        ConsensusStatus.vals.get(seq).add(index);
    }

    public static void addRoot2Index(int seq, String root, byte index) {
        locks[1].lock();
        if (!ConsensusStatus.root2Index.containsKey(seq)) {
            ConsensusStatus.root2Index.put(seq, new ConcurrentHashMap<>());
        }
        locks[1].unlock();
        ConsensusStatus.root2Index.get(seq).put(root, index);
    }

    public static boolean isRejectEchos(int seq, String root) {
        if (ConsensusStatus.rejectEchos.containsKey(seq)
                && ConsensusStatus.rejectEchos.get(seq).contains(root)) {
            return true;
        }
        return false;
    }

    public static void addRejectEchos(int seq, String root) {
        locks[2].lock();
        if (!ConsensusStatus.rejectEchos.containsKey(seq)) {
            ConsensusStatus.rejectEchos.put(seq, new ConcurrentHashSet<>());
        }
        locks[2].unlock();
        ConsensusStatus.rejectEchos.get(seq).add(root);
    }

    public static boolean isInEchos(int seq, String root, byte index) {
        if (ConsensusStatus.echos.containsKey(seq)
                && ConsensusStatus.echos.get(seq).containsKey(root)
                && ConsensusStatus.echos.get(seq).get(root).containsKey(index)) {
            return true;
        }
        return false;
    }

    public static void addEchos(int seq, String root, byte index, EchoMsg echoMsg) {
        locks[3].lock();
        if (!ConsensusStatus.echos.containsKey(seq)) {
            ConsensusStatus.echos.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.echos.get(seq).containsKey(root)) {
            ConsensusStatus.echos.get(seq).put(root, new ConcurrentHashMap<>());
        }
        locks[3].unlock();
        ConsensusStatus.echos.get(seq).get(root).put(index, echoMsg);
    }

    public static boolean isSendReadys(int seq, String root) {
        if (ConsensusStatus.sendReadys.containsKey(seq)
                && ConsensusStatus.sendReadys.get(seq).contains(root)) {
            return true;
        }
        return false;
    }

    public static void addSendReadys(int seq, String root) {
        locks[4].lock();
        if (!ConsensusStatus.sendReadys.containsKey(seq)) {
            ConsensusStatus.sendReadys.put(seq, new ConcurrentHashSet<>());
        }
        locks[4].unlock();
        ConsensusStatus.sendReadys.get(seq).add(root);
    }

    public static boolean isRejectReadys(int seq, String root) {
        if (ConsensusStatus.rejectReadys.containsKey(seq)
                && ConsensusStatus.rejectReadys.get(seq).contains(root)) {
            return true;
        }
        return false;
    }

    public static void addRejectReadys(int seq, String root) {
        locks[5].lock();
        if (!ConsensusStatus.rejectReadys.containsKey(seq)) {
            ConsensusStatus.rejectReadys.put(seq, new ConcurrentHashSet<>());
        }
        locks[5].unlock();
        ConsensusStatus.rejectReadys.get(seq).add(root);
    }

    public static boolean isInReadys(int seq, String root, byte index) {
        if (ConsensusStatus.readys.containsKey(seq)
                && ConsensusStatus.readys.get(seq).containsKey(root)
                && ConsensusStatus.readys.get(seq).get(root).contains(index)) {
            return true;
        }
        return false;
    }

    public static void addReadys(int seq, String root, byte index) {
        locks[6].lock();
        if (!ConsensusStatus.readys.containsKey(seq)) {
            ConsensusStatus.readys.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.readys.get(seq).containsKey(root)) {
            ConsensusStatus.readys.get(seq).put(root, new ConcurrentHashSet<>());
        }
        locks[6].unlock();
        ConsensusStatus.readys.get(seq).get(root).add(index);
    }

    public static boolean isSendBvals0(int seq, byte src, byte round) {
        if (ConsensusStatus.sendBvals0.containsKey(seq)
                && ConsensusStatus.sendBvals0.get(seq).containsKey(src)
                && ConsensusStatus.sendBvals0.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addSendBvals0(int seq, byte src, byte round) {
        locks[7].lock();
        if (!ConsensusStatus.sendBvals0.containsKey(seq)) {
            ConsensusStatus.sendBvals0.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.sendBvals0.get(seq).containsKey(src)) {
            ConsensusStatus.sendBvals0.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[7].unlock();
        ConsensusStatus.sendBvals0.get(seq).get(src).add(round);
    }

    public static boolean isRejectBvals0(int seq, byte src, byte round) {
        if (ConsensusStatus.rejectBvals0.containsKey(seq)
                && ConsensusStatus.rejectBvals0.get(seq).containsKey(src)
                && ConsensusStatus.rejectBvals0.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addRejectBvals0(int seq, byte src, byte round) {
        locks[8].lock();
        if (!ConsensusStatus.rejectBvals0.containsKey(seq)) {
            ConsensusStatus.rejectBvals0.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.rejectBvals0.get(seq).containsKey(src)) {
            ConsensusStatus.rejectBvals0.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[8].unlock();
        ConsensusStatus.rejectBvals0.get(seq).get(src).add(round);
    }

    public static boolean isInBvals0(int seq, byte src, byte round, byte index) {
        if (ConsensusStatus.bvals0.containsKey(seq)
                && ConsensusStatus.bvals0.get(seq).containsKey(src)
                && ConsensusStatus.bvals0.get(seq).get(src).containsKey(round)
                && ConsensusStatus.bvals0.get(seq).get(src).get(round).contains(index)) {
            return true;
        }
        return false;
    }

    public static void addBvals0(int seq, byte src, byte round, byte index) {
        locks[9].lock();
        if (!ConsensusStatus.bvals0.containsKey(seq)) {
            ConsensusStatus.bvals0.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.bvals0.get(seq).containsKey(src)) {
            ConsensusStatus.bvals0.get(seq).put(src, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.bvals0.get(seq).get(src).containsKey(round)) {
            ConsensusStatus.bvals0.get(seq).get(src).put(round, new ConcurrentHashSet<>());
        }
        locks[9].unlock();
        ConsensusStatus.bvals0.get(seq).get(src).get(round).add(index);
    }

    public static boolean isSendBvals1(int seq, byte src, byte round) {
        if (ConsensusStatus.sendBvals1.containsKey(seq)
                && ConsensusStatus.sendBvals1.get(seq).containsKey(src)
                && ConsensusStatus.sendBvals1.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addSendBvals1(int seq, byte src, byte round) {
        locks[10].lock();
        if (!ConsensusStatus.sendBvals1.containsKey(seq)) {
            ConsensusStatus.sendBvals1.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.sendBvals1.get(seq).containsKey(src)) {
            ConsensusStatus.sendBvals1.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[10].unlock();
        ConsensusStatus.sendBvals1.get(seq).get(src).add(round);
    }

    public static boolean isRejectBvals1(int seq, byte src, byte round) {
        if (ConsensusStatus.rejectBvals1.containsKey(seq)
                && ConsensusStatus.rejectBvals1.get(seq).containsKey(src)
                && ConsensusStatus.rejectBvals1.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addRejectBvals1(int seq, byte src, byte round) {
        locks[11].lock();
        if (!ConsensusStatus.rejectBvals1.containsKey(seq)) {
            ConsensusStatus.rejectBvals1.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.rejectBvals1.get(seq).containsKey(src)) {
            ConsensusStatus.rejectBvals1.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[11].unlock();
        ConsensusStatus.rejectBvals1.get(seq).get(src).add(round);
    }

    public static boolean isInBvals1(int seq, byte src, byte round, byte index) {
        if (ConsensusStatus.bvals1.containsKey(seq)
                && ConsensusStatus.bvals1.get(seq).containsKey(src)
                && ConsensusStatus.bvals1.get(seq).get(src).containsKey(round)
                && ConsensusStatus.bvals1.get(seq).get(src).get(round).contains(index)) {
            return true;
        }
        return false;
    }

    public static void addBvals1(int seq, byte src, byte round, byte index) {
        locks[12].lock();
        if (!ConsensusStatus.bvals1.containsKey(seq)) {
            ConsensusStatus.bvals1.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.bvals1.get(seq).containsKey(src)) {
            ConsensusStatus.bvals1.get(seq).put(src, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.bvals1.get(seq).get(src).containsKey(round)) {
            ConsensusStatus.bvals1.get(seq).get(src).put(round, new ConcurrentHashSet<>());
        }
        locks[12].unlock();
        ConsensusStatus.bvals1.get(seq).get(src).get(round).add(index);
    }

    public static boolean isSendAuxs(int seq, byte src, byte round) {
        if (ConsensusStatus.sendAuxs.containsKey(seq)
                && ConsensusStatus.sendAuxs.get(seq).containsKey(src)
                && ConsensusStatus.sendAuxs.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addSendAuxs(int seq, byte src, byte round) {
        locks[13].lock();
        if (!ConsensusStatus.sendAuxs.containsKey(seq)) {
            ConsensusStatus.sendAuxs.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.sendAuxs.get(seq).containsKey(src)) {
            ConsensusStatus.sendAuxs.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[13].unlock();
        ConsensusStatus.sendAuxs.get(seq).get(src).add(round);
    }

    public static boolean isRejectAuxs(int seq, byte src, byte round) {
        if (ConsensusStatus.rejectAuxs.containsKey(seq)
                && ConsensusStatus.rejectAuxs.get(seq).containsKey(src)
                && ConsensusStatus.rejectAuxs.get(seq).get(src).contains(round)) {
            return true;
        }
        return false;
    }

    public static void addRejectAuxs(int seq, byte src, byte round) {
        locks[14].lock();
        if (!ConsensusStatus.rejectAuxs.containsKey(seq)) {
            ConsensusStatus.rejectAuxs.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.rejectAuxs.get(seq).containsKey(src)) {
            ConsensusStatus.rejectAuxs.get(seq).put(src, new ConcurrentHashSet<>());
        }
        locks[14].unlock();
        ConsensusStatus.rejectAuxs.get(seq).get(src).add(round);
    }

    public static boolean isInAuxs(int seq, byte src, byte round, byte index) {
        if (ConsensusStatus.auxs.containsKey(seq)
                && ConsensusStatus.auxs.get(seq).containsKey(src)
                && ConsensusStatus.auxs.get(seq).get(src).containsKey(round)
                && (ConsensusStatus.auxs.get(seq).get(src).get(round).containsKey(true)
                && ConsensusStatus.auxs.get(seq).get(src).get(round).get(true).contains(index))
                || (ConsensusStatus.auxs.get(seq).get(src).get(round).containsKey(false)
                && ConsensusStatus.auxs.get(seq).get(src).get(round).get(false).contains(index))) {
            return true;
        }
        return false;
    }

    public static void addAuxs(int seq, byte src, byte round, boolean est, byte index) {
        locks[15].lock();
        if (!ConsensusStatus.auxs.containsKey(seq)) {
            ConsensusStatus.auxs.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.auxs.get(seq).containsKey(src)) {
            ConsensusStatus.auxs.get(seq).put(src, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.auxs.get(seq).get(src).containsKey(round)) {
            ConsensusStatus.auxs.get(seq).get(src).put(round, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.auxs.get(seq).get(src).get(round).containsKey(est)) {
            ConsensusStatus.auxs.get(seq).get(src).get(round).put(est, new ConcurrentHashSet<>());
        }
        locks[15].unlock();
        ConsensusStatus.auxs.get(seq).get(src).get(round).get(est).add(index);
    }

    public static boolean isInBinValues(int seq, byte src, byte round) {
        if (ConsensusStatus.binValues.containsKey(seq)
                && ConsensusStatus.binValues.get(seq).containsKey(src)
                && ConsensusStatus.binValues.get(seq).get(src).containsKey(round)) {
            return true;
        }
        return false;
    }

    public static void addBinValues(int seq, byte src, byte round, boolean est) {
        locks[16].lock();
        if (!ConsensusStatus.binValues.containsKey(seq)) {
            ConsensusStatus.binValues.put(seq, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.binValues.get(seq).containsKey(src)) {
            ConsensusStatus.binValues.get(seq).put(src, new ConcurrentHashMap<>());
        }
        if (!ConsensusStatus.binValues.get(seq).get(src).containsKey(round)) {
            ConsensusStatus.binValues.get(seq).get(src).put(round, (byte) 0);
        }
        locks[16].unlock();
        byte b = ConsensusStatus.binValues.get(seq).get(src).get(round);
        if (!est) {
            ConsensusStatus.binValues.get(seq).get(src).put(round, (byte) (b | 1));
        } else {
            ConsensusStatus.binValues.get(seq).get(src).put(round, (byte) (b | 2));
        }
    }

    public static boolean isEnoughAux(int seq, byte src, byte round) {
        if (isInBinValues(seq, src, round)) {
            Byte binValue = ConsensusStatus.binValues.get(seq).get(src).get(round);
            int received = 0;
            switch (binValue) {
                case 0:
                    break;
                case 1:
                    received = ConsensusStatus.auxs.get(seq).get(src).get(round).get(false).size();
                    break;
                case 2:
                    received = ConsensusStatus.auxs.get(seq).get(src).get(round).get(true).size();
                    break;
                case 3:
                    received = ConsensusStatus.auxs.get(seq).get(src).get(round).get(false).size()
                            + ConsensusStatus.auxs.get(seq).get(src).get(round).get(true).size();
                    break;
            }
            if (received >= NetworkInfo.getN() - NetworkInfo.getF()) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static void addOutputs(int seq, byte src) {
        locks[17].lock();
        if (!ConsensusStatus.outputs.containsKey(seq)) {
            ConsensusStatus.outputs.put(seq, new ConcurrentHashSet<>());
        }
        locks[17].unlock();
        ConsensusStatus.outputs.get(seq).add(src);
    }

}
