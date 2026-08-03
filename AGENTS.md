# Edge Client 工程规则

## 项目目标

本项目是面向 Minecraft 26.2 的纯客户端 Fabric 模组，目标是提供一个可扩展的辅助客户端基础，以及一套项目内部可复用的 GUI API。

核心体验：

- 新功能模块只声明功能逻辑和设置，不重复实现滑块、开关、下拉框等 GUI。
- Click GUI 根据模块和设置元数据自动生成界面。
- 自定义页面仍可直接组合本地 GUI 组件。
- 模块、设置、GUI、配置和 Minecraft 适配层之间保持清晰依赖方向。

当前仓库已实现模块、基础设置、Click GUI、动画、统一输入路由、扩展入口、JSON 配置持久化、消息提示框 API、通用 HUD 定位编辑 API、ClickGUI 外观模块、Fullbright、Better Health Bar 和 Auto Web。Friend 尚未接入；实现新功能前以真实源码和 `docs/specs/` 为准，不得把规划中的类型当作已经存在。

Click GUI 的规范视觉参考是根目录 `gui-example.png`，详细的信息结构、组件 API、主题令牌和验收条件见 `docs/specs/click-gui-api.md`。实现 GUI 前必须读取该规格；图片只作为设计参考，不能直接渲染成背景。

## 当前技术栈

- Minecraft `26.2`
- Java `25`
- Fabric Loader `0.19.3`
- Fabric API `0.155.2+26.2`
- Fabric Loom `1.17-SNAPSHOT`
- 包根：`io.qzz.iie`
- 模组 ID：`client`
- 运行环境：仅客户端

版本以 `gradle.properties`、`build.gradle` 和 `fabric.mod.json` 为准。文档与配置不一致时，先报告冲突，不要自行猜测。

## 常用命令

Windows：

```powershell
.\gradlew.bat runClient
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat build
.\gradlew.bat runDatagen
```

Unix：

```bash
./gradlew runClient
./gradlew test
./gradlew check
./gradlew build
./gradlew runDatagen
```

执行 Gradle 前确认 `JAVA_HOME` 指向可用的 JDK 25。不要通过降低 `build.gradle` 的 Java 版本掩盖本机 JDK 配置问题。

## 架构原则

1. `Client` 只是组合根，不承载业务逻辑。
2. 模块拥有行为，设置拥有状态，GUI 只负责展示与交互。
3. 模块不得依赖 `ui` 包、`Screen`、渲染上下文或具体控件。
4. GUI 读取并修改同一个 `Setting<T>`，不得维护第二份设置值。
5. 优先使用 Fabric 事件；只有 Fabric 没有合适钩子时才使用 Mixin。
6. Minecraft/Fabric 类型限制在入口、事件、屏幕和适配层，不向纯领域逻辑扩散。
7. API 从最小可用接口开始；没有第二个真实用例时，不提前设计插件系统或通用框架。
8. 所有游戏状态、输入和 GUI 更新默认发生在客户端线程。

依赖方向：

```text
bootstrap -> module -> setting
          -> config -> module + setting
          -> ui     -> module + setting
          -> minecraft adapters

module  -X-> ui
setting -X-> ui
```

`bootstrap` 可以组装所有层；低层包不得通过全局服务定位器反向获取高层对象。

## 建议包结构

```text
src/main/java/io/qzz/iie/
├── Client.java                  # Fabric 客户端入口；只负责启动
├── bootstrap/
│   └── ClientRuntime.java       # 服务组装、启动和关闭顺序
├── module/
│   ├── Module.java
│   ├── ModuleId.java
│   ├── ModuleCategory.java
│   ├── ModuleManager.java
│   └── impl/                    # 具体功能模块，按领域再分包
├── setting/
│   ├── Setting.java
│   ├── SettingCollection.java
│   ├── BooleanSetting.java
│   ├── DoubleSetting.java
│   ├── ChoiceOption.java
│   ├── ChoiceSetting.java
│   ├── ColorSetting.java
│   └── KeybindSetting.java
├── ui/
│   ├── screen/                  # ClickGuiScreen 等 Screen 适配器
│   ├── clickgui/                # 两栏壳层、控制器、页面和视图模型
│   ├── component/               # 通用组件协议与容器
│   ├── component/control/       # Slider、Toggle、Dropdown 等
│   ├── binding/                 # Setting/Module 到控件的显式值绑定
│   ├── factory/                 # Setting -> GUI 组件映射
│   ├── icon/                    # 稳定图标 ID 与纹理图集
│   ├── input/                   # 焦点、捕获、事件分发
│   ├── layout/                  # Rect、Size、约束与布局
│   ├── render/                  # GUI 绘制原语和裁剪
│   └── theme/                   # 颜色、间距、字体、动画令牌
├── config/                      # DTO、编解码、迁移和持久化
├── event/                       # 仅项目确有内部事件需求时创建
└── mixin/                       # 最小、可审计的 Minecraft 注入
```

测试放在与主包结构对应的 `src/test/java/io/qzz/iie/...`。资源、语言文件和纹理放在 `src/main/resources/assets/client/...`。

不要为了匹配此树一次性创建空包或占位类；每个目录应随首个真实类型出现。

## 启动与生命周期

`Client.onInitializeClient()` 保持简短，按以下职责调用 `ClientRuntime`：

1. 创建核心服务。
2. 注册内置模块及其设置。
3. 完成注册后加载配置。
4. 注册按键、Fabric 客户端事件和 GUI 打开入口。
5. 在可用的客户端关闭生命周期中刷新待保存配置。

不要在静态初始化器中访问 `Minecraft` 实例、读取配置或注册事件。不要使用可变的 `Client.INSTANCE` 作为全局服务定位器；显式构造依赖，并从组合根传入。

当前 `ExampleMixin` 注入了 `MinecraftServer`，与纯客户端目标不一致。开始实现架构时应删除模板 Mixin，并同步从 `client.mixins.json` 移除；不要围绕它建立任何功能。

## 模块 API

每个模块需要稳定 ID、翻译键、启用状态和设置集合。显示名称不是持久化 ID。

分类不在模块中显式声明：`Module` 基类从模块类包路径
`module.impl.<category>.<...>` 自动派生分类 id（`module.impl` 之后的第一段），
翻译键约定为 `client.category.<id>`，侧栏按分类 id 字母序排序。模块必须放在
某个分类子包下，不能直接位于 `module.impl`。

当前生命周期由 `ModuleManager` 统一驱动：

```java
moduleManager.setEnabled(module.id(), true);
moduleManager.setEnabled(module.id(), false);
```

约束：

- `enable()` 和 `disable()` 必须幂等。
- 生命周期异常不得让模块停留在“状态显示已启用、监听器却未注册”的半状态。
- `ModuleManager` 只为已启用模块调用 `onClientTick()`；普通模块不得各自重复注册 Fabric 客户端 Tick。
- 模块只订阅自己需要的事件，并在禁用时对称解除订阅或由带生命周期的订阅容器统一清理。
- 每帧逻辑不得分配大量临时对象、执行阻塞 I/O 或保存配置。
- `ModuleManager` 保持确定性注册顺序，并拒绝重复模块 ID。
- 不用类名、显示文本或列表下标作为配置键。

模块作者的正常用法是声明设置，而不是创建 GUI：

```java
public final class ExampleModule extends Module {
    private final DoubleSetting opacity = setting(
        new DoubleSetting(
            "opacity",
            "client.setting.example.opacity",
            0.80,
            0.10,
            1.00,
            0.05
        )
    );

    @Override
    protected void onEnable() {
        // 功能逻辑读取 opacity.value()。
    }

    @Override
    protected void onClientTick() {
        // 仅在模块启用时执行持续逻辑。
    }
}
```

除非正在实现自定义 UI 页面，模块类中不应出现 `SliderComponent`、颜色、坐标或渲染调用。

## 设置 API

`Setting<T>` 是设置值的唯一事实来源，至少定义：

- 在所属模块内稳定且唯一的 `id`
- 翻译键与可选描述翻译键
- 默认值和当前值
- 可见性条件
- 验证或规范化规则
- 值变更通知

设置类型的职责：

- `BooleanSetting`：开关值。
- `DoubleSetting`：`min`、`max`、`step` 和显示格式；写入时统一夹取并对齐步长。
- `ChoiceSetting<T>`：用稳定选项 ID、翻译键和值生成抽屉式单选框。
- `ColorSetting`：明确包含或不包含 alpha，不用模糊的裸 `int` API。
- `KeybindSetting`：保存输入语义，不在设置类中直接轮询键盘。

规则：

- 任何来源的写入都走同一验证路径，包括 GUI、配置加载和命令。
- 变更事件只在规范化后的值真正变化时触发。
- 可见性只影响 UI，不删除值，也不改变持久化行为。
- 设置回调不得反向操作具体 GUI 组件。
- 浮点设置比较、步长对齐和序列化必须有单元测试。
- 配置键使用 `<module-id>.<setting-id>` 组合，重命名时提供显式迁移。

## GUI API：三层设计

### 1. 面向模块作者的声明式 API

这是首选入口。Click GUI 枚举模块的设置，通过 `SettingComponentFactory` 自动生成控件：

```java
UiComponent control = settingComponents.create(setting);
```

默认映射：

```text
BooleanSetting -> ToggleComponent
DoubleSetting  -> SliderComponent
ChoiceSetting  -> ChoiceControl
ColorSetting   -> ColorPickerComponent
KeybindSetting -> KeybindComponent
HudPositionSetting -> HudPositionControl
```

### HUD 定位编辑 API

需要用户拖动位置的 HUD 元素必须声明 `HudPositionSetting`，不能在模块中创建专用 Screen 或处理鼠标事件：

```java
private final HudPositionSetting position = setting(
    new HudPositionSetting(
        "position",
        "example.setting.position",
        new HudPosition(0.5, 0.5)
    )
);
```

Minecraft HUD 适配器实现 `HudElementPreview` 后，在组合根通过共享 API 注册：

```java
context.hudPositions().register(position, preview);
```

注册后的设置由 `SettingControlFactory` 自动生成“更改位置”控件。通用编辑屏幕负责隐藏无关原版 HUD、绘制选中预览、限制元素不越界、拖拽输入和 Esc 返回；鼠标移动阶段只改草稿，释放时才写入 `Setting`，从而只触发一次配置保存。

位置使用 `[0,1]` 归一化坐标，表示元素在当前可移动区域内的左上位置。渲染端必须通过 `HudPositionLayout.resolve(...)` 解析实际像素坐标，不得把屏幕尺寸或 GUI scale 写进配置。

### 抽屉单选与模块快捷键

模块只声明值，不处理抽屉绘制、焦点或原始输入：

```java
private final ChoiceSetting<TargetType> target = setting(
    new ChoiceSetting<>(
        "target",
        "example.setting.target",
        TargetType.PLAYER,
        List.of(
            new ChoiceOption<>("player", "example.option.player", TargetType.PLAYER),
            new ChoiceOption<>("hostile", "example.option.hostile", TargetType.HOSTILE)
        )
    )
);

private final KeybindSetting shortcut = keybind(
    new KeybindSetting("keybind", "example.setting.module_keybind")
);
```

- `ChoiceOption.id()` 是配置兼容所需的稳定 ID；不得使用显示文本或列表下标代替。
- `ChoiceSetting` 始终只保存一个已声明选项的值，左键展开抽屉并选择；只有抽屉选项边界内的鼠标悬停才显示选项高亮，移出边界必须立即清除。
- 每个模块最多调用一次 `keybind(...)`；不声明则不参与模块快捷键调度。
- 快捷键默认未绑定；监听时普通键确认、Esc 取消并保留原值、Backspace 清除。
- 模块快捷键只在 `client.gui.screen() == null` 的正常游戏画面按下沿触发；长按不重复，GUI 内按住后关闭也不误触发。
- 动态模块快捷键由一个 `ModuleShortcutDispatcher` 统一轮询；模块不得注册自己的全局按键监听。
- 非模块切换动作使用普通 `KeybindSetting` 与 `KeybindActionDispatcher` 复用相同的按下沿和屏幕抑制语义；Click GUI 的 `open_shortcut` 默认右 Shift，并由通用 JSON 配置持久化。
- 鼠标键、组合键和冲突提示不属于当前 API，后续需先更新规格。

新增设置类型时，同时提供：

1. 设置的领域模型和验证测试。
2. 对应组件。
3. 工厂注册。
4. 输入、焦点和渲染测试或可重复的手动验证步骤。

未知设置类型必须显示明确的“不支持”占位或记录诊断信息，不能静默消失。

### 2. 面向自定义页面的组件 API

自定义页面可以直接组合本地组件：

```java
UiComponent slider = Components.slider(opacitySetting);
UiComponent toggle = Components.toggle(enabledSetting);
```

组件优先直接绑定 `Setting<T>`；需要复用到非设置状态时，使用显式 `ValueBinding<T>`。不接受一组容易失同步的 `value/min/max/onChange` 裸参数。允许提供纯展示型组件，但其命名必须明确为无绑定版本。

所有控件遵守统一协议，至少覆盖：

```text
measure(constraints) -> preferred size
layout(bounds)       -> 保存最终边界
extract(context)     -> 提取本帧 GUI 渲染状态
pointer/key/scroll   -> 返回是否消费事件
focus/blur           -> 管理键盘焦点
narration            -> 提供可访问文本
```

通用边界、可见性、启用状态、悬停、焦点和输入捕获由基础组件处理，不在每个控件中重复实现。

## 参考图 Click GUI 壳层

默认根组件采用参考图中的两栏结构：

```text
ClickGuiView
├── Sidebar
│   ├── SidebarHeader        # 菜单、品牌、运行版本
│   ├── CategoryNavItem[]    # 图标、分类名、模块数量
│   └── FooterNavItem[]      # Config Manager、Settings
└── ContentArea
    ├── ContentHeader        # 页面标题、SearchField
    └── PageHost
        ├── ModuleList
        │   └── ModuleRow[]  # 名称、说明、齿轮、ToggleSwitch
        └── ModuleSettingsPage
```

长期约束：

- 宽屏固定侧栏，窄屏由菜单按钮打开覆盖式侧栏。
- 分类数量来自 `ModuleManager`，不得硬编码。
- 搜索默认只过滤当前分类，匹配本地化名称、说明和稳定 ID。
- 点击齿轮进入模块设置页；只有开关负责模块启用/禁用。
- 模块开关必须调用 `ModuleManager` 生命周期 API，不能直接改布尔字段。
- 没有设置的模块保留齿轮布局位置但显示为禁用，保证各行对齐。
- 主列表使用裁剪和独立滚动；滚轮只作用于悬停容器。
- 品牌版本来自 Fabric 模组元数据，不在 GUI 中硬编码。
- 图标使用项目自有或许可兼容的纹理图集，不用 Unicode 字符冒充图标。
- `gui-example.png` 中的品牌和模块名称不是项目数据，也不打包为运行时背景。

### 3. 面向组件实现者的渲染 API

`UiRenderContext` 是对 Minecraft 26.2 GUI 渲染能力的薄封装，提供项目需要的最小原语：

- 填充矩形、渐变、边框
- 文本与纹理
- 裁剪区域
- 变换
- 主题、字体和帧时间

不要包装整个 Minecraft 渲染系统，也不要在模块层暴露 `GuiGraphicsExtractor`。缺少原语时先证明至少一个组件确实需要，再扩展接口。

## SliderComponent 契约

滑块是可复用 GUI API 的基准组件，实现必须满足：

- 直接绑定 `DoubleSetting`。
- 指针位置到值的转换统一经过 `min/max/step` 规范化。
- 点击轨道立即更新值；拖动时使用指针捕获，指针离开组件仍可继续拖动。
- 释放按键或失去屏幕焦点时结束拖动，不能留下卡住状态。
- 宽度过小、`min == max` 和非法配置有明确定义的退化行为。
- 支持键盘微调；方向键按一步调整，Home/End 到达边界。
- 展示值使用设置提供的格式化器，不在组件里硬编码小数位。
- 填充比例始终限制在 `[0, 1]`。
- 禁用或隐藏时不响应输入。
- 渲染不改变设置值；只有输入处理可以写入。

滑块的数值映射和拖动状态机优先写成不依赖 Minecraft 的纯 Java 逻辑，以便单元测试。

## Screen、布局与输入

- 自定义屏幕继承 `Screen`。
- 在 `init()` 中创建或重建依赖屏幕尺寸的组件树；构造器中不要读取尚未初始化的宽高。
- Minecraft 26.2 GUI 使用渲染状态提取模型；屏幕在 `extractRenderState(...)` 中提交绘制内容，并调用 `super` 保留背景和原生控件。
- `ClickGuiScreen` 只做 Minecraft `Screen` 与项目组件树之间的桥接。
- 屏幕保存父屏幕，关闭时返回父屏幕；不要无条件丢回游戏。
- 输入按视觉层级从上到下分发，第一个消费事件的组件阻止继续传播。
- 只有悬停组件接收滚轮；只有聚焦组件接收文本和普通按键。
- 拖动使用显式指针捕获，不用全局 `isDragging` 猜测当前目标。
- Esc 先关闭弹出层，再关闭主 GUI。
- 窗口缩放或 GUI scale 改变时重新布局，不缓存失效坐标。

布局使用 GUI 逻辑像素和 `Rect`/`Size` 值对象。组件不得读取固定屏幕分辨率，也不得在各控件里散落主题间距数字。

## 主题与视觉一致性

`Theme` 是不可变的设计令牌集合，至少集中管理：

- 背景、面板、边框、正文、弱化文本和强调色
- 危险、警告、成功和禁用状态色
- 间距、圆角、控件高度和边框宽度
- 动画时长与缓动

规则：

- 颜色统一为 ARGB；Minecraft 1.21.6 以后 GUI 文本传 RGB 会得到透明文本。
- 组件不硬编码品牌颜色或字体尺寸。
- 主题切换只替换令牌，不重建模块或设置状态。
- 动画依据帧时间计算，不依据帧数，且渲染过程中不修改领域状态。
- 文字、焦点和禁用状态必须可辨识；不能只依赖颜色表达含义。
- 所有交互控件提供悬停、按下、聚焦和禁用视觉状态。

## 渲染边界

Minecraft 26.2 支持可选 Vulkan 后端，禁止使用裸 OpenGL。使用 Minecraft/Fabric 提供的 Blaze3D、GUI 提取器、渲染管线和更高层 API。

每个裁剪或变换操作必须成对恢复，推荐使用作用域对象或 `try/finally`。渲染方法不得执行文件 I/O、网络请求、模块注册或配置保存。

Mixin 不能成为通用渲染入口。优先使用 `Screen`、Fabric HUD API 和事件；确需 Mixin 时，写明目标方法、使用原因、版本风险和无法使用事件的证据。

## 配置持久化

配置层序列化模块 ID、启用状态和设置原始值，不序列化 GUI 组件或 Minecraft 对象。

配置格式至少包含 `schemaVersion`。加载流程：

1. 解析为 DTO。
2. 根据稳定 ID 匹配已注册模块和设置。
3. 通过设置的公共验证路径应用值。
4. 对未知键保守处理并记录诊断。
5. 对损坏或越界值使用默认值或规范化结果，不让客户端崩溃。

`JsonConfigService` 监听 `Setting` 和 `ModuleManager` 的真实变化并立即保存；无变化时不得重复写盘，也不得在渲染或 Tick 轮询中保存。默认路径是 `<gameDir>/edge-client/cfg/edge-config.json`，开发环境对应 `run/edge-client/cfg/edge-config.json`。写入使用同目录临时文件再原子替换目标文件，降低中断导致配置损坏的风险；客户端停止时再强制保存一次。

不要依赖未声明的传递依赖完成 JSON 编解码。新增库前先检查现有运行时能力、许可证和最终 JAR 影响。

## 线程模型

- Minecraft 世界、玩家、模块状态、输入、组件树和渲染只在客户端线程访问。
- 后台线程只能处理不接触游戏对象的纯数据工作。
- 后台解析出的配置必须作为不可变结果回到客户端线程应用。
- 不要用 `synchronized` 掩盖不清晰的所有权；优先保持单线程状态模型。
- 异步任务必须有取消、异常记录和客户端关闭行为。

## Fabric 与版本边界

所有版本绑定代码在实现前先核对 Minecraft 26.2/Fabric 官方文档或 Gradle 已解析源码，特别是：

- `Screen` 生命周期与 `extractRenderState(...)`
- `GuiGraphicsExtractor` 和 GUI 渲染管线
- 按键注册与客户端 tick 消费
- HUD、客户端生命周期和资源重载事件
- Mixin 目标类与方法签名

不要从旧版教程复制 `DrawContext`、旧渲染回调、Yarn 名称或裸 OpenGL 示例。26.1+ 的项目使用当前官方/未混淆名称；以本项目可编译源码为最终准绳。

官方参考：

- <https://docs.fabricmc.net/develop/getting-started/project-structure>
- <https://docs.fabricmc.net/develop/rendering/gui/custom-screens>
- <https://docs.fabricmc.net/develop/rendering/gui-graphics>
- <https://docs.fabricmc.net/develop/key-mappings>
- <https://docs.fabricmc.net/develop/events>
- <https://docs.fabricmc.net/develop/rendering/basic-concepts>

## 代码约定

- 包名小写；类型使用 `UpperCamelCase`；字段和方法使用 `lowerCamelCase`；常量使用 `UPPER_SNAKE_CASE`。
- ID 使用小写 ASCII `snake_case` 或点分段格式，并在首次发布后保持稳定。
- 面向用户的文字使用 `Component` 和语言键，不在 Java 中散落中文或英文常量。
- 公共 API 和非直观约束写 Javadoc，注释解释“为什么”，不复述代码。
- 优先 `final`、不可变值对象和构造器注入。
- 不引入 Lombok、反射扫描或自动依赖注入容器。
- 不捕获后忽略异常；日志包含模块/设置 ID 等诊断上下文，但不刷屏。
- 不顺手格式化或重构与当前任务无关的文件。
- 不提交生成目录、运行目录、IDE 私有配置或秘密信息。

## 测试策略

当前 `build.gradle` 尚未声明测试框架。第一次添加单元测试时，先确认与 Java 25/Gradle 兼容的测试框架并获得新增依赖许可；随后显式声明版本，不依赖传递依赖。

纯 Java 单元测试覆盖：

- 设置默认值、验证、步长、可见性和变更通知
- 模块幂等生命周期和重复 ID 拒绝
- Slider 数值映射、边界、舍入和拖动状态机
- 布局测量、命中测试、焦点和输入消费
- 配置往返、未知字段、损坏数据和版本迁移

Minecraft 集成验证覆盖：

- 按键能够打开 Click GUI
- 关闭后返回正确父屏幕
- 调整设置后模块立即读取到同一值
- 调整 GUI scale 和窗口大小后布局正确
- 测试环境支持时分别检查 OpenGL 与 Vulkan；任何环境都不得调用后端专属 API
- 重启客户端后配置保持

每个行为变更先写失败测试，再写最小实现。渲染外观难以自动断言时，提供固定窗口尺寸、GUI scale、主题和操作步骤的可重复手动检查。

## 实现顺序

按可运行的纵向切片实现，不一次性铺满所有类：

1. 删除模板服务端 Mixin，建立最小 `ClientRuntime`。
2. 实现 `Module`、`ModuleManager`、`Setting<T>`、`BooleanSetting`、`DoubleSetting` 及单元测试。
3. 实现 `Rect`、组件协议、主题和最小渲染上下文。
4. 实现绑定 `DoubleSetting` 的 `SliderComponent`，完成纯逻辑测试。
5. 实现 `SettingComponentFactory` 和只包含一个示例模块的 Click GUI。
6. 注册 GUI 按键并通过 `runClient` 完成输入、缩放和关闭流程验证。
7. 实现带版本的配置加载/保存，再扩展其余设置组件。
8. 有真实需求后再加入动画系统、HUD 编辑器或内部事件总线。

每一步都必须能够独立构建和验证。单个任务尽量不修改超过约 5 个文件。

## 工作边界

始终执行：

- 修改前读取相关源文件、测试、配置和一个现有相似模式。
- 使用本项目真实版本核对 Fabric/Minecraft API。
- 保持模块到 GUI 的单向解耦。
- 为设置约束和交互状态机添加测试。
- 运行与改动范围匹配的 `test`、`check` 或 `build`。
- 更新本文件中已失效的架构事实。

先询问：

- 添加第三方依赖。
- 改变模组 ID、包根、Minecraft/Fabric/Java 版本或许可证。
- 改变公开 API、配置格式或稳定 ID。
- 新增会影响兼容性的 Mixin、Accessor 或 Invoker。
- 引入网络通信、账号、遥测、自动更新或服务端协议。
- 大规模移动包、重写 GUI 框架或删除用户配置。

绝不执行：

- 在模块中复制滑块、开关等 GUI 实现。
- 从渲染方法修改模块注册、执行 I/O 或触发阻塞任务。
- 使用裸 OpenGL 或后端专属渲染调用。
- 静默吞掉配置、Mixin 或渲染异常。
- 使用显示文本作为持久化键。
- 为通过测试而删除、跳过或弱化测试。
- 提交密钥、令牌、个人路径、`build/`、`run/` 或崩溃日志。

## GUI 动画 API

动画属于 `ui.animation` 和 `ui.theme.ClickGuiMotion`，模块与设置层不得自行维护动画状态。

- `AnimatedDouble` 负责单值插值、途中重新定向和精确落到目标值。
- `AnimatedScroll` 负责滚轮目标累加、上下边界限制和双向平滑重定向；滚轮事件不得直接跳变可见偏移。
- `AnimatedRect` 负责选择指示器等几何区域的平滑移动。
- `AnimationFrameClock` 每帧只计算一次增量，并限制超长帧，所有控件共享该增量。
- `AnimationSpec` 与 `Easing` 定义持续时间和缓动；Click GUI 的默认令牌集中在 `ClickGuiMotion`。
- `ArgbColor` 负责 ARGB 通道插值，不在组件中重复实现颜色混合。
- 任何设置通过 `SettingControlFactory` 生成 `ToggleControl` 后自动获得开关动画。
- 模块列表与模块设置页的总开关必须通过 `SettingControlFactory.createModuleEnabled` 创建，实时绑定同一个 `ModuleManager` 状态，不复制启用值。
- 打开 GUI 快捷键默认是右 Shift，且可持久化修改；呼出时的整窗动画由 `ClickGuiAnimations` 和 `ClickGuiMotion.GUI_OPEN` 统一提供。
- 折叠单选抽屉由 `ChoiceControl` 和 `ClickGuiMotion.CHOICE_DRAWER` 自动提供动画。
- 模块作者不得在模块类中调用动画、渲染或输入 API；动画由 GUI 壳层根据绑定状态驱动。
- 渲染方法只能推进显示动画，不得借动画进度修改模块或设置的真实值。

## Auto Web 当前契约

`client:auto_web` 位于战斗分类，模块实现集中在 `module.impl.combat.autoweb`。GUI 只枚举其设置，不包含模块专用的绘制或输入分支。

默认值：

- 目标优先级：距离最近。
- 目标类型：玩家。
- 放置形状：只放脚部。
- 范围：3 格（1–6，步长 1）。
- 静默转头：2 Ticks（1–20，步长 0.1）。
- 快捷栏：静默切换并恢复；背包检查默认关闭。
- 背包模式：静默交换当前选择槽并恢复。
- 放置节奏：每完成一次平滑转头放一个，每 Tick 最多一个。
- 放置间隔：1 Tick（0.1–20，步长 0.1），仅间隔模式使用。
- 模块快捷键：默认未绑定。

长期约束：

- 只向可替换且存在合法支撑面的目标方块发起原版客户端预测交互。
- 静默转头只发送服务器旋转并更新第三人称头部/身体朝向，不修改第一人称相机 yaw/pitch。
- 普通模式不得在同一 Tick 放置多个；只有“转头后全部放置”模式允许同 Tick 尝试多个位置。
- 临时背包交换必须恢复原背包与快捷栏物品；模块禁用和 GUI 打开时清理运行计划。
- Friend 尚未实现，因此当前版本还不能按好友名单排除玩家；接入 Friend 时必须在目标筛选边界统一排除。

## Auto Librarian 当前契约

`client:auto_librarian` 位于玩家分类，实现集中在
`module.impl.player.autolibrarian`。迁移来源只提供行为参考；旧项目的 Click GUI、
独立配置管理器、按键监听和通知实现不得重新引入。

- 默认目标是精准匹配 `minecraft:unbreaking` III，价格 1–64 绿宝石。
- 多目标列表、每项目标的精准/任意等级和价格范围必须完整保留，不得降级成单目标。
- 普通参数继续声明为 `BooleanSetting`、`DoubleSetting` 和 `KeybindSetting`。
- 多目标参数声明为 `EditorSetting` + `JsonSetting`；模块不得依赖 Screen、控件、
  坐标或鼠标事件。
- 复杂设置编辑器通过 `SettingEditorApi` 在组合根注册；Click GUI 自动生成编辑入口。
- 自动化保留村民定位、合法讲台位置、平滑转向、原版交易交互、交易匹配、破坏、
  可选掉落回收和返回起点的状态机。
- 自动回收必须跟随本轮讲台掉落物实体走到拾取范围，并以背包中的讲台数量恢复
  作为拾取成功确认；实体丢失时重新扫描，未确认拾取不得进入返回起点阶段。
- 自动化停止、成功、失败、世界变化或玩家手动移动后，必须释放挖掘与移动输入，
  恢复原快捷栏槽位和 yaw/pitch，并关闭临时交易容器。
- 模块快捷键仍由 `ModuleShortcutDispatcher` 统一处理，默认未绑定；模块不得注册
  自己的全局键盘轮询。
- 所有设置和目标列表写入现有
  `<gameDir>/edge-client/cfg/edge-config.json`，不得创建旧
  `client-auto-librarian.json`。
- 打开交易时的附魔书汇报独立于自动化启用状态，但仍读取同一个模块 Setting，
  并使用共享 `MessageBoxApi`。
- 不得迁移来源项目中的 `getinfo/getid.java`、网络、账号、遥测或其他无关功能。

## 完成定义

变更只有在以下条件满足时才算完成：

- 分层和依赖方向没有被破坏。
- 新模块通常只需声明设置即可获得统一 GUI。
- 新组件可在自定义页面复用，且不复制领域状态。
- 对应自动化测试通过。
- `.\gradlew.bat build` 通过；开发中可先用 `check` 做较快反馈。
- GUI 变更已用 `runClient` 验证输入、缩放、焦点和视觉状态。
- 配置或公共 API 变更包含迁移说明。
- 无新增警告、秘密信息、生成产物或无关改动。
