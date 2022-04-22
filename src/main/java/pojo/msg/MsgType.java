package pojo.msg;

public enum MsgType {

    /**
     * 密钥协商阶段client向server发送的消息类型
     */
    CONN,
    /**
     * 密钥协商阶段server收到client的CONN消息后，返回的消息类型
     */
    CONN_REPLY;

    private static MsgType[] int2EnumMap = null;

    /**
     * 枚举类本身不提供索引到对象的映射，需要自行实现；另外，对象到索引的映射可以使用枚举类自带的ordinal()函数
     * @param i 枚举对象索引
     * @return 枚举对象
     */
    public static MsgType int2Enum(int i) {
        if(int2EnumMap == null) {
            MsgType.int2EnumMap = MsgType.values();
        }
        return MsgType.int2EnumMap[i];
    }

}
