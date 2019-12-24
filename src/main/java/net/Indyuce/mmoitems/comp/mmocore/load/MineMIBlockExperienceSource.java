package net.Indyuce.mmoitems.comp.mmocore.load;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomBlock;

public class MineMIBlockExperienceSource extends SpecificExperienceSource<Integer> {
	public final int id;
	private final boolean silkTouch;
	private final boolean playerPlaced;

	public MineMIBlockExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("id");
		id = config.getInt("id", 1);
		silkTouch = config.getBoolean("silk-touch", true);
		playerPlaced = config.getBoolean("player-placed", false);
	}

	@Override
	public ExperienceManager<MineMIBlockExperienceSource> newManager() {
		return new ExperienceManager<MineMIBlockExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGHEST)
			public void a(BlockBreakEvent event) {
				if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.SURVIVAL)
					return;
				PlayerData data = PlayerData.get(event.getPlayer());
				
				for (MineMIBlockExperienceSource source : getSources())
				{
					if (!MMOItems.plugin.getCustomBlocks().isMushroomBlock(event.getBlock().getType()))
						continue;
					if (source.silkTouch && hasSilkTouch(event.getPlayer().getInventory().getItemInMainHand()))
						continue;
					if ((!source.playerPlaced) && event.getBlock().hasMetadata("player_placed"))
						continue;

					if (source.matches(data, CustomBlock.getFromData(event.getBlock().getBlockData()).getId()))
						source.giveExperience(data, event.getBlock().getLocation());
				}
			}
		};
	}

	private boolean hasSilkTouch(ItemStack item) {
		return item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}

	@Override
	public boolean matches(PlayerData player, Integer blockId) {
		return id == blockId && hasRightClass(player);
	}
}
