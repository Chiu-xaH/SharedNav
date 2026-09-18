# TODO 清单
1. floating-window 并入到 navigation 中，以解决 #8、#9
- 目前问题：navigation 中的临时keepAlive机制还尚未完善，多层开启的时候存在一些瑕疵。
- 优先级：High Refactor
2. shared-container 与 navigation 之间减少耦合度，使用模块间通信，目前是 navigation 主动向 shared-container 通信，询问是否接管动画，接管则转交给 shared-container，否则由 navigation 完成，整体思路不变，把模块强耦合优化一下。
- 目前问题：逻辑上没问题，只是太耦合了，缺乏重构动力。
- 优先级：Low Refactor
3. iOS 26 同款着色器扭曲
- 优先级：Low Feature
4. navigation 支持更自由的 EffectModifier- 目前问题：每次想新增效果，都需要在 PageEffect 里新开辟参数，比较不方便其他开发者自由定制。
- 优先级：Medium Refactor
5. 手势拖拽
- 优先级：Low Feature
7. 大屏适配
- 优先级：Low Feature
8. navigation 和 shared-container 支持自由传入动画曲线插值器，而不是钉死在二次贝塞尔曲线- 目前问题：改后不方便实时调参，存在一定的取舍，而且需要将现在的二次贝塞尔曲线转为插值器。
- 优先级：Medium Refactor
9. shared-container 的二次贝塞尔路径插值器存在一瑕疵，没有以前的版本灵动了。
- 优先级：High Bug
10. shared-container 在着色器关闭时，路径存在偏移瑕疵
- 优先级：High Bug
11. iOS14小组件翻转效果实现
- 优先级：Low Feature