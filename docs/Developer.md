# 开发文档（By Claude）

---

## 1. 快速开始

### 1.1 引入依赖

在settings.gradle添加
Groovy使用
```Groovy
maven { 
    url 'https://jitpack.io'
}
```
Kotlin使用
```Kotlin
maven {
    url = uri("https://jitpack.io")
}
```
添加依赖，版本以 Release 的 Tag 为准
```Kotlin
implementation("com.github.Chiu-xaH.SharedNav:navigation-android:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation-jvm:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation-ios:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation:<version>")
```

---

### 1.2 创建第一个 Destination

每个页面对应一个 `Destination` 对象，继承抽象类并实现 `key` 与 `Content()`：

```kotlin
object HomeDestination : Destination() {
    override val key = "home"   // 全局唯一，同时用作容器共享的匹配 Key

    @Composable
    override fun Content() {
        HomeScreen()
    }
}
```

---

### 1.3 初始化导航宿主

在 Activity 或顶层 Composable 中启动导航：

**写法 1：**

```kotlin
@Composable
fun App() {
    SharedNavHost(
        startDestination = HomeDestination
    )
}
```

**写法 2：手动控制 NavController**

```kotlin
val navController = rememberNavController(startDestination = HomeDestination)

SharedNavHost(
    navController = navController,
)
```

---

### 1.4 页面跳转与返回

在任意 Composable 中通过 `LocalNavController` 或 `LocalNavControllerSafely` 获取控制器：

> **提示**：如果无法保证 Composable 函数一定在 `SharedNav` 下调用，请使用 `LocalNavControllerSafely`，它返回可空对象；`LocalNavController` 获取不到控制器时会直接抛出异常导致 Crash。

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current

    Button(onClick = { navController.push(DetailDestination) }) {
        Text("进入详情")
    }

    Button(onClick = { navController.pop() }) {
        Text("返回")
    }
}
```

---

### 1.5 添加容器共享动效

用 `SharedContainer` 包裹触发跳转的组件，`key` 与目标 `Destination.key` 保持一致，即可获得类 Launcher 的展开/收起动画。

**写法 1：**

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val dest = DetailDestination

    SharedContainer(
        key = dest.key,
        shape = MaterialTheme.shapes.medium,
    ) {
        // 内层组件 shape 必须为 RoundedCornerShape(0.dp) 或 RectangleShape
        Card(
            shape = RectangleShape,
            onClick = { navController.push(dest) }
        ) { /* 内容 */ }
    }
}
```

**写法 2：Modifier 扩展**

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val dest = DetailDestination

    Card(
        modifier = Modifier.sharedContainer(
            key = dest.key,
            shape = MaterialTheme.shapes.medium
        ),
        shape = RectangleShape,
        onClick = { navController.push(dest) }
    ) { /* 内容 */ }
}
```

> **注意**：`SharedContainer` 内层组件的 `shape` 必须设置为无圆角，圆角统一由外层 `SharedContainer` 管理，否则在提取 1 像素时会缺失边角。

`SharedContainer` 除必填的 `key` 和 `shape` 外，还有以下可选参数：

| 参数                        | 类型                        | 默认值       | 说明                                                                   |
|---------------------------|---------------------------|-----------|----------------------------------------------------------------------|
| `shadow`                  | `Dp`                      | `0.dp`    | 阴影                                                                   |
| `containerColor`          | `Color?`                  | `null`    | `null` 时：SDK 33+ 使用像素提取填充，低版本使用裁切填充；指定颜色时：SDK 33+ 使用像素提取填充，低版本使用颜色填充 |
| `containerFilledStrategy` | `ContainerFilledStrategy` | `Pixel()` | 更精细地指定填充方式，与 `containerColor` 二选一，不同时使用                              |

---

## 2. 核心概念

### 2.1 模块结构

| 模块                 | 职责                                          |
|--------------------|---------------------------------------------|
| `navigation`       | 页面路由、返回栈管理、导航时的前景、背景过渡动画                    |
| `shared-container` | 容器共享动效核心：记录源与目标的Rect与GraphicsLayer，驱动容器共享动画 |
| `floating-window`  | 浮层窗口系统：BottomSheet、Dialog 等，可与容器共享联动        |
| `common`           | 两个模块共用的工具代码                                 |
| `app`              | 示例应用                                        |

## 3. 导航 API — `navigation` 模块

### 3.1 `Destination`

所有页面的基类，开发者继承后实现以下成员：

| 成员                   | 类型                          | 默认值     | 说明                                                  |
|----------------------|-----------------------------|---------|-----------------------------------------------------|
| `key`                | `String`                    | 必填      | 全局唯一，同时作为容器共享的匹配 Key                                |
| `Content()`          | `@Composable`               | 必填      | 页面 UI 内容                                            |
| `PlaceHolder`        | `(@Composable () -> Unit)?` | `null`  | 启动屏内容，动画期间先显示，结束后切换为真实内容                            |
| `enforcePlaceHolder` | `Boolean`                   | `false` | 强制在动画期间显示 PlaceHolder，不依赖全局 `enableSplashScreen` 开关 |

**带参数的 Destination（推荐用 `data class`）：**

```kotlin
data class SecondDestination(val userId: Int) : Destination() {
    override val key = "second_$userId"

    @Composable
    override fun Content() { SecondScreen() }
}

// 使用
navController.push(SecondDestination(userId = 42))
```

---

### 3.2 `rememberNavController`

创建并记住一个 `NavigationController` 实例：

```kotlin
val navController = rememberNavController(startDestination = HomeDestination)
```

---

### 3.3 `SharedNavHost` / `NavHost`

| 参数                  | 类型                       | 默认值                    | 说明           |
|---------------------|--------------------------|------------------------|--------------|
| `startDestination`  | `Destination`            | 必填                     | 初始页面         |
| `navController`     | `NavigationController`   | 内部自动创建                 | 传入已有实例以手动控制  |
| `modifier`          | `Modifier`               | `Modifier`             | 应用到宿主容器      |
| `dependencies`      | `Dependencies`           | `Dependencies()`       | 跨页面注入的依赖数据   |
| `backHandler`       | `@Composable () -> Unit` | `DefaultBackHandler()` | 自定义返回手势/按键处理 |

`SharedNavHost` 在内部自动包裹 `SharedContainerRoot` 与 `FloatingRoot`，启用容器共享和浮层功能。不需要这两者时可直接使用 `NavHost`。

---

### 3.4 `NavigationController`

#### 核心方法

```kotlin
// 跳转到目标页面
fun push(destination: Destination, launchMode: LaunchMode = LaunchMode.Push(reuse = true), effect: TransitionEffect = defaultTransitionEffect)

// 返回上一页
fun pop()

// 获取当前栈顶
fun current(): StackEntry

// 获取上一个栈条目（可空）
fun previous(): StackEntry?

// 是否可以返回
fun canPop(): Boolean

// 当前栈顶是否为指定 Destination
fun isCurrentDestination(destination: Destination): Boolean

// 挂起直到过渡动画完成
suspend fun awaitTransition()
```

#### 属性一览

| 属性                    | 类型                 | 默认值              | 说明                                             |
|-----------------------|--------------------|------------------|------------------------------------------------|
| `startDestination`    | `Destination`      | 只读               | 初始 Destination                                 |
| `stack`               | `List<StackEntry>` | 只读               | 当前导航返回栈                                        |
| `transitionLevel`     | `EffectLevel`      | `FULL`           | 全局动效等级，可运行时动态切换                                |
| `enableSplashScreen`  | `Boolean`          | `false`          | 动画期间是否对前景页显示 PlaceHolder                       |
| `enableKeepAlive`     | `Boolean`          | `false`          | 保留页面不被销毁（仅被遮挡），节省 POP 重建开销，多页面时有 OOM 风险，慎用     |
| `enableBlur`          | `Boolean`          | API 31+为`true`   | 是否启用背景模糊效果，低于 API 31 自动为 `false`               |
| `enableShader`        | `Boolean`          | API 33+为`true`   | 是否启用 RuntimeShader（像素提取、镜面等），低于 API 33 自动为 `false` |
| `enablePredictiveBack`| `Boolean`          | API 33+为`true`   | 是否启用预测式返回手势，低于 API 33 自动为 `false`              |
| `isTransitioning`     | `Boolean`          | `false`（只读）      | 当前是否正在播放过渡动画                                   |
| `inPredictive`        | `Boolean`          | `false`（只读）      | 当前是否处于预测式返回手势拖拽中                               |
| `transitionProgress`  | `Animatable<Float>`| 只读               | 过渡进度（0f→1f），可用于自定义联动效果                         |

---

## 4. 容器共享 API — `shared-container` 模块

### 4.1 `SharedContainer`（Composable 写法）

包裹触发跳转的卡片/按钮，作为容器共享的「源端」。

| 参数                        | 类型                        | 默认值        | 说明                                     |
|---------------------------|---------------------------|------------|----------------------------------------|
| `key`                     | `String?`                 | 必填         | 与目标 `Destination.key` 完全一致；传 `null` 时禁用共享 |
| `shape`                   | `CornerBasedShape`        | 必填         | 容器圆角形状，过渡时会对此圆角做插值                     |
| `containerColor`          | `Color?`                  | `null`     | 容器背景色；`null` 时自动使用 1 像素填充策略            |
| `containerFilledStrategy` | `ContainerFilledStrategy` | `Pixel()`  | 容器填充策略，详见 4.3 节，与 `containerColor` 二选一 |
| `shadow`                  | `Dp`                      | `0.dp`     | 容器阴影                                   |
| `modifier`                | `Modifier`                | `Modifier` | 外层修饰符                                  |

```kotlin
SharedContainer(
    key = dest.key,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.primaryContainer,
) {
    Card(shape = RectangleShape, ...) { ... }
}
```

---

### 4.2 `Modifier.sharedContainer()`（扩展函数写法）

效果等同于 `SharedContainer` 包裹，可直接挂载到已有组件上：

```kotlin
Card(
    modifier = Modifier.sharedContainer(
        key = dest.key,
        shape = MaterialTheme.shapes.medium
    ),
    onClick = { navController.push(dest) }
) { /* 内容 */ }
```

---

### 4.3 `ContainerFilledStrategy`（容器填充策略）

控制容器展开过渡时「未填充区域」的视觉处理方式：

| 策略                     | 效果                        | 说明                                                |
|------------------------|---------------------------|---------------------------------------------------|
| `Pixel(spareStrategy)` | 取底部或右侧 1 像素拉伸填充           | 效果最好，需 SDK 33+，低版本自动降级到 `spareStrategy`           |
| `Color(color)`         | 用指定纯色填充                   | 兼容所有版本，适合有明确主色的卡片                                 |
| `Clip`                 | 裁切放大                      | 兼容性最强，开发成本最低，但无分层效果                               |
| `Stretch`              | 拉伸填充                      | 类老版本 EMUI 风格，观感一般，优先考虑 `Clip`                     |
| `Element`              | 内容透明渐变，置于中央裁切              | 专用于元素级共享（非整卡片），不受 `enforceContainerFilledStrategy` 限制 |

![effect_level](../src/filled.jpg)

为达到开发效率和效果的平衡，`SharedContainer` 有 `containerColor` 字段，SDK33+ 时使用 `Pixel` 方案，否则读取 `containerColor`，为 `null` 则使用 `Clip` 方案，不为 `null` 则使用 `Color` 方案。

```kotlin
@Composable
fun SharedContainer(
    key : Any,
    shape : Shape,
    modifier : Modifier = Modifier,
    shadow : Dp = 0.dp,
    containerColor : Color?,
    content : @Composable () -> Unit
) = SharedContainer(
    key,
    shape,
    modifier,
    shadow,
    if(containerColor == null) {
        ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Clip)
    } else {
        ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Color(containerColor))
    },
    content
)
```

---

### 4.4 `ContentStrategy`（内容策略）

控制目标页面（`SharedContent`）在共享动画期间的行为，通常由框架自动选择，高级场景下可手动传入：

| 策略                    | 说明                                                         |
|-----------------------|-------------------------------------------------------------|
| `Navigation`          | 默认。适用于全屏页面导航，container 展开后不隐藏，content 正常插值过渡              |
| `Layer(isFloating)`   | container 展开后隐藏，content 正常插值过渡。`isFloating = true` 时用于浮层窗口，`false` 时用于同级容器互换 |
| `Copy`                | container 不运动也不隐藏，content 以透明度 0 从 container 处淡入飞出         |

---

### 4.5 `SharedRegistry`

容器共享的核心注册表，通过 `LocalSharedRegistry.current` 访问，可在运行时动态调整动画参数：

| 属性                              | 类型                        | 默认值              | 说明                                                           |
|---------------------------------|---------------------------|--------------------|--------------------------------------------------------------|
| `enabled`                       | `Boolean`                 | `true`             | `false` 时关闭容器共享动画                                           |
| `animationTime`                 | `Int`                     | `500`（ms）         | 容器共享动画时长                                                     |
| `pushEasing`                    | `Easing?`                 | `null`             | push 时的自定义缓动曲线；`null` 时使用内置贝塞尔参数                            |
| `popEasing`                     | `Easing?`                 | `null`             | pop 时的自定义缓动曲线；`null` 时使用内置贝塞尔参数                             |
| `pushX1/Y1/X2/Y2`               | `Float`                   | `0.4/0.65/0.25/1.0`| push 贝塞尔曲线控制点，可在运行时调节（示例 App 中有可视化调节器）                     |
| `popX1/Y1/X2/Y2`                | `Float`                   | `0.4/0.65/0.15/1.0`| pop 贝塞尔曲线控制点                                                 |
| `waitFrameMaxValue`             | `Int`                     | `10`               | 等待容器测量的最大帧数，超过则降级为普通导航动画。页面越复杂可适当调大，一般不超过 10 帧              |
| `enablePredictiveBack`          | `Boolean`                 | API 33+为`true`    | 是否在容器共享时启用预测式返回手势支持                                          |
| `enforceContainerFilledStrategy`| `ContainerFilledStrategy?`| `null`             | 全局强制覆盖所有容器的填充策略；`null` 表示不强制，各容器使用自身策略                     |
| `speedUpRadio`                  | `Float`                   | `1.5f`             | 渐隐、圆角变化速度相对容器运动的倍率，值越大圆角/透明度变化越先完成                         |
| `enableTilt`                    | `Boolean`                 | `true`             | 是否启用容器动画时的倾斜（透视）效果                                           |
| `tiltMaxValue`                  | `Float`                   | `20f`              | 最大倾斜角度（度）                                                    |
| `extensionDouble`               | `Boolean`                 | `false`            | `false` 时底部或右侧单边像素填充，`true` 时上下或左右双边填充                      |

![effect_level](../src/extension.jpg)

---

## 5. 浮层窗口 API — `floating-window` 模块

浮层窗口系统独立于导航栈，用于展示 BottomSheet、Dialog 等覆盖层内容，支持与容器共享动画联动。`SharedNavHost` 已自动内置，无需手动添加。

### 5.1 Window 类型

所有浮层窗口继承自 `Window`，框架提供以下几种预设基类：

| 基类             | 表现                                   | 默认动画                        |
|----------------|--------------------------------------|------------------------------|
| `BottomSheet`  | 底部弹出，紧贴屏幕底边，顶部圆角跟随屏幕圆角               | 从底部滑入/滑出                    |
| `BottomDialog` | 底部弹出，带导航栏 padding，圆角为完整圆角             | 弹簧弹入（低阻尼）/ 滑出               |
| `CenterDialog` | 居中弹出，带状态栏和导航栏 padding                 | 缩放淡入/淡出（`scaleIn + fadeIn`） |
| `SharedWindow` | 居中对齐，支持指定 `key` 与容器共享联动，展开/关闭有容器形变动画 | 与容器共享动画同步                   |
| `Window`       | 完全自定义基类，自行实现 `Layer()` 布局            | `scaleIn + fadeIn`           |

**实现一个 BottomDialog：**

```kotlin
object MyBottomDialog : BottomDialog() {
    override val modifier = Modifier.padding(horizontal = 16.dp)

    @Composable
    override fun BoxScope.Content() {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) {
            // 内容
        }
    }
}
```

**实现一个与容器共享联动的 SharedWindow（如从卡片展开的详情弹窗）：**

```kotlin
data class DetailDialog(val id: String) : SharedWindow() {
    override val key = "detail_$id"   // 与触发它的 SharedContainer.key 一致

    @Composable
    override fun BoxScope.Content() {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,  // 使用 SharedWindow 提供的 shape，与容器圆角同步
        ) {
            // 内容
        }
    }
}
```

`SharedWindow` 的可重写属性：

| 属性        | 类型                | 默认值                         | 说明                   |
|-----------|--------------------|------------------------------|----------------------|
| `key`     | `String?`          | 必填（`null` 时退化为普通 CenterDialog）| 与对应 SharedContainer 的 key 一致 |
| `align`   | `Alignment`        | `Alignment.Center`           | 浮层对齐方式               |
| `shape`   | `CornerBasedShape` | `MaterialTheme.shapes.large` | 弹窗圆角，与容器过渡时做圆角插值     |
| `modifier`| `Modifier`         | `Modifier`                   | 外层修饰符（如 padding）     |

---

### 5.2 `FloatingController`

控制浮层的推入和弹出，通过 `LocalFloatingController.current` 获取：

```kotlin
@Composable
fun HomeScreen() {
    val floatingController = LocalFloatingController.current

    Button(onClick = { floatingController.push(MyBottomDialog) }) {
        Text("显示弹窗")
    }
}
```

#### 核心方法

```kotlin
// 推入一个浮层（栈顶已有相同 window 时自动去重）
fun push(window: Window)

// 关闭当前浮层（触发退场动画后移除）
fun pop()

// 是否有浮层正在显示
val isRunning: Boolean

// 是否可以关闭
fun canPop(): Boolean
```

#### 属性

| 属性              | 类型        | 默认值         | 说明                               |
|-----------------|-----------|-------------|----------------------------------|
| `enableBlur`    | `Boolean` | API 31+为`true` | 浮层背景是否启用模糊                     |
| `enableShader`  | `Boolean` | API 33+为`true` | 是否启用 RuntimeShader               |

---

### 5.3 `FloatingRoot`

浮层系统的宿主容器。`SharedNavHost` 内部已自动包裹，大多数场景无需手动使用。如需在独立场景（不使用 `SharedNavHost`）中启用浮层，可手动包裹：

```kotlin
FloatingRoot {
    // 你的内容
}
```

也可自定义浮层背景效果：

```kotlin
val controller = rememberFloatingController(
    backgroundEffect = BackgroundEffect(
        pageEffect = PageEffect(scale = 1f, blur = 0.dp, mask = 0.4f),
        animationSpec = spring()
    )
)

FloatingRoot(controller = controller) {
    // 你的内容
}
```

通过 `LocalFloatingControllerSafely`（可空）或 `LocalFloatingController`（不可空，获取不到时抛异常）访问控制器。

---

## 6. 动效等级与 PageEffect

### 6.1 `EffectLevel`

控制背景页与前景页在过渡时所应用的视觉效果层级，可运行时通过 `navController.transitionLevel` 切换：

| 等级             | 背景页效果        | 前景页效果          |
|----------------|--------------|----------------|
| `FULL (3)`     | 模糊 + 压暗 + 缩放 | 模糊 + 缩放 + 圆角插值 |
| `NO_BLUR (2)`  | 压暗 + 缩放      | 缩放 + 圆角插值      |
| `NO_SCALE (1)` | 仅压暗          | 缩放 + 圆角插值      |
| `NONE (0)`     | 无效果          | 轻缩放 + 透明度淡入    |

![effect_level](../src/effect_level.jpg)

---

### 6.2 内置过渡效果

以下效果均实现 `TransitionEffect` 接口，可在 `rememberNavController` 或 `push()` 时传入。

#### `DefaultTransitionEffect`（默认，类桌面动效）

起始界面缩小并模糊、压暗，目标界面从屏幕中央偏上位置缩放放大直到完全覆盖。

```kotlin
val navController = rememberNavController(
    startDestination = HomeDestination,
    defaultTransitionEffect = DefaultTransitionEffect()
)
```

#### `FlipTransitionEffect`（侧滑，类多级设置）

起始界面向左位移 1/3 并压暗，目标界面从右侧推入覆盖。

```kotlin
navController.push(DetailDestination, effect = FlipTransitionEffect())
```

#### `SlideTransitionEffect`（四向滑入）

起始界面压暗 + 缩放，目标界面从指定方向滑入。适合底部弹出式全屏页面。

| 参数          | 类型          | 默认值               | 说明                              |
|-------------|-------------|-------------------|---------------------------------|
| `direction` | `Direction` | `Direction.BOTTOM`| 滑入方向：`TOP` `BOTTOM` `START` `END` |

```kotlin
navController.push(DetailDestination, effect = SlideTransitionEffect(Direction.BOTTOM))
```

#### `IslandTransitionEffect`（灵动岛风格）

起始界面模糊、压暗、缩放，目标界面从指定位置（如灵动岛、肩键）飞出并放大。

| 参数          | 类型                | 默认值                          | 说明                                      |
|-------------|-------------------|-----------------------------|------------------------------------------|
| `position`  | `TransformOrigin` | `TransformOrigin(0.5f, 0f)` | 飞出起点，`(0.5f, 0f)` 为顶部中央，可指向灵动岛或任意位置 |

```kotlin
navController.push(DetailDestination, effect = IslandTransitionEffect(
    position = TransformOrigin(0.5f, 0f)
))
```

#### `JumpTransitionEffect`（应用跳转风格）

起始与目标界面同时缩放，互向两侧位移，背景可为纯色或壁纸。适合模拟系统应用切换。

| 参数          | 类型                   | 默认值                              | 说明                                     |
|-------------|----------------------|---------------------------------|-----------------------------------------|
| `background`| `BgEffectBackground` | `BgEffectBackground.Color(Black)` | 背景内容：纯色或位图（壁纸）                        |

`BgEffectBackground` 的两种子类：

| 子类                             | 说明                                  |
|--------------------------------|-------------------------------------|
| `BgEffectBackground.Color(color)` | 纯色背景，兼容所有版本                        |
| `BgEffectBackground.Image(bitmap, mask)` | 位图背景（通常传壁纸），`mask` 控制蒙版透明度 |

使用壁纸作为背景（需存储权限）：

```kotlin
// 需要 READ_WALLPAPER_INTERNAL 或 MANAGE_EXTERNAL_STORAGE 权限
val effect = JumpTransitionEffect(context)  // 自动读取系统壁纸
navController.push(DetailDestination, effect = effect)
```

---

### 6.3 `PageEffect`（内部状态，了解即可）

描述某一帧页面的视觉状态，由 `NavHost` 根据 `transitionProgress`（0f → 1f）自动插值计算。

| 字段       | Full（展开态） | Background（背景态） | None（收起态） |
|----------|-----------|-----------------|-----------| 
| `scale`  | `1.0f`    | `0.875f`        | `0.0f`    |
| `blur`   | `0.dp`    | `25.dp`         | `20.dp`   |
| `mask`   | `0.0f`    | `0.25f`         | `0.0f`    |
| `corner` | 屏幕圆角      | `0.dp`          | 屏幕圆角 × 2  |

---

## 7. `LaunchMode` 启动模式

`LaunchMode` 决定 `push()` 时如何操作导航返回栈：

| 模式                          | 行为                                                                                 |
|-----------------------------|------------------------------------------------------------------------------------|
| `Push(reuse)`               | 压入栈顶。`reuse = true` 时若栈顶已是目标则复用，不重复入栈                                              |
| `PopToExisting`             | 若栈中已有目标实例，清除其上所有页面并执行 pop 回到它；否则正常 push                                            |
| `Single(reuse, actionType)` | 保证栈中只有一个目标实例。`reuse = true` 时复用并清除其余所有项；`actionType = ActionType.POP` 可使过渡动画呈现返回效果 |

**示例：从深层页面一键回到首页，并使用返回动画**

```kotlin
navController.push(
    destination = navController.startDestination,
    launchMode = LaunchMode.Single(
        reuse = true,
        actionType = ActionType.POP
    )
)
```

---

## 8. `Dependencies` 依赖注入

`Dependencies` 是一个轻量键值容器，用于在 `NavHost` 树范围内跨页面传递数据（如外部配置、回调等），无需 ViewModel：

```kotlin
// 在入口处构建依赖，keys 变化时自动重建
val deps = rememberNavDependencies(userId) {
    put(userId, tag = "userId")
    put("admin", tag = "role")
}

SharedNavHost(
    startDestination = HomeDestination,
    dependencies = deps
)

// 在任意子页面中读取
@Composable
fun HomeScreen() {
    val userId = LocalNavDependencies.current.get<Int>("userId")
    val role   = LocalNavDependencies.current.get<String>("role")
}
```

> **提示**：`rememberNavDependencies(vararg keys)` 当 `keys` 发生变化时会重新执行 builder 并更新依赖，适合将外部状态透传给整个导航树。`get<T>(tag)` 中的 `tag` 参数可为 `null`，`null` 对应无 tag 的 `put()` 调用。

---

## 9. 进阶用法

### 9.1 可视化调节贝塞尔曲线

示例 App 内置了贝塞尔曲线可视化调节器（`BezierSettingsDestination`），可在运行时实时调节容器共享动画的 push/pop 缓动曲线（对应 `SharedRegistry` 的 `pushX1/Y1/X2/Y2` 和 `popX1/Y1/X2/Y2` 属性）。

![bezier settings](../src/spec.jpg)

---

### 9.2 屏幕圆角适配

`ScreenCornerHelper` 负责读取设备实际物理圆角，保证页面过渡边角与屏幕对齐：

示例 App 内置了屏幕圆角可视化调节器（`CornerSettingsDestination`），可在运行时实时修改，供 SDK 低于 33 的设备手动校准。

---

### 9.3 Splash Screen（PlaceHolder）

当页面初始化较慢（如相机、大量数据加载）时，在 `Destination` 中提供 `PlaceHolder`，动画期间先展示占位内容，避免卡顿：

```kotlin
object CameraDestination : Destination() {
    override val key = "camera"
    // 强制启用，不受 navController.enableSplashScreen 限制
    override val enforcePlaceHolder = true
    override val PlaceHolder: (@Composable () -> Unit) = {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

    @Composable
    override fun Content() { CameraScreen() }
}
```

![splash screen](../src/splash.jpg)

---

### 9.4 自定义返回手势

`SharedNavHost` 接受 `backHandler` 参数，可替换默认的系统返回拦截逻辑：

```kotlin
SharedNavHost(
    startDestination = HomeDestination,
    backHandler = {
        // 默认返回逻辑
        DefaultBackHandler()
    }
)
```

---

### 9.5 KeepAlive 页面保活

当页面较重（如包含 WebView、视频播放器）时，可开启 `enableKeepAlive` 避免 POP 后重建的性能消耗。注意：保活会使所有历史页面长驻内存，多页面场景下存在 OOM 风险。

```kotlin
navController.enableKeepAlive = true
```

---

## 10. DeepLink

1. 为首Activity配置：
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data android:scheme="your_app"/>
</intent-filter>
```

2. 为首 Activity 的`onCreate`函数中接收 DeepLink 并处理：
```Kotlin
val startDestination = intent?.data?.let { deeplink ->
    DeepLinkRegistry.parse(deeplink)
}

val navController = rememberNavigationController(
    startDestination = startDestination ?: HomeDestination
)
```

3. 在 Application.onCreate 中统一注册需要暴露的 DeepLink：
> 可放心在 Application 初始化时使用,此函数逻辑简单,仅仅是存储一个 HashMap<String,DeepLink> ,数据结构较简单,不会很耗时
```Kotlin
DeepLinkRegistry.init(
    listOf(SecondDeepLnk, BezierSettingsDeepLink)
)

// 带参数的Destination
private val SecondDeepLink by lazy {
    // 可以拿Destination的key作为host，方便管理,也可以自己指定传入
    DeepLink(SecondDestination.KEY) { uri ->
        val userId = uri.getQueryParameter("id")?.toIntOrNull() ?: return@DeepLink null
        SecondDestination(userId = userId)
    }
}
// 不带参数的Destination
private val BezierSettingsDeepLink by lazy {
    DeepLink(BezierSettingsDestination.key) { BezierSettingsDestination }
}
```

使用ADB指令测试：
```bash
adb shell am start -a android.intent.action.VIEW -d "your_app://second?id=999"
adb shell am start -a android.intent.action.VIEW -d "your_app://settings_bezier"
```

使用网页测试：
```html
<!DOCTYPE html>
<html>
<body>

<p>
    <a href="intent://second?id=999#Intent;scheme=your_app;end">
        打开 App
    </a>
</p>

</body>
</html>
```

## 11. 注意事项与常见问题

### 10.1 核心限制

- **圆角必须交给 SharedContainer 管理**：内层组件的 `shape` 必须设为无圆角，否则过渡动画中会导致 1 像素提取缺失边角。
- **key 全局唯一**：同一 `key` 的 `SharedContainer` 与 `Destination.key` 必须完全一致（大小写敏感），否则共享动画不触发。
- **SharedNavHost 是容器共享和浮层的前提**：`SharedContainerRoot` 与 `FloatingRoot` 仅由 `SharedNavHost` 自动提供，直接使用 `NavHost` 时两者均不生效。

### 10.2 系统版本兼容

| 特性            | 最低版本   | 低版本方案                  |
|---------------|--------|------------------------|
| 背景模糊          | API 31 | 无模糊                    |
| 1 像素填充        | API 33 | 使用 `Color` 或 `Clip` 填充 |
| 自动获取屏幕圆角      | API 33 | 无圆角，可手动指定（app 模块有具体示例）  |
| 镜面缩放（共享容器运行时） | API 33 | 背景直接做 scale 缩放         |
| 预测式返回手势       | API 33 | 不启用，退化为普通返回            |

启用预测式返回需在清单文件配置好：
```xml
<application
    android:enableOnBackInvokedCallback="true"
></application>
```