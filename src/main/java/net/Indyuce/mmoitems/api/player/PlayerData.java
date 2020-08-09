package net.Indyuce.mmoitems.api.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.ItemSet.SetBonuses;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus;
import net.Indyuce.mmoitems.api.event.AbilityUseEvent;
import net.Indyuce.mmoitems.api.item.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory.EquippedItem;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.player.MMOPlayerData;

public class PlayerData {

	private final MMOPlayerData mmoData;

	/*
	 * reloaded everytime the player reconnects in case of major change.
	 */
	private RPGPlayer rpgPlayer;

	/*
	 * the inventory is all the items the player can actually use. items are
	 * cached here to check if the player's items changed, if so just update
	 * inventory TODO improve player inventory checkup method
	 */
	private ItemStack helmet = null, chestplate = null, leggings = null, boots = null, hand = null, offhand = null;
	private List<VolatileMMOItem> playerInventory = new ArrayList<>();

	private CraftingStatus craftingStatus = new CraftingStatus();
	private PlayerAbilityData playerAbilityData = new PlayerAbilityData();
	private Map<String, CooldownInformation> abilityCooldowns = new HashMap<>();
	private Map<String, Long> itemCooldowns = new HashMap<>();
	private Map<CooldownType, Long> extraCooldowns = new HashMap<>();

	/*
	 * specific stat calculation TODO compress it in Map<ItemStat,
	 * DynamicStatData>
	 */
	private Map<PotionEffectType, PotionEffect> permanentEffects = new HashMap<>();
	private Set<ParticleRunnable> itemParticles = new HashSet<>();
	private ParticleRunnable overridingItemParticles = null;
	private Set<AbilityData> itemAbilities = new HashSet<>();
	private boolean fullHands = false;
	private SetBonuses setBonuses = null;
	private final PlayerStats stats;

	private PlayerData(MMOPlayerData mmoData) {
		mmoData.setMMOItems(this);

		this.mmoData = mmoData;
		this.rpgPlayer = MMOItems.plugin.getRPG().getInfo(this);
		this.stats = new PlayerStats(this);

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
	}

	public MMOPlayerData getMMOPlayerData() {
		return mmoData;
	}

	public UUID getUniqueId() {
		return mmoData.getUniqueId();
	}

	public Player getPlayer() {
		return mmoData.getPlayer();
	}

	public RPGPlayer getRPG() {
		return rpgPlayer;
	}

	/*
	 * returns all the usable MMOItems in the player inventory, this can be used
	 * to calculate stats. this list updates each time a player equips a new
	 * item.
	 */
	public List<VolatileMMOItem> getMMOItems() {
		return playerInventory;
	}

	public void checkForInventoryUpdate() {
		PlayerInventory inv = getPlayer().getInventory();
		if (!equals(helmet, inv.getHelmet()) || !equals(chestplate, inv.getChestplate()) || !equals(leggings, inv.getLeggings())
				|| !equals(boots, inv.getBoots()) || !equals(hand, inv.getItemInMainHand()) || !equals(offhand, inv.getItemInOffHand()))
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
		NBTItem main = MMOLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInMainHand());
		NBTItem off = MMOLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInOffHand());
		return (main.getBoolean("MMOITEMS_TWO_HANDED") && (off.getItem() != null && off.getItem().getType() != Material.AIR))
				|| (off.getBoolean("MMOITEMS_TWO_HANDED") && (main.getItem() != null && main.getItem().getType() != Material.AIR));
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
		for (EquippedItem item : MMOItems.plugin.getInventory().getInventory(getPlayer())) {
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

			playerInventory.add(new VolatileMMOItem(nbtItem));
		}

		for (VolatileMMOItem item : getMMOItems()) {

			/*
			 * apply permanent potion effects
			 */
			if (item.hasData(ItemStat.PERM_EFFECTS))
				((PotionEffectListData) item.getData(ItemStat.PERM_EFFECTS)).getEffects().forEach(effect -> {
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
				if (item.getNBT().getItem().equals(getPlayer().getInventory().getItemInOffHand())
						&& MMOItems.plugin.getConfig().getBoolean("disable-abilities-in-offhand")) {
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
		for (VolatileMMOItem item : getMMOItems()) {
			String tag = item.getNBT().getString("MMOITEMS_ITEM_SET");
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
		stats.updateStats();

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
		helmet = getPlayer().getInventory().getHelmet();
		chestplate = getPlayer().getInventory().getChestplate();
		leggings = getPlayer().getInventory().getLeggings();
		boots = getPlayer().getInventory().getBoots();
		hand = getPlayer().getInventory().getItemInMainHand();
		offhand = getPlayer().getInventory().getItemInOffHand();
	}

	public void updateEffects() {

		// perm effects
		permanentEffects.keySet().forEach(effect -> {
			getPlayer().removePotionEffect(effect);
			getPlayer().addPotionEffect(permanentEffects.get(effect));
		});

		// two handed
		if (fullHands) {
			getPlayer().removePotionEffect(PotionEffectType.SLOW);
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, true, false));
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

	public Set<AbilityData> getItemAbilities() {
		return itemAbilities;
	}

	private boolean hasAbility(CastingMode castMode) {
		for (AbilityData ability : itemAbilities)
			if (ability.getCastingMode() == castMode)
				return true;
		return false;
	}

	public ItemAttackResult castAbilities(LivingEntity target, ItemAttackResult result, CastingMode castMode) {

		/*
		 * performance improvement, do not cache the player stats into a
		 * CachedStats if the player has no ability on that cast mode
		 */
		if (!hasAbility(castMode))
			return result;

		return castAbilities(getStats().newTemporary(), target, result, castMode);
	}

	public ItemAttackResult castAbilities(CachedStats stats, LivingEntity target, ItemAttackResult result, CastingMode castMode) {

		/*
		 * if ability has target, check for ability flag at location of target
		 * and make sure player can attack target. if ability has no target,
		 * check for WG flag at the caster location
		 */
		if (target == null ? !MMOItems.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MI_ABILITIES)
				: !MMOItems.plugin.getFlags().isFlagAllowed(target.getLocation(), CustomFlag.MI_ABILITIES)
						|| !MMOUtils.canDamage(getPlayer(), target))
			return result.setSuccessful(false);

		for (AbilityData ability : itemAbilities)
			if (ability.getCastingMode() == castMode)
				cast(stats, target, result, ability);

		return result;
	}

	/*
	 * shall only be used with right click abilites since the on-hit abilities
	 * also requires the initial damage value and a target to be successfully
	 * cast
	 */
	@Deprecated
	public void cast(Ability ability) {
		cast(getStats().newTemporary(), null, new ItemAttackResult(true, DamageType.SKILL), new AbilityData(ability, CastingMode.RIGHT_CLICK));
	}

	public void cast(AbilityData data) {
		cast(getStats().newTemporary(), null, new ItemAttackResult(true, DamageType.SKILL), data);
	}

	public void cast(CachedStats stats, LivingEntity target, ItemAttackResult attack, AbilityData ability) {
		AbilityUseEvent event = new AbilityUseEvent(this, ability, target);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		if (!rpgPlayer.canCast(ability))
			return;

		/*
		 * check if ability can be cast (custom conditions)
		 */
		AbilityResult abilityResult = ability.getAbility().whenRan(stats, target, ability, attack);
		if (!abilityResult.isSuccessful())
			return;

		/*
		 * the player can cast the ability, and it was successfully cast on its
		 * target, removes resources needed from the player
		 */
		if (ability.hasModifier("mana"))
			rpgPlayer.giveMana(-abilityResult.getModifier("mana"));
		if (ability.hasModifier("stamina"))
			rpgPlayer.giveStamina(-abilityResult.getModifier("stamina"));

		double cooldown = abilityResult.getModifier("cooldown") * (1 - Math.min(.8, stats.getStat(ItemStat.COOLDOWN_REDUCTION) / 100));
		if (cooldown > 0)
			applyAbilityCooldown(ability.getAbility(), cooldown);

		/*
		 * finally cast the ability (BUG FIX) cooldown MUST be applied BEFORE
		 * the ability is cast otherwise instantaneously damaging abilities like
		 * Sparkle can trigger deadly crash loops
		 */
		ability.getAbility().whenCast(stats, abilityResult, attack);
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
		abilityCooldowns.put(id, new CooldownInformation(value));
	}

	public boolean hasCooldownInfo(Ability ability) {
		return hasCooldownInfo(ability.getID());
	}

	public boolean hasCooldownInfo(String id) {
		return abilityCooldowns.containsKey(id);
	}

	public CooldownInformation getCooldownInfo(Ability ability) {
		return getCooldownInfo(ability.getID());
	}

	public CooldownInformation getCooldownInfo(String id) {
		return abilityCooldowns.get(id);
	}

	public double getItemCooldown(String id) {
		return Math.max(0, (double) (itemCooldowns.get(id) - System.currentTimeMillis()) / 1000);
	}

	public static PlayerData get(OfflinePlayer player) {
		return get(player.getUniqueId());
	}

	public static PlayerData get(UUID uuid) {
		return MMOPlayerData.get(uuid).getMMOItems();
	}

	/*
	 * method called when the corresponding MMOPlayerData has already been
	 * initialized
	 */
	public static void load(Player player) {

		MMOPlayerData mmoData = MMOPlayerData.get(player.getUniqueId());

		/*
		 * if no mmoitems data could be found, it must be initialized
		 */
		if (mmoData.getMMOItems() == null)
			mmoData.setMMOItems(new PlayerData(mmoData));

		/*
		 * otherwise just update the cached RPGPlayer in case of any major
		 * change in the player data of other rpg plugins
		 */
		else
			mmoData.getMMOItems().rpgPlayer = MMOItems.plugin.getRPG().getInfo(mmoData.getMMOItems());
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
