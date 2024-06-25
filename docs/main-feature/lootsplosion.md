# 掉落物飞溅

_Lootsplosion_ 是我们模仿其他游戏（如《无主之地》系列）的一种效果。它可以让击杀怪物后的掉落物不再无聊地掉在地上，而是通过爆炸飞溅的方式展示掉落物品！

``` image
# Offset 是在X和Y坐标上移动的距离
# Height 是Y轴上的速度坐标。Lootsplosions
# 只在MythicMobs怪物身上触发。
lootsplosion:
    enabled: true
    color: true
    offset: .4
    height: .5
```

您可以在config.yml中进行配置，改变物品从怪物身上飞出的X和Y轴变动范围。现在您可以击杀Boss，让大量发光的物品飞散开来，供玩家拾取！

要为某个MythicMob启用Lootsplosion，您需要给它一个`Lootsplosion`变量。值可以是任意的，例如1。此技能在怪物生成时将变量应用于怪物：
`setvariable{var=Lootsplosion;scope=target;value=1} @self ~onSpawn`

![image](https://i.imgur.com/oSUizuE.gif)

---
Lootsplosion完全兼容MMOItems的等级。当使用MythicMobs掉落表掉落**有等级**的物品时，掉落的物品会留下颜色取决于其等级的粒子轨迹！再加上MMOItems的提示和发光系统，您将获得漂亮的掉落效果！

![image](https://i.imgur.com/8dL2Nqr.gif)

要使物品具有Lootsplosion粒子效果，只需在物品等级的MMOItems配置文件中设置物品等级颜色，如下所示：

``` yaml
RARE:
    name: '&6&lRARE'
    unidentification:
        ...
    item-glow:
        color: 'ORANGE'
```

在这个例子中，稀有物品在MythicMobs掉落时将显示橙色粒子效果。
