# 开始安装

## 安装 MMOItems 于 1.14 及以上版本

将最新版本的 MythicLib 和 MMOItems 拖入你的服务器，并重启服务器。

## ~~在旧版（1.13）上安装 MMOItems Premium~~

自 4.7.6 版本以来，MMOItems Premium 已支持 1.12 版本，但 1.14+ 仍然是插件的原生版本。你需要下载 [1.12 默认配置文件](https://www.dropbox.com/s/7j9gowyd32wy9cv/legacy-configs-UPDATED.zip?dl=1)，并在安装了 MI 版本后首次启动服务器后手动安装它们。

如果你在旧版服务器上运行 WorldGuard，你 **必须** 安装 **MMOItems LegacyWG ([下载](https://github.com/mmopluginteam/mmoitems-legacywg/releases))**，否则 MMOItems 在加载时会报错并无法启用。

**最新版本的 MMOItems Premium (6.0.0+) 不再支持 1.12，请确保你的服务器保持最新版本以享受 MMOItems 的所有功能！**

## 附加组件：MMOMana ([下载](https://github.com/mmopluginteam/mmoitems-mana/releases))

MMOMana 是旧版 MMOItems Mana&Stamina 附加组件的最新、开源且持续维护的版本。它为玩家增加了魔力和体力资源，可以在不同的 MMOItems 机制中使用。

**MMOMana 不兼容已经处理魔力和体力的其他 RPG 核心插件。**

*注意:* MMOMana 提供的占位符与旧版 Mana&Stamina 附加组件相同！

* %mana_mana%
* %mana_stamina%
* %mana_mana_regen%
* %mana_stamina_regen%
* %mana_mana_bar%
* %mana_stamina_bar%
* %mana_max_mana%
* %mana_max_stamina%

## 附加组件：MI PerWorldDrops ([下载](https://github.com/mmopluginteam/mmoitems-perworlddrops/releases))

MMOItems PerWorldDrops 允许你为方块和怪物设定 **特定世界的掉落表**。你可以像使用 MMOItems 一样将此附加组件拖入插件文件夹。重启服务器后，MMOItems 插件文件夹中应出现一个 /drops 文件夹。在该文件夹中，你创建的每个 .yml 文件都对应一个特定的世界（文件名必须与世界名 **完全** 匹配）。有关如何设置这些 YAML 配置，请参考此 [维基页面](../item-management/item-drop-tables)。

此附加组件保持最新，对于不需要庞大或复杂掉落表插件（如 MMOCore 或 MythicMobs）的人来说，非常实用。
