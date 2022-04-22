# Consortium-Blockchain-Consensus
用于实现联盟链共识算法的底层框架
## 运行方式
运行多个Main.java即可生成P2P网络。配置文件中node.properties表示当前节点信息，peers.properties表示网络中所有结点的信息。运行多个Main.java时，每次需修改node.properties，而peers.properties不需要修改。若要扩展代码，参考以下方式：
- 日志：类上注解@Slf4j，类内使用log.info()等函数即可。
- 自定义消息：自定义消息结构类Msg，发送时使用Gson进行json化，并生成new RawMsg(msgType, json, null)，通过NetworkInfo中的serverPeers/clientPeers找到接收方的channel，调用channel.writeAndFlush(rawMsg)发送消息，接收消息类似。框架会自动完成加密/解密，签名/验签。
## 架构
![联盟链共识框架](https://user-images.githubusercontent.com/82380622/164715109-8350f0aa-d97c-4fc2-b06a-f1c8afe350e9.png)
## 流程
![联盟链共识框架流程](https://user-images.githubusercontent.com/82380622/164716735-e0c4fabf-4e28-4ce0-81b9-b362510d7dfd.png)
## 进度
- 整合Slf4j日志框架
- 实现P2P网络
  - 基本P2P连接：基于Netty框架，节点启动时，自动连接到peers.properties文件里配置的对等节点。
  - 密钥协商：使用javax.crypto、java.security密码学类库，peer间在建立连接时，通过非对称加密算法，协商生成对称加密密钥，用于后续通信加密/解密。
  - 通信安全性：通过自定义消息格式及编解码器，节点通信时自动进行加密/解密，签名/验签。
## TODO
- 实现Honey Badger BFT协议流程
