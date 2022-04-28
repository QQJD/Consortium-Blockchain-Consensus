package utils;

import java.util.Map;
import java.util.Set;

public class LocalUtils {

    /**
     * 将String格式的ip转换为long格式
     * @param ipStr XXX.XXX.XXX.XXX格式的ip字符串
     * @return 以long表示的ip
     */
    public static long ipStr2Long(String ipStr) {
        String[] ip = ipStr.split("\\.");
        long ret = 0;
        for (int i = 0; i < 4; i++) {
            ret += Long.parseLong(ip[i]) << (3 - i) * 8;
        }
        return ret;
    }

    /**
     * 将long格式的ip转换为String格式
     * @param ip 以long表示的ip
     * @return XXX.XXX.XXX.XXX格式的ip字符串
     */
    public static String long2IPStr(long ip) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(ip >> (3 - i) * 8 & 0xFF);
            sb.append('.');
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 将1个字节转换为2个16进制字符，例如(byte)43->00100011->0010|1011->(hex)2B
     * @param b 字节
     * @return 1个字节对应的2个16进制字符
     */
    public static char[] byte2Hex(byte b) {
        int n = b;
        if (n < 0) n += 256;
        char h = (char) (n >> 4 <= 9 ? (n >> 4) + 48 : (n >> 4) + 55);
        char l = (char) ((n & 0x0F) <= 9 ? (n & 0x0F) + 48 : (n & 0x0F) + 55);
        return new char[]{h, l};
    }

    /**
     * 将字节数组转换为16进制字符串
     * @param b 字节数组
     * @return 16进制字符串
     */
    public static String bytes2Hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(byte2Hex(b[i]));
        }
        return sb.toString();
    }

    /**
     * 将2个16进制字符转换为1个字节，例如(hex)2B->0010|1011->00101011->(byte)43
     * @param c 1个字节对应的2个16进制字符
     * @return 字节
     */
    public static byte hex2Byte(char[] c) {
        int h = c[0] <= 57 ? c[0] - 48 : c[0] - 55;
        int l = c[1] <= 57 ? c[1] - 48 : c[1] - 55;
        int n = (h << 4) + l;
        if (n >= 128) n -= 256;
        return (byte) n;
    }

    /**
     * 将16进制字符串转换为字节数组
     * @param s 16进制字符串
     * @return 字节数组
     */
    public static byte[] hex2Bytes(String s) {
        byte[] b = new byte[s.length() >> 1];
        for (int i = 0; i < s.length(); i += 2) {
            b[i >> 1] = hex2Byte(new char[]{s.charAt(i), s.charAt(i + 1)});
        }
        return b;
    }

}
