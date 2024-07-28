package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.RequiredLevelData;
import net.Indyuce.mmoitems.stat.type.RequiredLevelStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public class RequiredLevel extends RequiredLevelStat {

    /**
     * Stat that uses a custom DoubleStatData because the merge algorithm is
     * slightly different. When merging two "required level", MMOItems should
     * only keep the highest levels of the two and not sum the two values
     */
    public RequiredLevel() {
        super("LEVEL", Material.EXPERIENCE_BOTTLE, "等级", new String[]{"您的物品需要使用的级别"});
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        final int level = item.getInteger(this.getNBTPath());
        if (level <= 0) return true;

        if (player.getLevel() >= level || player.getPlayer().hasPermission("mmoitems.bypass.level")) return true;

        if (message) {
            Message.NOT_ENOUGH_LEVELS.format(ChatColor.RED).send(player.getPlayer());
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
        }
        return false;
    }

	/*
	@NotNull
	@Override
	public StatData apply(@NotNull StatData original, @NotNull UpgradeInfo info, int level) {
		//UPGRD//MMOItems. Log("\u00a7a  --> \u00a77Applying Enchants Upgrade");

		// Must be RequiredLevelData
		if (original instanceof RequiredLevelData && info instanceof DoubleUpgradeInfo) {

			// Get value
			RequiredLevelData originalLevel = ((RequiredLevelData) original);
			DoubleUpgradeInfo dui = (DoubleUpgradeInfo) info;

			// For every enchantment
			int lSimulation = level;

			// Get current level
			double value = originalLevel.getValue();
			PlusMinusPercent pmp = dui.getPMP();

			// If leveling up
			if (lSimulation > 0) {

				// While still positive
				while (lSimulation > 0) {

					// Apply PMP Operation Positively
					value = pmp.apply(value);

					// Decrease
					lSimulation--;
				}

				// Degrading the item
			} else if (lSimulation < 0) {

				// While still negative
				while (lSimulation < 0) {

					// Reverse Operation
					value = pmp.reverse(value);

					// Decrease
					lSimulation++;
				}
			}

			// Update
			originalLevel.setValue(SilentNumbers.floor(value));

			// Yes
			return originalLevel;
		}

		// Upgraded
		return original;
	}	///*/

    @Override
    public @NotNull
    RequiredLevelData getClearStatData() {
        return new RequiredLevelData(0D);
    }
}
