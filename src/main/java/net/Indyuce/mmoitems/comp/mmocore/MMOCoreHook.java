package net.Indyuce.mmoitems.comp.mmocore;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Attribute;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class MMOCoreHook implements RPGHandler, Listener, DamageHandler {

	private final ItemStat manaRegen = new DoubleStat(VersionMaterial.LAPIS_LAZULI.toItem(), "Mana Regeneration", new String[] { "Increases mana regen." }, "mana-regeneration");
	private final ItemStat maxStamina = new DoubleStat(VersionMaterial.LIGHT_BLUE_DYE.toItem(), "Max Stamina", new String[] { "Adds stamina to your max stamina bar." }, "max-stamina");
	private final ItemStat staminaRegen = new DoubleStat(VersionMaterial.LIGHT_BLUE_DYE.toItem(), "Stamina Regeneration", new String[] { "Increases stamina regen." }, "stamina-regeneration");
	private final ItemStat cooldownReduction = new DoubleStat(new ItemStack(Material.BOOK), "Skill Cooldown Reduction", new String[] { "Reduces cooldowns of MMOCore skills (%)." }, "skill-cooldown-reduction");
	private final ItemStat additionalExperience = new DoubleStat(new ItemStack(Material.EXPERIENCE_BOTTLE), "Additional Experience", new String[] { "Additional MMOCore main class experience in %." }, "additional-experience");

	/*
	 * called when MMOItems enables
	 */
	public MMOCoreHook() {

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		/*
		 * register custom damage
		 */
		MMOCore.plugin.damage.registerHandler(new MMOItemsDamageHandler());
		MMOItems.plugin.getDamage().registerHandler(this);

		MMOItems.plugin.getStats().register("MANA_REGENERATION", manaRegen);
		MMOItems.plugin.getStats().register("MAX_STAMINA", maxStamina);
		MMOItems.plugin.getStats().register("STAMINA_REGENERATION", staminaRegen);
		MMOItems.plugin.getStats().register("SKILL_COOLDOWN_REDUCTION", cooldownReduction);
		MMOItems.plugin.getStats().register("ADDITIONAL_EXPERIENCE", additionalExperience);

		/*
		 * only works when the server is reloaded. needs /reload when changing
		 * attributes to refresh MMOItems stats
		 */
		for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
			MMOItems.plugin.getStats().register("REQUIRED_" + attribute.getId().toUpperCase().replace("-", "_"), new Required_Attribute(attribute));

		MMOItems.plugin.getUpgrades().reload();
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return MMOCore.plugin.damage.hasDamage(entity);
	}

	@Override
	public DamageInfo getDamage(Entity entity) {
		net.Indyuce.mmocore.comp.rpg.damage.DamageInfo info = MMOCore.plugin.damage.getDamage(entity);
		return new DamageInfo(info.getValue(), info.getTypes().stream().map((type) -> DamageType.valueOf(type.name())).collect(Collectors.toList()));
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
		PlayerData rpgdata = PlayerData.get(data.getPlayer());
		rpgdata.getStats().getInstance(StatType.HEALTH_REGENERATION).addModifier("MMOItems", data.getStats().getStat(ItemStat.REGENERATION));
		rpgdata.getStats().getInstance(StatType.MAX_MANA).addModifier("MMOItems", data.getStats().getStat(ItemStat.MAX_MANA));
		rpgdata.getStats().getInstance(StatType.MANA_REGENERATION).addModifier("MMOItems", data.getStats().getStat(manaRegen));
		rpgdata.getStats().getInstance(StatType.MAX_STAMINA).addModifier("MMOItems", data.getStats().getStat(maxStamina));
		rpgdata.getStats().getInstance(StatType.STAMINA_REGENERATION).addModifier("MMOItems", data.getStats().getStat(staminaRegen));

		rpgdata.getStats().getInstance(StatType.COOLDOWN_REDUCTION).addModifier("MMOItems", data.getStats().getStat(cooldownReduction));
		rpgdata.getStats().getInstance(StatType.ADDITIONAL_EXPERIENCE).addModifier("MMOItems", data.getStats().getStat(additionalExperience));

		rpgdata.getStats().getInstance(StatType.SKILL_DAMAGE).addModifier("MMOItems", data.getStats().getStat(ItemStat.SKILL_DAMAGE));
		rpgdata.getStats().getInstance(StatType.WEAPON_DAMAGE).addModifier("MMOItems", data.getStats().getStat(ItemStat.WEAPON_DAMAGE));
		rpgdata.getStats().getInstance(StatType.PROJECTILE_DAMAGE).addModifier("MMOItems", data.getStats().getStat(ItemStat.PROJECTILE_DAMAGE));
		rpgdata.getStats().getInstance(StatType.PHYSICAL_DAMAGE).addModifier("MMOItems", data.getStats().getStat(ItemStat.PHYSICAL_DAMAGE));
		rpgdata.getStats().getInstance(StatType.MAGICAL_DAMAGE).addModifier("MMOItems", data.getStats().getStat(ItemStat.MAGIC_DAMAGE));
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new MMOCoreRPGPlayer(data);
	}

	@EventHandler
	public void a(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void b(PlayerChangeClassEvent event) {
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