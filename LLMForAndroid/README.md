基于瑞芯微NPU芯片的一款android调用大模型接口,目前支持Deepseek和Qwen 文本模型，后续会新增更多的参数设置,模型文件尝试使用3576或者3588版本,经过测试2b内的模型，NPU占比在47%左右，每秒10个token,内存加载占用1.5G


<div align="center">

# LLM - FOR - Android   android调用NPU进行大模型推理应用

![Version](https://img.shields.io/badge/version-1.2.2-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%209.0%2B-green.svg)
![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)
![License](https://img.shields.io/badge/license-MIT-red.svg)

**一款功能强大、操作流畅的Android调用NPU进行大模型推理应用**

让大模型推理变得简单、直观、专业

[📥 立即下载](#-下载安装) • [📖 使用指南](#-使用指南) • [✨ 核心特性](#-核心特性) • [🎯 应用场景](#-应用场景)

</div>

---

## 🌟 项目简介

**LLMForAndroid**是一款专为Android平台瑞芯微NPU芯片开发的LLM应用，它允许用户直接使用api调用大模型或者发布为三方服务，提供给三方app进行第哦啊用，让使用更加直观高效。

### 💡 为什么选择LLMForAndroid？

- **🎯 全局可用** - 在任何应用界面上都能快速开启AI助手功能
- **⚡ 极致流畅** - 响应延迟<100ms，书写流畅不卡顿,每秒10+个token,运行1.8B大模型 NPU占比不到40%
- **🔒 隐私安全** - 完全离线运行，不上传任何数据
- **🆓 完全免费** - 无广告、无内购、透明

---

## 🎯 应用场景

### 📚 AI助手
本地化部署AI助手。无需下载应用，直接API调用大模型。


---

## ✨ 核心特性

### 🎨 专业的标注工具

#### API调用大模型推理
API方式调用大模型进行推理。

#### 提供三方app调用
定义服务提供给三方app进行调用。

---

## 🔧 技术实现

### 现代化开发框架
完全使用Kotlin语言开发，Kotlin是Google官方推荐的Android开发语言，具有简洁、安全、高效的特点，C++使用llm。

---

## 📱 使用指南

### 初次使用

#### 第一步：授予存储权限
点击"授予存储权限"按钮。在Android 13+设备上，系统会弹出"授权访问存储卡内容"对话框，选择"允许"。这个权限允许应用将读写sd卡。

---

## 🔒 隐私与安全

### 隐私承诺
LLMForAndroid是一款离线运行的应用，不收集用户数据，不上传任何信息到云端或第三方服务器。应用请求的所有权限都仅用于本地功能实现，没有任何隐私风险。

### 权限说明

**存储权限（READ_MEDIA_IMAGES / WRITE_EXTERNAL_STORAGE）**
允许应用将截图保存到系统相册。Android 13+使用READ_MEDIA_IMAGES权限，仅允许访问图片；Android 12及以下使用WRITE_EXTERNAL_STORAGE权限。应用仅会读入自己授权文件，不会读取或修改用户的其他文件。

## 🚀 下载安装

### 系统要求
- **最低版本**：Android 9.0 (API 28)
- **推荐版本**：Android 10.0 (API 29) 或更高
- **设备内存**：建议2GB以上
- **屏幕分辨率**：不限
- **存储空间**：应用大小约22MB
- **NPU平台**：瑞芯微NPU,3576,3588 等...

### 下载方式

#### 从GitHub下载（推荐）
访问项目的[Releases页面](https://github.com/zhanghuadong/LLMForAndroid/releases)，下载最新版本的APK文件。每个版本都包含详细的更新说明，建议下载标记为"Latest"的最新稳定版本。


### 安装步骤
1. 下载APK文件到Android设备
2. 打开APK文件，系统会提示"是否允许安装来自此来源的应用"
3. 选择"设置"，打开"允许安装未知应用"开关
4. 返回，再次打开APK文件
5. 点击"安装"按钮，等待安装完成
6. 点击"打开"按钮启动应用

---

## 🛣️ 未来规划

### 近期计划（3-6个月）

**新增更多大模型**
计划新增文心一言ERNIE-4.5-0.3B、腾讯混元翻译模型1.8B、以及腾讯混元0.5b模型、Paddle-OCR-VL模型。

---


## 📞 联系与支持
13544061760

### 项目主页
[GitHub仓库](https://github.com/zhanghuadong/LLMForAndroid)


### 本SDK接口版权
非本人授权,请勿商用!

---

### 商用使用请邮件联系，授权使用
zhanghuadongzi@hotmail.com

---


<div align="center">

## ⭐ 如果这个项目对你有帮助，请给个Star支持一下！

**让NPU调用大模型推理变得简单、直观、专业**

Made with ❤️ by zhanghuadong

[⬆ 回到顶部](#LLMForAndroid---android-NPU跑大模型工具)

</div>

