package net.Indyuce.mmoitems.comp.mmocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOCoreHook implements RPGHandler, Listener {

	private final ItemStat cooldownReduction = new DoubleStat(new ItemStack(Material.BOOK), "Skill Cooldown Reduction", new String[] { "Reduces cooldowns of MMOCore skills (%)." }, "skill-cooldown-reduction");
	private final ItemStat additionalExperience = new DoubleStat(new ItemStack(Material.EXPERIENCE_BOTTLE), "Additional Experience", new String[] { "Additional MMOCore main class experience in %." }, "additional-experience");
	private final ItemStat weaponDamage = new DoubleStat(new ItemStack(Material.IRON_SWORD), "Weapon Damage (MMOCore)", new String[] { "Additional weapon damage in %." }, "weapon-damage");
	private final ItemStat skillDamage = new DoubleStat(new ItemStack(Material.BLAZE_POWDER), "Skill Damage (MMOCore)", new String[] { "Additional skill damage in %." }, "skill-damage");
	private final ItemStat projectileDamage = new DoubleStat(new ItemStack(Material.SPECTRAL_ARROW), "Projectile Damage (MMOCore)", new String[] { "Additional projectile damage in %." }, "projectile-damage");
	private final ItemStat magicalDamage = new DoubleStat(new ItemStack(Material.ENCHANTED_BOOK), "Magical Damage (MMOCore)", new String[] { "Additional magical damage in %." }, "magical-damage");
	private final ItemStat physicalDamage = new DoubleStat(new ItemStack(Material.IRON_SWORD), "Physical Damage (MMOCore)", new String[] { "Additional physical damage in %." }, "physical-damage");

	/*
	 * called when MMOItems enables
	 */
	public MMOCoreHook() {

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		/*
		 * register custom damage
		 */
		MMOCore.plugin.damage.registerHandler(new MMOCoreDamageHandler());

		MMOItems.plugin.getStats().register("COOLDOWN_REDUCTION", cooldownReduction);
		MMOItems.plugin.getStats().register("ADDITIONAL_EXPERIENCE", additionalExperience);
		MMOItems.plugin.getStats().register("WEAPON_DAMAGE", weaponDamage);
		MMOItems.plugin.getStats().register("SKILL_DAMAGE", skillDamage);
		MMOItems.plugin.getStats().register("PROJECTILE_DAMAGE", projectileDamage);
		MMOItems.plugin.getStats().register("MAGICAL_DAMAGE", magicalDamage);
		MMOItems.plugin.getStats().register("PHYSICAL_DAMAGE", physicalDamage);
	}

	@Override
	public boolean canBeDamaged(Entity entity) {
		return !MMOCore.plugin.damage.hasDamage(entity);
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
		PlayerData rpgdata = PlayerData.get(data.getPlayer());
		rpgdata.getStats().getInstance(StatType.MAX_MANA).addAttribute("MMOItems", data.getStats().getStat(ItemStat.MAX_MANA));
		rpgdata.getStats().getInstance(StatType.HEALTH_REGENERATION).addAttribute("MMOItems", data.getStats().getStat(ItemStat.REGENERATION));
		rpgdata.getStats().getInstance(StatType.COOLDOWN_REDUCTION).addAttribute("MMOItems", data.getStats().getStat(cooldownReduction));
		rpgdata.getStats().getInstance(StatType.ADDITIONAL_EXPERIENCE).addAttribute("MMOItems", data.getStats().getStat(additionalExperience));

		rpgdata.getStats().getInstance(StatType.SKILL_DAMAGE).addAttribute("MMOItems", data.getStats().getStat(skillDamage));
		rpgdata.getStats().getInstance(StatType.WEAPON_DAMAGE).addAttribute("MMOItems", data.getStats().getStat(weaponDamage));
		rpgdata.getStats().getInstance(StatType.PROJECTILE_DAMAGE).addAttribute("MMOItems", data.getStats().getStat(projectileDamage));
		rpgdata.getStats().getInstance(StatType.PHYSICAL_DAMAGE).addAttribute("MMOItems", data.getStats().getStat(physicalDamage));
		rpgdata.getStats().getInstance(StatType.MAGICAL_DAMAGE).addAttribute("MMOItems", data.getStats().getStat(magicalDamage));
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new MMOCoreRPGPlayer(data);
	}

	@EventHandler
	public void a(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	public class MMOCoreRPGPlayer extends RPGPlayer {
		private PlayerData data;

		public MMOCoreRPGPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
			super(playerData);

			data = PlayerData.get(playerData.getPlayer());
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
			return data.getStellium();
		}

		@Override
		public void setMana(double value) {
			data.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			data.setStellium(value);
		}

		@Override
		public void giveMana(double value) {
			data.giveMana(value);
		}

		@Override
		public void giveStamina(double value) {
			data.giveStellium(value);
		}
	}
}