# 配方材料

制作配方时，最重要的部分是配方材料（任何玩家必须拥有的物理物品才能使用配方）。配方材料存储在配方配置部分的一个列表中。有多种类型的配方材料，默认的有：

- 使用MMOItems生成的物品
- 带有自定义显示名称的原版物品（非MMOItems生成）
- 任何来自MythicLib UIFilters支持的插件中的物品

![image](https://i.imgur.com/jECX7PW.png)

## 示例

例如，制作一把银剑所需的材料如下：4个钢锭（使用MMOItems生成的物品）和2根原版木棍。

```yaml
recipes:
    steel-sword:
        ingredients:
        - 'mmoitem{type=MATERIAL,id=STEEL_INGOT,amount=4}'
        - 'vanilla{type=STICK,amount=2}'
```

## 可能的材料列表

| 材料      | 用法                                             |
|------------|---------------------------------------------------|
| MMOItem    | `mmoitem{type=..,id=..,amount=..,level=..,display=".."}`     |
| Vanilla    | `vanilla{type=..,name="..",amount=..,display=".."}` |

括号内的参数是可选的。`GUI-name`参数对应配方GUI中材料列表中显示的名称。对于原版材料，将`display-name`参数设置为`.`表示没有显示名称。游戏中的材料列表应如下所示：

对于MMOItems材料，还有一个选项是物品等级。您可以通过在材料行配置中添加`level=..`，使MMOItems材料在配方中具有最低等级要求。

要为材料添加物品等级范围，在数字后面添加两个点：`2..`表示2及以上 & `2..6`表示限制在等级2到6的物品。

材料的等级和显示选项都不是必需的。

**请确保不要使用两个相同的物品！这是由于制作站设计的一个小限制——您不能让配方要求例如10个铁锭 + 10个铁锭。这些材料需要合并为一个，即20个铁锭。**

### MythicLib UIFilters

MythicLib UIFilters允许任何外部插件轻松接入这些制作站以及工作台配方：

```yaml
recipes:
  mythic-mobs-ice-skates:
    output:
   
      # 使用MythicMobs UIFilter生成神话物品，"mm <MythicItem> ~ <Amount>"
      # 如果需要，也可以生成原版物品 "v <Material> ~ <Amount>"
      item: mm IceSkates ~ 3

    crafting-time: 10
    ingredients:

    # 请求原版物品的另一种方法 "v <Material> ~"
    - vanilla{type="v LEATHER ~",amount=4}

    # 请求MMOItems的另一种方法 "m <TYPE> <ID>"
    - vanilla{type="m MATERIAL WARM_FELT",amount=2}

    # 这里也可以请求MythicItems "mm <MythicItem> ~"
    - vanilla{type="mm Skates ~",amount=2}
```
