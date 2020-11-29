漫品Android客户端

构建命令

```shell
./gradlew clean assembleDebug
```
or

```shell
./gradlew clean assembleRelease
```
github下载最新版本:https://github.com/AnyMarvel/ManPinAPP/releases

国内下载地址: https://www.pgyer.com/manpin


主要内容为以下三个板块:

#1. 小说模块
(基于客户端本地爬虫对多个网站内容进行爬取,轮训多个站点,找到你喜欢的内容为目的)

相对于原作增加的功能与改进:

1. 基于mvp架构进行代码布局,降低代码耦合度。
2. 采用 sql 数据库对数据进行存储。
4. 支持小说更新提示。(开发中)
5. 小说阅读器重构,提供的功能如下

   * 支持翻页动画:仿真翻页、覆盖翻页、上下滚动翻页等翻页效果。
   * 支持页面定制:亮度调节、背景调节、字体大小调节
   * 支持全屏模式(含有虚拟按键的手机)、音量键翻页
   * 支持页面进度显示、页面切换、上下章切换。
   * 支持在线章节阅读、本地书籍查找。
   * 支持本地书籍加载到页面(支持本地书籍分章、加载速度快、耗费内存少)

**注: 该项目已经上线,市场搜索 漫品 即可下载**

采用了以下开源框架

* [butterknife](https://github.com/JakeWharton/butterknife)    ==>    View注入
* [rxandroid](https://github.com/ReactiveX/RxAndroid)
* [rxjava](https://github.com/ReactiveX/RxJava)
* [greenDAO](https://github.com/greenrobot/greenDAO)    ==>    数据库
* [retrofit](https://github.com/square/retrofit)  ==> 网络
* [Glide](https://github.com/bumptech/glide)    ==>    图片加载


小说模块 Changlog 详情请点击 [漫品 小说模块 Changlog](Changlog/xiaoshuochanglog.md)

<font color=red>ps:小说模块本站展示内容均来自网络,如有侵权请联系删除,本站只做技术学习,切勿作为商业使用</font>

目前漫品客户端支持小说网站:

- [零点读书](https://www.lingdiankanshu.co)
- [梧桐中文网](http://www.wzzw.la)
- [ABC小说网](https://www.yb3.cc)
- [文学馆](https://www.xwxguan.com)
- [九桃小说](https://www.9txs.com/)

持续更新中

<div align="center">
<img src="pictures/xiaoshuo01.jpeg" width="150" height="300"/> <img src="pictures/xiaoshuo02.jpeg" width="150" height="300"/> <img src="pictures/xiaoshuo03.jpeg" width="150" height="300"/> <img src="pictures/xiaoshuo04.jpeg" width="150" height="300"/><img src="pictures/xiaoshuo05.jpeg" width="150" height="300"/>
</div>

#2. 漫画模块

基于tensorflow生成漫画,内置3000+种模板,基于人脸识别和目标识别分析等多种手段自动拼接漫画,效果如图:

<div align="center">
<img src="pictures/manhua.png" width="150"/> <img src="pictures/manhua_1.png" width="150"/> <img src="pictures/manhua_2.png" width="150"/> <img src="pictures/manhua_3.png" width="150"/> <img src="pictures/manhua_4.png" width="150"/>
</div>

#3. 明信片

本模块提供多种模板提供用户使用如图,效果如图

<div align="center">
<img src="pictures/pic_template2.jpg" width="150" height="250"/> <img src="pictures/pic_template3.jpg" width="150" height="250"/> <img src="pictures/pic_template5.jpg" width="150" height="250"/> <img src="pictures/pic_template6.jpg" width="150" height="250"/> <img src="pictures/pic_template8.jpg" width="150" height="250"/>
</div>


<font color="red">此app作为开源项目，搭载阿里云低性能服务器及OSS存储，用于照片存储和功能渲染，续费本人已经很吃力，望各位大大不要攻击。</font>

各个模块架构,实现细节等都会逐步补充,期待你的start

Android开发内容总结,主要针对Android技术,界面,底层,性能优化等方面进行的自我总结

更多技术细节,请参考如下技术总结


[https://androidsummary.gitbook.io/androidsummary/](https://androidsummary.gitbook.io/androidsummary/)
