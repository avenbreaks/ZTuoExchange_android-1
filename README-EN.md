## This project is the Android end of the ZTuo digital asset trading platform

This source code is limited to exchange and study, where it involves legal issues and I have nothing to do with it


- ## [简体中文](README.md)
---
# Donate:
#### Your donation is our biggest motivation for open source
- BTC/USDT (Bitcoin/USDT): 1Dwwqhw9pV9iSSQwuJc8nAygda7XfahaoW
- ETH/USDT (Ethereum/USDT): 0x4f1ea0f10aa99f608f31f70b4d3119f6928693ed
- LTC (Litecoin): LXr4TMtDhCSpdAo98vg2sbvX3UXDVPQvMa

## Join us
    In order to facilitate everyone to communicate and learn, please join the QQ exchange group:
    *Blockchain exchange technical knowledge exchange group [QQ group: 735446452]

## Basic project information

-Development language: Java
-Development tool: Android studio (version number 3.1.2)
-Project structure: MVP
-Minimum supported Android version: 16


## The main third party library description:

1. kchartlib: chart library used by candlestick charts
2. lib-zxing: used to generate and share QR codes
3. permission: permission request
4. glide: image loading
5. Banner: Homepage carousel picture
6. BaseRecyclerViewAdapterHelper: RecyclerView adapter
7. barlibrary: immersive status bar
8. captchasdk: Tencent waterproof, used for registration and login
9. xutils: store chat information to the database
10. PickerView: Time Picker

## How to quickly run your own exchange Android terminal?

1. Copy the project to the local: git clone https://github.com/xunibidev/ZTuoExchange_android.git
2. In the project, click this path main>java>top>biduo>exchange>config to find AppConfig, and replace the IP with your own domain name or IP. At the same time, this IP is also the IP pushed by the socket (the current market, heartbeat, and chat ports are all It is 28901, can be modified according to your own server configuration), replace Tencent waterproof app_id: AUTH_APP_ID
3. Replace app_name, application icon, start image and other resources
4. Replace bugly appkey in MyApplication: CrashReport.initCrashReport(getApplicationContext(), "xxx", false);
5. Replace the signed configuration in build.gradle: signingConfigs
6. Replace applicationId
7. Click Run

## Possible problems:

1. If you need to modify the style or function of the K-line chart, you can directly modify it in the Model:kchartlib source code. Please refer to the github address of the kchartlib project for details: [https://github.com/tifezh/KChartView](https://github .com/tifezh/KChartView)
2. The K-line chart currently realizes real-time push of data for one minute. After receiving the data, add it to the tail (on the right) and execute the panning to the left. If you need to realize this function at other times, please refer to the one-minute implementation.
3. Socket input parameter is byte[] transferred from json, for example: `new JSONObject().put("uid","1").toString().getBytes()`
4. The chat module has not been deployed in the test environment. The chat push code in ChatActivity has been annotated. If you need to use the chat function, you can restore the relevant code.
