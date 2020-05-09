package net.Indyuce.mmoitems.comp.mmocore;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Attribute;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MMOCoreHook implements RPGHandler, Listener {

	private static final ItemStat MANA_REGENERATION = new DoubleStat("MANA_REGENERATION", VersionMaterial.LAPIS_LAZULI.toItem(), "Mana Regeneration",
			new String[] { "Increases mana regen." });
	private static final ItemStat MAX_STAMINA = new DoubleStat("MAX_STAMINA", VersionMaterial.LIGHT_BLUE_DYE.toItem(), "Max Stamina",
			new String[] { "Adds stamina to your max stamina bar." });
	private static final ItemStat STAMINA_REGENERATION = new DoubleStat("STAMINA_REGENERATION", VersionMaterial.LIGHT_BLUE_DYE.toItem(),
			"Stamina Regeneration", new String[] { "Increases stamina regen." });
	private static final ItemStat ADDITIONAL_EXPERIENCE = new DoubleStat("ADDITIONAL_EXPERIENCE", VersionMaterial.EXPERIENCE_BOTTLE.toItem(),
			"Additional Experience", new String[] { "Additional MMOCore main class experience in %." });
	private static final ItemStat HEALTH_REGENERATION = new DoubleStat("HEALTH_REGENERATION", new ItemStack(Material.BREAD), "Health Regeneration",
			new String[] { "Increases MMOCore health regen.", "In %." });

	/*
	 * called when MMOItems enables
	 */
	public MMOCoreHook() {

		MMOItems.plugin.getStats().register(HEALTH_REGENERATION);
		MMOItems.plugin.getStats().register( MANA_REGENERATION);
		MMOItems.plugin.getStats().register( MAX_STAMINA);
		MMOItems.plugin.getStats().register( STAMINA_REGENERATION);
		MMOItems.plugin.getStats().register( ADDITIONAL_EXPERIENCE);

		/*
		 * only works when the server is reloaded. needs /reload when changing
		 * attributes to refresh MMOItems stats
		 */
		for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
			MMOItems.plugin.getStats().register(new Required_Attribute(attribute));
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new MMOCoreRPGPlayer(data);
	}

	@EventHandler
	public void updateInventoryOnLevelUp(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void updateInventoryOnClassChange(PlayerChangeClassEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void updateInventoryOnPlayerDataLoad(PlayerDataLoadEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	public class MMOCoreRPGPlayer extends RPGPlayer {
		private final PlayerData data;

		public MMOCoreRPGPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
			super(playerData);

			data = PlayerData.get(playerData.getPlayer());
		}

		public PlayerData getData() {
			return data;
		}

		@Override
		public int getLevel() {
			return data.getLevel();
		}

		@Override
		public String getClassName() {
			return data.getProfess().getName();
		}

		@Override
		public double getMana() {
			return data.getMana();
		}

		@Override
		public double getStamina() {
			return data.getStamina();
		}

		@Override
		public void setMana(double value) {
			data.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			data.setStamina(value);
		}

		@Override
		public void giveMana(double value) {
			data.giveMana(value);
		}

		@Override
		public void giveStamina(double value) {
			data.giveStamina(value);
		}
	}
}