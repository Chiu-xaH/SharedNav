# SharedNav  [![](https://jitpack.io/v/Chiu-xaH/SharedNav.svg)](https://jitpack.io/#Chiu-xaH/SharedNav)
基于 Compose Multiplatform 与 Kotlin Multiplatform 的容器共享（SharedContainer）、页面导航（Navigation）以及全局浮窗（FloatingWindow）库。

复刻系统 Launcher 动画，支持背景模糊、镜面缩放，1像素填充、二次贝赛尔插值、屏幕圆角贴合、预测式返回、并行打断动画等特性；相比于目前[官方文档](https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation?hl=zh-cn)推荐的方案(Navigation2+SharedElements)，可以显著简化 Compose 应用在此方面的开发流程，提高可定制性，且拥有更流畅、精美的交互视觉体验。

支持平台：Wasm(JS)、Desktop(JVM)、iOS、Android
![cover](src/cover.jpg)

## [Demo App](https://github.com/Chiu-xaH/SharedNav/releases/download/1.0.0-dev01/app-release.apk)
### Demo 编译
```bash
./gradlew :app:wasmJsBrowserDevelopmentRun 
./gradlew :app:wasmJsBrowserProductRun
./gradlew :app:jsBrowserDevelopmentRun
./gradlew :app:jsBrowserProductRun
./gradlew :app:run
./gradlew :app:wasmJsBrowserDistribution
./gradlew :appjsBrowserDistribution
./gradlew :app:packageDistributionForCurrentOS
```

## 快速开始

> ⚠️ 本库目前仍处于开发阶段，目前在[聚在工大](https://github.com/Chiu-xaH/HFUT-Schedule)项目中使用率较高，可以体验实际交互效果，看一下里面代码使用方式，再慎重考虑是否接入本库，如有 Bug 及时提。

### 引入依赖
#### 从 JitPack 引入
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

Android 项目：
```Kotlin
implementation("com.github.Chiu-xaH.SharedNav:navigation-android:<version>")
```
KMP 项目，在commonMain里添加：
```Kotlin
implementation("com.github.Chiu-xaH.SharedNav:navigation:<version>")
```
KMP 项目，按平台添加：
```Kotlin
implementation("com.github.Chiu-xaH.SharedNav:navigation-android:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation-jvm:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation-ios:<version>")
implementation("com.github.Chiu-xaH.SharedNav:navigation-wasmJs:<version>")
```

#### 编译为本地产物
这里以 Android 为例，其余平台类似。
```bash
./gradlew assembleRelease
```
在各自模块的`build/outputs/aar`目录找到 aar 产物，拷贝到要引入的应用模块下的`libs`文件夹中
```groovy
def tag = "release"
implementation(files("libs/common-${tag}.aar")) 
implementation(files("libs/navigation-${tag}.aar")) 
implementation(files("libs/shared-container-${tag}.aar")) 
implementation(files("libs/floating-window-${tag}.aar")) 
implementation(files("libs/shader-${tag}.aar"))
```

### 创建第一个 Destination

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

### 初始化导航宿主

在 Activity 或顶层 Composable 中启动导航：

```kotlin
val navController = rememberNavController(startDestination = HomeDestination)

SharedNavHost(
    navController = navController
)
```

### 页面跳转与返回

在任意 Composable 中通过 `LocalNavController` 或 `LocalNavControllerSafely` 获取控制器：

> **提示**：如果无法保证 Composable 函数一定在 `SharedNavHost` 下调用，请使用 `LocalNavControllerSafely`，它返回可空对象；`LocalNavController` 获取不到控制器时会直接抛出异常导致 Crash。

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current

    Button(
        onClick = { navController.push(DetailDestination) }
    ) {
        Text("进入详情")
    }

    Button(
        onClick = { navController.pop() },
    ) {
        Text("返回")
    }
}
```

### 添加容器共享动效

用 `SharedContainer` 包裹触发跳转的组件，`key` 与目标 `Destination.key` 保持一致，即可获得容器共享的展开/收起动画。

**写法 1：**

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val dest = remember(id) { DetailDestination(id) }

    SharedContainer(
        key = dest.key,
        shape = MaterialTheme.shapes.medium,
    ) {
        Card(
            shape = NoneRoundShape,
            onClick = { navController.push(dest) }
        ) {
            /* 内容 */
        }
    }
}
```

**写法 2：Modifier 扩展**

```kotlin
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val dest = remember(id) { DetailDestination(id) }

    Card(
        modifier = Modifier.sharedContainer(
            key = dest.key,
            shape = MaterialTheme.shapes.medium
        ),
        shape = NoneRoundShape,
        onClick = { navController.push(dest) }
    ) {
        /* 内容 */
    }
}
```

> **注意**：`SharedContainer` 内层组件的 `shape` 必须设置为无圆角，圆角统一由外层 `SharedContainer` 管理，否则在提取 1 像素时会缺失边角。

## [原理讲解](docs/Doc.md)

## [接口文档](docs/Developer.md)

## [后续安排](docs/Todo.md)

## [Pull Request 须知(参与本项目)](docs/Rule.md)

## 已接入的项目
[聚在工大](https://github.com/Chiu-xaH/HFUT-Schedule)

[小小账本](https://github.com/manykofeissssss/android-moneySave)
