# Edge Client

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B47A?style=flat-square)
![Fabric](https://img.shields.io/badge/Fabric-0.19.3-DBD0B4?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-E76F00?style=flat-square&logo=openjdk&logoColor=white)
![Environment](https://img.shields.io/badge/Environment-Client--only-5B8DEF?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)
![Version](https://img.shields.io/badge/Version-1.2.0-8A2BE2?style=flat-square)

Edge Client 是面向 Minecraft 26.2 的纯客户端 Fabric 模组基础，专注于模块化功能、声明式设置与可复用的 Click GUI API。

## 设计方向

- 模块只声明业务逻辑和设置，不重复编写 GUI 绘制与输入处理。
- GUI、配置、模块和 Minecraft 适配层保持清晰的依赖边界。
- 为客户端扩展提供稳定、可组合的内部 API。

## 技术栈

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.155.2+26.2 |

## 许可证

本项目采用 [MIT](LICENSE)。
