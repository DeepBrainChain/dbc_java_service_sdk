# dbc_java_service_sdk

运行example需要配置环境变量

运行环境jdk 11（否则会编译错误）

需要配置maven私服 必须要本地jar包
（下载页面：https://github.com/DeepBrainChain/dbc_java_service_sdk/releases， 将jar包同步到本地仓库）

转账在实例 demo 下 transfer

String api = "wss://info.dbcwallet.io";（也可以自己部署一个钱包节点）

DotAmount amount = DotAmount.fromDots(100);在这里修改需要转账的金额

balance 查询余额
