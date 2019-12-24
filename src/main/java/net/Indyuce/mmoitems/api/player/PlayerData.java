package net.Indyuce.mmoitems.api.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.ItemSet.SetBonuses;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus;
import net.Indyuce.mmoitems.api.event.AbilityUseEvent;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory.EquippedItem;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.Abilities.AbilityListData;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.EffectListData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class PlayerData {

	/*
	 * refreshes the player instance if the player has gone offline since or
	 * else just return it. the offline player does not have to be refreshed, it
	 * is just cached at the beginning
	 */
	private Player player;
	private final OfflinePlayer offline;

	/*
	 * reloaded everytime the player reconnects in case of major change.
	 */
	private RPGPlayer rpgPlayer;

	/*
	 * the inventory is all the items the player can actually use. items are
	 * cached here to check if the player's items changed, if so just update
	 * inventory
	 */
	private ItemStack helmet = null, chestplate = null, leggings = null, boots = null, hand = null, offhand = null;
	private List<MMOItem> playerInventory = new ArrayList<>();

	private CraftingStatus craftingStatus = new CraftingStatus();
	private PlayerAbilityData playerAbilityData = new PlayerAbilityData();
	private Map<String, Long> abilityCooldowns = new HashMap<>();
	private Map<String, Long> itemCooldowns = new HashMap<>();
	private Map<CooldownType, Long> extraCooldowns = new HashMap<>();

	/*
	 * specific stat calculation
	 */
	private Map<PotionEffectType, PotionEffect> permanentEffects = new HashMap<>();
	private Set<ParticleRunnable> itemParticles = new HashSet<>();
	private ParticleRunnable overridingItemParticles = null;
	private Set<AbilityData> itemAbilities = new HashSet<>();

	private SetBonuses setBonuses = null;
	private final PlayerStats stats;

	private boolean fullHands = false;

	private static Map<UUID, PlayerData> playerDatas = new HashMap<>();

	private PlayerData(Player player) {
		this.offline = player;
		setPlayer(player);
		stats = new PlayerStats(this);

		load(new ConfigFile("/userdata", getUniqueId().toString()).getConfig());
		updateInventory();
	}

	private void load(FileConfiguration config) {
		if (config.contains("crafting-queue"))
			craftingStatus.load(this, config.getConfigurationSection("crafting-queue"));
	}

	public void save() {
		cancelRunnables();

		ConfigFile config = new ConfigFile("/userdata", getUniqueId().toString());
		config.getConfig().createSection("crafting-queue");
		craftingStatus.save(config.getConfig().getConfigurationSection("crafting-queue"));
		config.save();

		/*
		 * memory leaks
		 */
		player = null;
	}

	private void setPlayer(Player player) {
		this.player = player;
		this.rpgPlayer = MMOItems.plugin.getRPG().getInfo(this);
	}

	public UUID getUniqueId() {
		return offline.getUniqueId();
	}

	public Player getPlayer() {
		return player;
	}

	public RPGPlayer getRPG() {
		return rpgPlayer;
	}

	/*
	 * returns all the usable MMOItems in the player inventory, this can be used
	 * to calculate stats. this list updates each time a player equips a new
	 * item.
	 */
	public List<MMOItem> getMMOItems() {
		return playerInventory;
	}

	public void checkForInventoryUpdate() {
		PlayerInventory inv = player.getInventory();
		if (!equals(helmet, inv.getHelmet()) || !equals(chestplate, inv.getChestplate()) || !equals(leggings, inv.getLeggings()) || !equals(boots, inv.getBoots()) || !equals(hand, inv.getItemInMainHand()) || !equals(offhand, inv.getItemInOffHand()))
			updateInventory();
	}

	public void scheduleDelayedInventoryUpdate() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> updateInventory());
	}

	private boolean equals(ItemStack item, ItemStack item1) {
		return item == null ? item1 == null : item.equals(item1);
	}

	public void cancelRunnables() {
		itemParticles.forEach(runnable -> runnable.cancel());
		if (overridingItemParticles != null)
			overridingItemParticles.cancel();
	}

	/*
	 * returns true if the player hands are full, i.e if the player is holding
	 * one two handed item and one other item at the same time. this will
	 */
	public boolean areHandsFull() {
		NBTItem main = MMOLib.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
		NBTItem off = MMOLib.plugin.getNMS().getNBTItem(player.getInventory().getItemInOffHand());
		return (main.getBoolean("MMOITEMS_TWO_HANDED") && (off.getItem() != null && off.getItem().getType() != Material.AIR)) || (off.getBoolean("MMOITEMS_TWO_HANDED") && (main.getItem() != null && main.getItem().getType() != Material.AIR));
	}

	public void updateInventory() {

		/*
		 * very important, clear particle data AFTER canceling the runnable
		 * otherwise it cannot cancel and the runnable keeps going (severe)
		 */
		playerInventory.clear();
		permanentEffects.clear();
		itemAbilities.clear();
		cancelRunnables();
		itemParticles.clear();
		overridingItemParticles = null;

		/*
		 * updates the full-hands boolean, this way it can be cached and used in
		 * the updateEffects() method
		 */
		fullHands = areHandsFull();

		// find all the items the player can actually use.
		for (EquippedItem item : MMOItems.plugin.getInventory().getInventory(player)) {
			NBTItem nbtItem = item.newNBTItem();

			Type type = nbtItem.getType();
			if (type == null)
				continue;

			/*
			 * if the player is holding an item the wrong way i.e if the item is
			 * not in the right slot. intuitive methods with small exceptions
			 * like BOTH_HANDS and ANY
			 */
			if (!item.matches(type))
				continue;

			if (!getRPG().canUse(nbtItem, false))
				continue;

			playerInventory.add(new MMOItem(nbtItem, false));
		}

		for (MMOItem item : getMMOItems()) {

			/*
			 * apply permanent potion effects
			 */
			if (item.hasData(ItemStat.PERM_EFFECTS))
				((EffectListData) item.getData(ItemStat.PERM_EFFECTS)).getEffects().forEach(effect -> {
					if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getLevel() - 1)
						permanentEffects.put(effect.getType(), effect.toEffect());
				});

			/*
			 * apply item particles
			 */
			if (item.hasData(ItemStat.ITEM_PARTICLES)) {
				ParticleData particleData = (ParticleData) item.getData(ItemStat.ITEM_PARTICLES);

				if (particleData.getType().hasPriority()) {
					if (overridingItemParticles == null)
						overridingItemParticles = particleData.start(this);
				} else
					itemParticles.add(particleData.start(this));
			}

			/*
			 * apply abilities
			 */
			if (item.hasData(ItemStat.ABILITIES)) {
				// if the item with the abilities is in the players offhand AND
				// its disabled in the config then just move on, else add the
				// ability
				if (item.getNBTItem().getItem().equals(player.getInventory().getItemInOffHand()) && MMOItems.plugin.getConfig().getBoolean("disable-abilities-in-offhand")) {
					continue;
				} else
					((AbilityListData) item.getData(ItemStat.ABILITIES)).getAbilities().forEach(ability -> itemAbilities.add(ability));
			}
		}

		/*
		 * calculate the player's item set and add the bonus permanent effects /
		 * bonus abilities to the playerdata maps
		 */
		int max = 0;
		ItemSet set = null;
		Map<ItemSet, Integer> sets = new HashMap<>();
		for (MMOItem item : getMMOItems()) {
			String tag = item.getNBTItem().getString("MMOITEMS_ITEM_SET");
			ItemSet itemSet = MMOItems.plugin.getSets().get(tag);
			if (itemSet == null)
				continue;

			int nextInt = (sets.containsKey(itemSet) ? sets.get(itemSet) : 0) + 1;
			sets.put(itemSet, nextInt);
			if (nextInt >= max) {
				max = nextInt;
				set = itemSet;
			}
		}
		setBonuses = set == null ? null : set.getBonuses(max);

		if (hasSetBonuses()) {
			itemAbilities.addAll(setBonuses.getAbilities());
			for (PotionEffect effect : setBonuses.getPotionEffects())
				if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getAmplifier())
					permanentEffects.put(effect.getType(), effect);
		}

		/*
		 * calculate all stats.
		 */
		stats.update();

		/*
		 * update stuff from the external MMOCore plugins. the 'max mana' stat
		 * currently only supports Heroes since other APIs do not allow other
		 * plugins to easily increase this type of stat.
		 */
		MMOItems.plugin.getRPG().refreshStats(this);

		/*
		 * actually update the player inventory so the task doesn't infinitely
		 * loop on updating
		 */
		helmet = player.getInventory().getHelmet();
		chestplate = player.getInventory().getChestplate();
		leggings = player.getInventory().getLeggings();
		boots = player.getInventory().getBoots();
		hand = player.getInventory().getItemInMainHand();
		offhand = player.getInventory().getItemInOffHand();
	}

	public void updateEffects() {

		// perm effects
		permanentEffects.keySet().forEach(effect -> {
			player.removePotionEffect(effect);
			player.addPotionEffect(permanentEffects.get(effect));
		});

		// two handed
		if (fullHands) {
			player.removePotionEffect(PotionEffectType.SLOW);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 3, true, false));
		}
	}

	public SetBonuses getSetBonuses() {
		return setBonuses;
	}

	public boolean hasSetBonuses() {
		return setBonuses != null;
	}

	public CraftingStatus getCrafting() {
		return craftingStatus;
	}

	public PlayerAbilityData getAbilityData() {
		return playerAbilityData;
	}

	public int getPermanentPotionEffectAmplifier(PotionEffectType type) {
		return permanentEffects.containsKey(type) ? permanentEffects.get(type).getAmplifier() : -1;
	}

	public Collection<PotionEffect> getPermanentPotionEffects() {
		return permanentEffects.values();
	}

	public PlayerStats getStats() {
		return stats;
	}

	public Set<AbilityData> getItemAbilities(CastingMode castMode) {
		return itemAbilities.stream().filter(abilityData -> abilityData.getCastingMode() == castMode).collect(Collectors.toSet());
	}

	public ItemAttackResult castAbilities(LivingEntity target, ItemAttackResult result, CastingMode castMode) {
		return castAbilities(getStats().newTemporary(), target, result, castMode);
	}

	public ItemAttackResult castAbilities(CachedStats stats, LivingEntity target, ItemAttackResult result, CastingMode castMode) {
		if (target == null) {
			if (!MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_ABILITIES))
				return result.setSuccessful(false);
		} else if (!MMOItems.plugin.getFlags().isFlagAllowed(target.getLocation(), CustomFlag.MI_ABILITIES))
			return result.setSuccessful(false);

		if (target != null && !MMOUtils.canDamage(player, target))
			return result.setSuccessful(false);

		boolean message = castMode.displaysMessage();
		for (AbilityData ability : getItemAbilities(castMode))
			cast(stats, target, result, ability, message);

		return result;
	}

	/*
	 * shall only be used with right click abilites since the on-hit abilities
	 * also requires the initial damage value and a target to be successfully
	 * cast
	 */
	public void cast(Ability ability) {
		cast(getStats().newTemporary(), null, new ItemAttackResult(true, DamageType.SKILL), new AbilityData(ability), true);
	}

	public void cast(AbilityData data) {
		cast(getStats().newTemporary(), null, new ItemAttackResult(true, DamageType.SKILL), data, true);
	}

	public void cast(CachedStats stats, LivingEntity target, ItemAttackResult result, AbilityData ability, boolean message) {
		AbilityUseEvent event = new AbilityUseEvent(this, ability, target);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		/*
		 * check if the player can cast the ability, if he can't just return a
		 * new instance of of ItemAttackResult with false boolean
		 */
		if (!rpgPlayer.canCast(ability, message))
			return;

		/*
		 * cast the actual ability and see if it was successfully cast
		 */
		ability.getAbility().whenCast(stats, target, ability, result);
		if (!result.isSuccessful())
			return;

		/*
		 * the player can cast the ability, and it was successfully cast on its
		 * target, removes resources needed from the player
		 */
		if (ability.hasModifier("mana"))
			rpgPlayer.giveMana(-ability.getModifier("mana"));
		if (ability.hasModifier("stamina"))
			rpgPlayer.giveStamina(-ability.getModifier("stamina"));

		double cooldown = ability.getModifier("cooldown");
		if (cooldown > 0)
			applyAbilityCooldown(ability.getAbility(), cooldown);
	}

	public void log(String... lines) {
		for (String line : lines)
			MMOItems.plugin.getLogger().log(Level.WARNING, "[Data] " + player.getName() + ": " + line);
	}

	public boolean isOnCooldown(CooldownType type) {
		return extraCooldowns.containsKey(type) && extraCooldowns.get(type) > System.currentTimeMillis();
	}

	/*
	 * 'value' is either the cooldown value if the cooldown type is a regular
	 * attack cooldown, or the cooldown reduction stat value, if the cooldown
	 * type is mitigation
	 */
	public void applyCooldown(CooldownType type, double value) {
		extraCooldowns.put(type, (long) (System.currentTimeMillis() + 1000 * value));
	}

	public boolean canUseItem(String id) {
		return (itemCooldowns.containsKey(id) ? itemCooldowns.get(id) : 0) < System.currentTimeMillis();
	}

	public void applyItemCooldown(String id, double value) {
		itemCooldowns.put(id, (long) (System.currentTimeMillis() + value * 1000));
	}

	public void applyAbilityCooldown(Ability ability, double value) {
		applyAbilityCooldown(ability.getID(), value);
	}

	public void applyAbilityCooldown(String id, double value) {
		abilityCooldowns.put(id, (long) (System.currentTimeMillis() + value * 1000));
	}

	public double getRemainingAbilityCooldown(Ability ability) {
		return getRemainingAbilityCooldown(ability.getID());
	}

	public double getRemainingAbilityCooldown(String id) {
		return Math.max((abilityCooldowns.containsKey(id) ? abilityCooldowns.get(id) : 0) - System.currentTimeMillis(), 0) / 1000.;
	}

	public double getItemCooldown(String id) {
		return Math.max(0, (double) (itemCooldowns.get(id) - System.currentTimeMillis()) / 1000);
	}

	public static PlayerData get(OfflinePlayer player) {
		return playerDatas.get(player.getUniqueId());
	}

	public static void load(Player player) {

		/*
		 * if the player data is not loaded yet, load it.
		 */
		if (!playerDatas.containsKey(player.getUniqueId()))
			playerDatas.put(player.getUniqueId(), new PlayerData(player));

		/*
		 * otherwise it is already loaded and the player variable must be
		 * refreshed since the player logged out and in again.
		 */
		else
			get(player).setPlayer(player);
	}

	public static Collection<PlayerData> getLoaded() {
		return playerDatas.values();
	}

	public enum CooldownType {

		// either for consumables or for commands
		ITEM,

		// simple attack cooldown
		ATTACK,

		// elemental ttack
		ELEMENTAL_ATTACK,

		// item type special effects
		SPECIAL_ATTACK,

		// piercing / blunt / slashing passive effects
		SET_TYPE_ATTACK;
	}
}
