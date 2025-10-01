# WinTouchKt
用于Windows触屏，提供虚拟触摸按键，有丰富的可配置选项。

旨在为Windows触屏玩家提供轻量、开源、高效的游戏键位软件。打包形式为体积仅2~3MB的单个exe文件。

# 框架
开发语言使用 kotlin/native，并使用少量c++完成对接。

使用 Direct 2D 渲染按钮，配置页面使用 Windows 原生控件(GDI)。

# 编译
在lib/Clib文件夹中使用vscode执行构建task，生成.o文件，在idea中重新构建cinterop，然后再编译。由于没找到免费的断点调试插件，所以只有release。
# UiAccess
参考了[uiaccess](https://github.com/killtimer0/uiaccess)的实现，使得窗口可以置于最顶层

  