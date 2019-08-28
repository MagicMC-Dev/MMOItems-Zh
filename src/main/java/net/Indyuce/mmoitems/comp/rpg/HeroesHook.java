package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class HeroesHook implements RPGHandler, Listener {
	public HeroesHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public boolean canBeDamaged(Entity player) {
		return !Heroes.getInstance().getDamageManager().isSpellTarget(player);
	}

	@Override
	public void refreshStats(PlayerData data) {
		Hero hero = Heroes.getInstance().getCharacterManager().getHero(data.getPlayer());
		hero.removeMaxMana("MMOItems");
		hero.addMaxMana("MMOItems", (int) data.getStats().getStat(ItemStat.MAX_MANA));
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new HeroesPlayer(data);
	}

	/*
	 * update the player's inventory whenever he levels up since it could change
	 * its current stat requirements
	 */
	@EventHandler
	public void a(HeroChangeLevelEvent event) {
		PlayerData.get(event.getHero().getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void b(SkillDamageEvent event) {

		/*
		 * apply the 'Magic Damage' and 'Magic Damage Reduction' item option to
		 * Heroes skills
		 */
		if (event.getSkill().isType(SkillType.ABILITY_PROPERTY_MAGICAL)) {
			if (event.getDamager().getEntity() instanceof Player)
				event.setDamage(event.getDamage() * (1 + PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.MAGIC_DAMAGE) / 100));
			if (event.getEntity() instanceof Player)
				event.setDamage(event.getDamage() * (1 - PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.MAGIC_DAMAGE_REDUCTION) / 100));
		}

		/*
		 * apply 'Physical Damage Reduction' to physical skills
		 */
		if (event.getSkill().isType(SkillType.ABILITY_PROPERTY_PHYSICAL))
			if (event.getEntity() instanceof Player)
				event.setDamage(event.getDamage() * (1 - PlayerData.get((Player) event.getDamager().getEntity()).getStats().getStat(ItemStat.PHYSICAL_DAMAGE_REDUCTION) / 100));
	}

	public class HeroesPlayer extends RPGPlayer {
		private Hero hero;

		public HeroesPlayer(PlayerData playerData) {
			super(playerData);

			hero = Heroes.getInstance().getCharacterManager().getHero(getPlayer());
		}

		@Override
		public int getLevel() {
			return hero.getHeroLevel();
		}

		@Override
		public String getClassName() {
			return hero.getHeroClass().getName();
		}

		@Override
		public double getMana() {
			return hero.getMana();
		}

		@Override
		public double getStamina() {
			return hero.getStamina();
		}

		@Override
		public void setMana(double value) {
			hero.setMana((int) value);
		}

		@Override
		public void setStamina(double value) {
			hero.setStamina((int) value);
		}
	}
}