# TinyOTP

纯本地 TOTP 双因素认证管理器 · Windows Hello 唯一认证 · 无密码

## 功能

- **Windows Hello** — 指纹 / PIN / 面部识别，唯一认证方式
- **即时复制** — 主页面实时显示 TOTP 码，单击复制
- **扫码添加** — 屏幕框选二维码，自动解析 `otpauth://` URI
- **加密存储** — vault 文件 AES-256-GCM 整文件加密，DPAPI 保护密钥
- **内存安全** — secret 常驻密文，生成码逐步擦除中间变量

## 快速开始

```bash
git clone https://github.com/MC-XiaoHei/TinyOTP
cd TinyOTP
./gradlew.bat run
```

首次启动会自动引导设置 Windows Hello。

## 许可

MIT © 2026 MC-XiaoHei
