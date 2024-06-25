# 自定义方块生成

世界生成模板用于使您的自定义方块在新生成的世界中生成。自定义方块会以随机数量的矿脉形式生成。它们可能只在特定的生物群系列表中生成。生成模板还有更多具体选项，这篇wiki页面将概述这些内容。

**如果您希望自定义方块在您的世界中生成，请确保在主MMOItems配置文件中启用`custom-blocks.enable-world-gen`选项！**

## 基本生成模板示例

``` yaml
basic-template:
    replace: [STONE]
    chunk-chance: 0.7
    depth: -64=255
    vein-size: 5
    vein-count: 2
```

当世界正在生成时，每个区块都有一定的几率被选择以生成方块（`chunk-chance`）。您还可以配置方块的放置深度。`replace`选项定义了您的自定义方块将替换哪些原版方块。这意味着自定义方块不能在空中生成，这样自定义矿石可以像原版矿石一样在石墙中生成。

`vein-count`是将在选定区块内生成的矿脉数量，`vein-size`是每个矿脉中生成的自定义方块数量。

## 具体选项

使用`slime-chunk`选项，使您的方块只能在粘液区块中生成。

``` yaml
template-id:
    slime-chunk: true
```

使用`bordering`或`not-bordering`选项，使您的方块只有在特定方块围绕自定义方块的条件满足时才生成。

``` yaml
end-debris:
    replace: [END_STONE]
    chunk-chance: 0.4
    depth: 0=255
    vein-size: 2
    vein-count: 20
    bordering:
    - AIR
    worlds:
    - world_the_end
```

在这个例子中，`end-debris`只有当其一面接触空气时才会生成。

## 生物群系/世界黑/白名单

通过在`worlds`列表中添加条目，您可以将任何自定义方块限制在特定的世界集合中。如果在世界名称前使用`!`，世界白名单将变成黑名单，自定义方块将生成在除指定世界以外的任何地方。

``` yaml
template-id:
    worlds:
    - world_nether # 就像下界石英一样
```

格式对于生物群系是相同的，只需使用`biomes`代替`worlds`并输入生物群系名称（[spigot文档](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html)）。

``` yaml
template-id:
    biomes:
    - mountains # 就像绿宝石一样
```
