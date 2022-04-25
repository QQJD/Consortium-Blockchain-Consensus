package utils;

import p2p.NetworkInfo;

import java.util.Arrays;

public class ErasureCodeUtils {

    /**
     * （N-2f，N）纠删码编码，将原内容分为N-2f块，使用纠删码编码，附加2f个冗余块，总共生成N块数据并返回
     * 因为没找到合适的工具包，目前是假编码
     * @param raw 待编码内容
     * @return 纠删码编码结果
     */
    public static byte[][] encode(byte[] raw) {
        int n = NetworkInfo.getN();
        int f = NetworkInfo.getF();
        int k = n - 2 * f;
        int blockLen = raw.length / k + 1;
        byte[][] enc = new byte[n][blockLen];
        for (int i = 0; i < n; i++) {
            int start = i % k * blockLen;
            int end = Math.min(start + blockLen, raw.length);
            enc[i] = Arrays.copyOfRange(raw, start, end);
        }
        return enc;
    }

}
