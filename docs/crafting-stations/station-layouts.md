# 布局概述

布局决定了您的制作站的外观和功能，通过创建GUI来实现。每当您重新启动插件或运行以下命令时，布局将重新加载：**/mi reload stations**。

## 创建布局

每当您的制作站生成时，它们将全部使用默认布局。该文件夹中的每个YML文件对应一个布局，因此您可以通过创建一个新的YML文件来创建布局。选择文件名时要小心，因为它对应于您在创建站点时使用的ID。这些是自动生成的模板：
![auto templates files](https://i.imgur.com/N3D5bqh.png)

## 使用布局

这是一个制作站设置的示例。在布局节点下，您可以输入您要使用的布局ID。

``` yaml
# 打开制作站时显示的名称
name: '示例制作站 (#page#/#max#)'

# 制作队列中物品的最大数量，即玩家可以同时制作的物品数量。
# 必须在1到64之间。
max-queue-size: 10

# 每当在制作站中完成一个动作时播放的声音。
# 在此处获取声音名称：
# https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html
sound: ENTITY_EXPERIENCE_ORB_PICKUP

# 这是GUI的外观。您可以在crafting-stations/layouts中
# 创建一个新文件来定义自己的布局，文件名即为ID。
layout: expanded
```

## 默认布局

``` yaml
# GUI的大小。必须在9到54之间，并且必须是9的倍数。
slots: 54

layout:

  # 显示站点配方的槽位。
  recipe-slots: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 21, 22, 23]

  # 显示站点队列槽位的槽位。
  queue-slots: [38, 39, 40, 41, 42]

  # 显示用于导航站点配方的箭头的槽位。
  # 仅在可以使用时显示。
  recipe-previous-slot: 20

  recipe-next-slot: 24

  # 显示用于导航站点队列的箭头的槽位。
  # 仅在可以使用时显示。
  queue-previous-slot: 37

  queue-next-slot: 43
```
