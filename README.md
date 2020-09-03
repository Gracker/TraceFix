# AutoAddSystrace
为项目自动添加 Systrace Tag，方便使用 Systrace 进行代码 Debug

## 待更新
1. 允许自定义需要过滤的方法
2. 允许按照注解来进行 Systrace 的 Tag 插入

## Module
### app 
测试项目，引入了插件，可以在编译时在新加入的方法里面加入 Systrace 的 Tag

### buildSrc
插件目录，使用的 ASM 在编译时在代码入口和出口添加 TAG

### TestForAsm
主要是为了配合 Android ASM 的插件，来查看对应需要插入的代码的 ASM 格式

## Demo
下面是 App 冷启动的时候，抓的 Systrace
![Demo](/pic/systrace_demo.png)