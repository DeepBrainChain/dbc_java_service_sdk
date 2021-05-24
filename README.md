# dbc_java_service_sdk

运行example需要配置环境变量

运行环境jdk 11（否则会编译错误）

需要配置maven私服 必须要本地jar包

转账在实例 demo 下 transfer

String api = "wss://innertest.dbcwallet.io";
链上地址 可以使用 127.0.0.1：9944 测试

DotAmount amount = DotAmount.fromDots(
        100
);
在这里修改需要转账的金额

balance 查询余额
