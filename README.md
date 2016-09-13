# codeplus

标签： eclipse mybatis

***
mybatis是一个流行的ORM工具，其有一个特点：将sql抽取出来单独在xml文件中配置。 正是这一点导致了：
1. 在实际开发过程中，写完了dao类后需要手工打开对应的xml文件继续开发；
2. review代码时需要打开对应的xml文件才能看到实际的sql；

当项目文件越来越多时，以上操作越发显得繁琐。为了解决这一问题，我写了一个eclipse插件，取名codeplus，希望它能为编码工作带来方便。
***
## 安装方法
把jar包放到eclipse所在位置的dropins子文件夹(若没有可以新建一个)中，重启eclipse即可。
## 如何使用
1. 在以dao命名结尾的类中，当鼠标在sqlId的文本上悬停时，会弹出弹层，显示出实际的sql，如图所示：
![鼠标悬停](https://raw.githubusercontent.com/huangice/images/master/screenshots/codeplus-hover.png)
(由于第一次需要扫描工程中所有的xml文件，所以第一次弹出会稍慢一些，之后就会很快了。)

这个弹层下有两个按钮，第一个按钮点击会打开对应的xml文件并定位到对应的行；第二个按钮点击则会复制sql到剪切板。
2. 在以dao或mapper命名结尾的类中，在任意位置右键点击，可以看到弹出的右键菜单中有一个选项：Open mybatis sql xml,如图：
![右键](https://raw.githubusercontent.com/huangice/images/master/screenshots/codeplus-menu.png)

点击会打开对应的xml文件。如果在sqlId的文本上右键时，会同时定位到对应的sql行。
  