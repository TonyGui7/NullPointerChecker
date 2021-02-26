# NullPointerChecker

## 概述

NullPointerChecker是一个编译期检测空指针的工具。



## 配置

在您的项目主工程目录下的build.gradle文件的buildscript和allprojects的repositories添加如下仓库依赖

```
maven {
   url "https://raw.githubusercontent.com/TonyGui7/maven-repo/master"
}
```

然后同样在该build.gradle文件的dependencies中添加如下class路径依赖

```
classpath 'com.npcheck:plugin:1.0.3'
```

其中版本号取最新的版本。

然后在主module或者您想检测的module目录下的build.gradle文件中添加如下插件依赖

```
apply plugin: 'com.npcheck.plugin'
```

最后在该文件的dependencies依赖下添加如下依赖

```
annotationProcessor 'com.npcheck:compiler:1.0.3'
implementation 'com.npcheck:annotations:1.0.3'
```

到这里，配置就结束了。



## 使用

在您想检测的java类添加@NPClassCheck注解，在编译时即可对该类进行检测。
检测结果直接在android studio控制台打印出来，如下图
![](https://github.com/TonyGui7/NullPointerChecker/blob/master/document_res/demoResult.jpg)
