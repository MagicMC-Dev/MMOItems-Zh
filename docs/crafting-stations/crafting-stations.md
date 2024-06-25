# 合成站概述

合成站使玩家能够通过使用合成配方轻松合成特定物品，这些配方需要材料和条件。打开合成站时，玩家可以查看有关合成站配方的信息，包括配方条件（权限、等级）、材料和输出，并使用分页系统浏览它们。

您可以使用以下命令使玩家打开合成站GUI：`/mi stations open {station-id} {player}`。您可以使用`/mi stations list`检查可用合成站的列表。

## 创建新的合成站

合成站保存在/MMOItems/crafting-stations文件夹中。该文件夹中的每个YML文件对应一个合成站，因此您可以通过创建一个新的YML文件来创建合成站。选择文件名时要小心，因为它对应于您在通过命令为玩家打开GUI时使用的ID，如前所述。

![image](https://i.imgur.com/uaXobmx.png)

## 配置合成站

``` yaml
# 打开合成站时显示的名称
name: '奥术熔炉 (#page#/#max#)'
max-queue-size: 10
sound: ENTITY_EXPERIENCE_ORB_PICKUP
layout: default

# 在此处配置GUI物品
items:
    fill:
        material: PINK_STAINED_GLASS_PANE
        name: '&a'
    no-recipe:
        material: GRAY_DYE
        name: '&a'
```

每个合成站需要在站点配置文件中提供一些基本信息，包括`name`选项，它对应于GUI名称。`items`配置部分用于更改一些GUI物品。`fill`物品用于填充配方物品周围的剩余GUI槽位。`no-recipe`物品用于在没有配方时填充GUI配方槽位。上述配置将显示如下：

![image](https://i.imgur.com/nGXH8GG.png)

## 创建合成配方

第一步是在站点配置中创建`recipes`配置部分。然后，您需要为您的站点配方创建一个配置部分。配置部分名称对应于配方ID，仅在插件内部使用。您在这里输入的内容无关紧要，只需**确保**不同的配方具有不同的配方ID。

在本指南中，我们将为钢剑创建一个配方，要求玩家至少达到5级。该配方需要两个特定的玩家权限，一些钢锭和原版木棍作为材料。

``` yaml
# 合成站配方
recipes:
    steel-sword: {}
```

### 设置配方输出

这是玩家在使用您的配方时将获得的MMOItem。由于我们希望玩家获得1把钢剑（其MMOItems类型为`SWORD`，ID为`STEEL_SWORD`），因此`output`部分应如下所示：

``` yaml
recipes:
    steel-sword:
        output:
            type: SWORD
            id: STEEL_SWORD
            amount: 1
```

### 合成时间

配方还可以有一个合成时间，这是玩家合成物品所需的时间（以秒为单位）。在GUI中单击配方物品时，玩家将暂时退出GUI，并使用原版标题功能显示进度条，直到合成完成。要设置**配方合成时间**，请使用以下配置模板：

``` yaml
recipes:
    steel-sword:
        crafting-time: 10 # 时间以秒为单位
```

### 使用/领取/取消配方时执行特定操作

请先阅读[这个Wiki页面](../crafting-stations/recipe-triggers)以了解合成触发器。使用触发器，您可以在与配方交互时执行特定操作。

设置这些触发器时，有三种类型的交互可以监听，以修改配方的行为。如果您的配方不是即时的，您必须与其交互两次：第一次单击配方，这会消耗所有材料并将结果放入合成队列一段时间（这种交互称为`use`），然后在合成队列中领取配方输出（称为`claim`）。如果您想要取回材料并停止合成物品，可以`cancel`合成并将物品从队列中移除。

``` yaml
recipes:
    steel-sword:
        ....
        on-use: # 使用配方时调用
        - 'message{format="您正在合成钢剑"}'
        - .....
        triggers: # 领取物品时调用
        - 'message{format="您刚刚领取了一把钢剑"}'
        - .....
        on-cancel: # 取消任何该配方合成时调用
        - 'message{format="您不再合成钢剑"}'
        - .....
```

注意，不要将`triggers`选项与`on-use`选项混淆。某些配方有延迟，因此在使用配方后不会立即获得物品，而是在X秒后获得。即时配方会同时触发两个列表。

触发器可以与[配方条件](../crafting-stations/recipe-conditions)一起使用来处理虚拟材料，例如货币。您可以有一些占位符条件，如`%yourplugin_coins% >= 50`，检查玩家是否至少有50单位的某种货币，并在玩家使用配方时调用触发器，扣除玩家的50单位货币。在这种情况下，如果玩家想取消合成，您还需要将货币退还给玩家。

### 额外选项

配方选项是您可以为每个配方设置的独特设置。它们决定配方在站点中的功能。

``` yaml
recipes:
    steel-sword:
        options:
            output-item: false
            hide-when-locked: false
            silent-craft: true
```

| 选项     | 键                        | 描述                  |
| ------ | ------------------------ | ------------------- |
| 输出物品   | output-item              | 是否将输出物品给予玩家。        |
| 锁定时隐藏  | hide-when-locked         | 如果玩家不满足条件，配方将不会显示。  |
| 无材料时隐藏 | hide-when-no-ingredients | 如果玩家没有所有资源，配方将不会显示。 |
| 静默合成   | silent-craft             | 合成物品时不会发出声音。        |

## 合成站示例

``` yaml
name: '钢铁合成站 (#page#/#max#)'
max-queue-size: 10
sound: ENTITY_EXPERIENCE_ORB_PICKUP
layout: default
items:
    fill:
        material: AIR
    no-recipe:
        material: GRAY_STAINED_GLASS_PANE
        name: '&a'
    no-queue-item:
        material: GRAY_STAINED_GLASS_PANE
        name: '&a队列中没有物品'
recipes:
    two-handed-steel-sword:
        output:
            type: GREATSWORD
            id: TWO_HANDED_STEEL_SWORD
            amount: 1
        crafting-time: 10
        conditions:
        - 'level{level=8}'
        ingredients:
        - 'mmoitem{type=MATERIAL,id=STEEL_INGOT,amount=8}'
        - 'vanilla{type=STICK,amount=4}'
```

![image](https://i.imgur.com/XRmuskj.png)
