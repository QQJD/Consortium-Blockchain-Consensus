package utils;

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

}
