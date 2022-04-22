# Consortium-Blockchain-Consensus
用于实现联盟链共识算法的底层框架
## 进度
- 整合Slf4j日志框架
- 实现P2P网络
  - 基本P2P连接：基于Netty框架，节点启动时，自动连接到peers.properties文件里配置的对等节点
  - 密钥协商：使用javax.crypto、java.security密码学类库，peer间在建立连接时，通过非对称加密算法，协商生成对称加密密钥，用于后续通信加密/解密
  - 通信安全性：通过自定义消息格式及编解码器，节点通信时自动进行加密/解密，签名/验签
## TODO
- 实现Honey Badger BFT协议流程
