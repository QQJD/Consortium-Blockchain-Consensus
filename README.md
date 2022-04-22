# Consortium-Blockchain-Consensus
用于实现联盟链共识算法的底层框架
## 进度
- 整合Slf4j日志框架
- 实现P2P网络
  - 完成密钥协商流程，peer间通过非对称加密协商对称加密密钥，用于后续通信
  - 完成通信时的加密/解密，签名/验签
## TODO
- 实现Honey Badger BFT协议流程
