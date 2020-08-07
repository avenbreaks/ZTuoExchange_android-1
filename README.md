## 本项目为ZTuo数字资产交易平台Android端，仅供学习交流。

- ## [English](README-EN.md)
---
# 捐赠:
#### 您的捐赠是我们开源最大的动力
- BTC/USDT(比特币/USDT)：1Dwwqhw9pV9iSSQwuJc8nAygda7XfahaoW
- ETH/USDT(以太坊/USDT)：0x4f1ea0f10aa99f608f31f70b4d3119f6928693ed
- LTC(莱特币)：LXr4TMtDhCSpdAo98vg2sbvX3UXDVPQvMa

## 加入我们
    为方便大家交流和学习，请各位小伙伴加入QQ交流群：
	*区块链交易所技术知识交流群【QQ群：735446452】

## 项目基本信息

- 开发语言：Java
- 开发工具：Android studio（版本号3.1.2）
- 项目架构：MVP
- 最低支持Android版本：16


## 主要三方库说明：

1. kchartlib：K线图使用的图表库
2. lib-zxing：用于生成分享二维码
3. permission：权限请求
4. glide：图片加载
5. banner：首页轮播图
6. BaseRecyclerViewAdapterHelper：RecyclerView适配器
7. barlibrary：沉浸式状态栏
8. captchasdk：腾讯防水，用于注册和登录
9. xutils：存储聊天信息到数据库
10. PickerView：时间选择器

## 如何快速运行自己的交易所Android端？

1. 复制项目到本地：git clone https://github.com/xunibidev/ZTuoExchange_android.git
2. 项目中按此路径main>java>top>biduo>exchange>config找到AppConfig，把IP替换为自己的域名或IP,同时此IP也是socket推送的IP（目前行情、心跳、聊天用的端口都是28901，可根据自己服务端配置修改）,替换腾讯防水app_id：AUTH_APP_ID
3. 替换app_name，应用图标，启动图片等资源
4. 替换MyApplication中bugly的appkey:CrashReport.initCrashReport(getApplicationContext(), "xxx", false);
5. 在build.gradle中替换签名的配置：signingConfigs
6. 替换applicationId
7. 点击运行

## 可能遇到的问题：

1. 如需修改K线图样式或功能，可直接在Model:kchartlib 源码中修改，使用介绍详见kchartlib项目github地址：[https://github.com/tifezh/KChartView](https://github.com/tifezh/KChartView)
2. K线图目前实现了一分钟数据实时推送，在接收数据后添加到尾部（右边），并执行向左平移动画，如需实现其他时间此功能可参考一分钟实现。
3. socket入参是json转的byte[],例：`new JSONObject().put("uid","1").toString().getBytes()`
4. 测试环境暂未部署聊天模块，ChatActivity中聊天推送代码已注掉，如需使用聊天功能，恢复相关代码即可




