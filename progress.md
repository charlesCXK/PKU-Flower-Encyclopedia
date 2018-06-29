# Progress of project

#### 2018.05.22 Updated

1. 写好了跳转函数。从主界面的按钮点击可跳转

   1. 花卉识别：
      跳转目的界面的布局文件路径为

      PKU-Flower-Encyclopedia/MyApplication/app/src/main/res/layout/activity_identify.xml

      跳转之后执行的代码路径为/Users/coder_cxk/Desktop/PKU-Flower-Encyclopedia/MyApplication/app/src/main/java/com/example/coder_cxk/myapplication/identify.java

   2. 花卉百科：
      跳转目的界面的布局文件路径为

      PKU-Flower-Encyclopedia/MyApplication/app/src/main/res/layout/activity_baike.xml
      跳转之后执行的代码路径为/Users/coder_cxk/Desktop/PKU-Flower-Encyclopedia/MyApplication/app/src/main/java/com/example/coder_cxk/myapplication/baike.java

2. 可以完善上面的两个java文件，以及xml文件，实现子模块的布局



#### 2018.05.26 Updated

1. 添加了滑动侧边栏，以及里面item的跳转功能
2. 文件说明
   1. 主界面两个按钮，点击分别跳转到 identify.java 以及 baike.java，这两个文件路径见上方。下面仅以identify为例，解释文件位置。
   2. 这个模块分为如下几部分：toolbar，侧滑栏，正中央的主内容。文件在layout/activity_identify。
      1. toolbar是最上面的，有两个按钮和一个文字，文件为layout/app_bar_identify.xml
      2. 主界面：layout/content_identify.xml
      3. 侧滑栏：分为头部和菜单栏。头部是侧滑栏上面的部分，一个图标一个文字。layout/nav_header_identify.xml。菜单栏可以自己设计，内容在menu/activity_identify_drawer.xml中指定
3. **需要继续做的部分**：完善主界面：layout/content_identify.xml，建议其中用fragment布局，这样的话可以动态替换主界面内容，而共用toolbar以及侧滑栏。
4. **需要注意的地方**：identify和baike采用相同的侧滑栏，但是这是两个，更新的时候需要同步更新。 




#### 2018.06.07 Updated

- 更新了百科界面，调整了布局（图片+说明），以小卡片的形式呈现。增加了四个花种。图片格式均为220*165。
- 文件说明：
  - MyApplication/app/src/main/res/drawable/baike_flower_frame.xml
     百科界面小卡片的背景格式（白色、弧度等）
  - MyApplication/app/src/main/res/drawable/TempFile/
     用python修改图片的size，统一化。
  - MyApplication/app/src/main/res/drawable/flow1.png 这些事花卉的图片。
- To Do: 百科界面内容差不多是这样。有空需要改一下上面bar的格式，现在有点丑。



#### 2018.06.19 Updated

- 修改了baike界面上面的bar里面的文字($Title$字段)
- 添加了几种花卉的图片



#### 2018.06.28 Updated

- 增加了**每日猜花**界面(一个新的Activity)
- 在百科和识花界面的侧边栏增加了 **每日猜花**项
- TO DO：现在只有一种花，以后考虑添加 **随机选择** 按钮，切换花的图片