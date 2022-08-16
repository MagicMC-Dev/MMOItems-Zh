package net.Indyuce.mmoitems.comp.rpg;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelDownEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.DisableStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class McMMOHook implements RPGHandler, Listener {

	/**
	 * McMMO is a special plugin, it can be used along with other RPG plugins
	 * like MMOCore. That stat must be registered even if McMMO is not the main
	 * RPG core plugin, therefore the register() method is on the onEnable() and
	 * not in the constructor of that class
	 */
	public static final ItemStat disableMcMMORepair = new DisableStat("MCMMO_REPAIR", Material.IRON_BLOCK, "Disable McMMO Repair",
			"Players can't repair this with McMMO.");

	@EventHandler(ignoreCancelled = true)
	public void a(McMMOPlayerLevelUpEvent event) {
		PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@EventHandler(ignoreCancelled = true)
	public void b(McMMOPlayerLevelDownEvent event) {
		PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new McMMOPlayer(data);
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	public static class McMMOPlayer extends RPGPlayer {
		public McMMOPlayer(PlayerData playerData) {
			super(playerData);
		}

		@Override
		public int getLevel() {
			//RPG*/MMOItems. Log("Getting level of \u00a7c" + getPlayer().getName());

			// No errors, right?
			try {

				// Get through Experience API I suppose
				int r = ExperienceAPI.getPowerLevel(getPlayer());

				// Log rq
				//RPG*/MMOItems. Log("\u00a76  + \u00a77Found level as \u00a7c" + r);

				// thats it
				return r;

			// A problem may have occured
			} catch (McMMOPlayerNotFoundException exception) {

				// Log rq
				//RPG*/MMOItems. Log("\u00a76  - \u00a77No data found. Using \u00a7c0");

				// Thats it
				return 0;
			}
		}

		@Override
		public String getClassName() {
			return "";
		}

		@Override
		public double getMana() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public double getStamina() {
			return 0;
		}

		@Override
		public void setMana(double value) {
			getPlayer().setFoodLevel((int) value);
		}

		@Override
		public void setStamina(double value) {
		}
	}
}