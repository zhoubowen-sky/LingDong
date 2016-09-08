
## "灵动快传"项目简介

-  本项目为 第五届[“中国软件杯”][2]——大学生软件设计大赛 [A6赛题][1] 的参赛作品
-  开发团队为来自湖北大学的“根号三”队伍，队员有 **周博文**、**郑志琦**、**董致礼** 
-  凭借此作品，我们 **“根号三”** 团队获得**全国二等奖**以及**最佳表现奖**的成绩
-  项目总共分为三个模块，分别为Android端、PC端以及Web端
-  以下将分别从这三个模块进行较为详细的介绍

### “灵动快传”Android端介绍

> 本项目全称为：互联网多平台文件快传系统——灵动快传
> 
> 开发人员：[周博文][7] [郑志琦][6] [董致礼][8]
> 
> 集成开发工具：Android Studio 2.1.2 
> 
> 开发语言：Java

#####Android端APP的UI展示

![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/loading.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/cebianlan.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/chuansong.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/wenjianguanli.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/yaokongdiannao.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/main.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/feedback.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/trans.jpg)
![image](https://github.com/zhoubowen-sky/LingDong2.0/blob/master/github-images-folder/offlinetrans.jpg)

#####Android端APP程序相关说明

> 程序目录中 **github-images-folder** 文件夹与APP程序无关，里面的图片为Github项目README页面的说明


#####Android端APP功能相关说明

> **“面对面快传”**，这是APP的核心功能，是一个可以在任何网络情况下，不消耗任何流量在两台Android手机之间或者Android手机与PC之间互传文件
> 
> **“蓝牙传输”**，可以调用系统的蓝牙模块发送文件
> 
> **“文件管理”**，文件浏览功能，如文件的剪贴、复制、粘贴、重命名、删除、属性查看等的功能
> 
> **“离线文件”**，借助搭建好的云服务器，用户可以在Android、PC、Web三平台之间任意的上传文件与下载文件
> 
> **“遥控电脑”**，APP与PC建立连接后，可以遥控电脑，如关机、播放APP、关闭PC当前窗口、打开程序等等
> 
> **"文字剪贴板"**，Android端开启此项服务后，在手机任何位置选取一段文字，并将其“复制”，此时PC端直接鼠标右键“粘贴”即可
> 
> **“大数据分析”**，我们收集了一定量的用户APP的使用行为数据，并将其上传到服务器，借助百度开源的数据可视化工具Echarts做了在网页上做了可视化的分析
> 
> **“用户反馈”**，用户在反馈页面提交了反馈信息，数据就会存储到服务器后台的数据库里面


#####Android端APP简要使用说明

>此项目的核心功能是实现Android之间、Android与PC之间在任意网络情况下均可互联传递文件
>
>Android使用的时候，一方点“创建连接”，另一方点“搜索加入”即可
>
>Android端使用的时候，无需管当前的网络状况，APP会自动识别并选择最合理的数据传输路径，如果两台手机在同一个路由器下面连接着，数据则会走路由器通道，如果没有在同一个 路由器下面，则会一方开启手机热点，另一方自动连接上它的热点，这一切都是全自动实现的
>
>Android与PC连接的时候，同样，无论在不在同一个路由器下均可建立连接，建立连接的过程与上述也是差不多的，同一路由器下如果没有设备，Android将会开启热点，PC连接上此热点，PC开热点，Android连接也是可以的
>



### “灵动快传”Android端介绍

> [点击访问Android端介绍][3]

### “灵动快传”Web端介绍

> [点击访问Web端介绍][4]

### “灵动快传”PC端介绍

> [点击访问PC端介绍][5]

####关于我们

>此项目为湖北大学 **周博文**、**郑志琦**、**董致礼**三人合力开发。
>联系方式：
>董：yhinu@qq.com
>郑：664837069@qq.com
>周：zhoubowen.sky@qq.com





[1]:http://www.cnsoftbei.com/bencandy.php?fid=130&aid=1379
[2]:http://www.cnsoftbei.com/
[3]:https://github.com/zhoubowen-sky/LingDong2.0/blob/master/README.md
[4]:https://github.com/zhoubowen-sky/LingDongWeb/blob/master/README.md
[5]:https://github.com/zhoubowen-sky/File-Transmit-pc/blob/master/README.md

[6]:https://github.com/ZhengZhiQI
[7]:https://github.com/zhoubowen-sky
[8]:https://github.com/yhinu