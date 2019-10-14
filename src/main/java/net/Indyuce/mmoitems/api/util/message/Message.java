package net.Indyuce.mmoitems.api.util.message;

import org.bukkit.ChatColor;

import net.Indyuce.mmoitems.MMOItems;

public enum Message {

	RECEIVED_ITEM("You received &6#item#&e#amount#."),

	// general restrictions
	HANDS_TOO_CHARGED("You can't do anything, your hands are too charged."),
	SPELL_ON_COOLDOWN("#progress# &eYou must wait #left# second#s# before casting this spell."),
	ITEM_ON_COOLDOWN("This item is on cooldown! Please wait #left#s."),
	NOT_ENOUGH_PERMS_COMMAND("You don't have enough permissions."),

	// mitigation
	ATTACK_BLOCKED("You just blocked #percent#% of the attack damage!"),
	ATTACK_DODGED("You just dodged an attack!"),
	ATTACK_PARRIED("You just parried an attack!"),

	// item restrictions
	NOT_ENOUGH_LEVELS("You don't have enough levels to use this item!"),
	NOT_ENOUGH_ATTRIBUTE("You don't have enough attributes to use this item!"),
	SOULBOUND_RESTRICTION("This item is linked to another player, you can't use it!"),
	NOT_ENOUGH_PERMS("You don't have enough permissions to use this."),
	WRONG_CLASS("You don't have the right class!"),
	NOT_ENOUGH_MANA("You don't have enough mana!"),
	NOT_ENOUGH_STAMINA("You don't have enough stamina!"),
	UNIDENTIFIED_ITEM("You can't use an unidentified item!"),

	// custom durability
	ITEM_BROKE("Your #item#&c broke."),
	ZERO_DURABILITY("This item is broken, you first need to repair it."),

	// consumables & gem stones
	SUCCESSFULLY_IDENTIFIED("You successfully identified &6#item#&e."),
	SUCCESSFULLY_DECONSTRUCTED("You successfully deconstructed &6#item#&e."),
	GEM_STONE_APPLIED("You successfully applied &6#gem#&e onto your &6#item#&e."),
	GEM_STONE_BROKE("Your gem stone &6#gem#&c broke while trying to apply it onto &6#item#&c."),
	REPAIRED_ITEM("You successfully repaired &6#item#&e for &6#amount# &euses."),

	// advanced workbennch
	ADVANCED_WORKBENCH("Advanced Workbench"),
	EMPTY_WORKBENCH_FIRST("Please empty the workbench first."),
	NOT_ENOUGH_PERMS_CRAFT("You don't have enough permissions to craft this item."),
	ADVANCED_RECIPES("Advanced Recipes"),
	CLICK_ADVANCED_RECIPE("#d Click to see its recipe."),

	// soulbound
	CANT_BIND_ITEM("This item is currently linked to #player# by a Lvl #level# soulbound. You will have to break this soulbound first."),
	NO_SOULBOUND("This item is not bound to anyone."),
	CANT_BIND_STACKED("You can't bind stacked items."),
	UNSUCCESSFUL_SOULBOUND("Your soulbound failed."),
	UNSUCCESSFUL_SOULBOUND_BREAK("You couldn't break the soulbound."),
	LOW_SOULBOUND_LEVEL("This item soulbound is Lvl #level#. You will need a higher soulbound level on your consumable to break this soulbound."),
	SUCCESSFULLY_BIND_ITEM("You successfully applied a Lvl &6#level# &esoulbound to your &6#item#&e."),
	SUCCESSFULLY_BREAK_BIND("You successfully broke the Lvl &6#level# &eitem soulbound!"),
	SOULBOUND_ITEM_LORE("&4Linked to #player#//&4Lvl #level# Soulbound"),

	// upgrade
	CANT_UPGRADED_STACK("You can't upgrade stacked items."),
	MAX_UPGRADES_HIT("This item cannot be upgraded anymore."),
	UPGRADE_FAIL("Your upgrade failed and you lost your consumable."),
	UPGRADE_FAIL_STATION("Your upgrade failed and you lost your materials."),
	WRONG_UPGRADE_REFERENCE("You cannot upgrade this item with this consumable."),
	UPGRADE_SUCCESS("You successfully upgraded your &6#item#&e!"),
	NOT_HAVE_ITEM_UPGRADE("You don't have the item to upgrade!"),

	// crafting messages
	NOT_ENOUGH_MATERIALS("You do not have enough materials to craft this item."),
	CONDITIONS_NOT_MET("You cannot craft this item."),
	CRAFTING_CANCELED("You cancelled a crafting recipe."),
	CRAFTING_QUEUE_FULL("The crafting queue is currently full."),
	// ALREADY_CRAFTING("You are already crafting something else."),
	// ALREADY_CRAFTING_STATION("You are already crafting something else in that
	// station."),
	// CRAFTING_SUBTITLE("#bar# &e#left# left"),
	;

	private final String defaultMessage, path;

	private Message(String defaultMessage) {
		this.defaultMessage = defaultMessage;
		this.path = name().toLowerCase().replace("_", "-");
	}

	public String getDefault() {
		return defaultMessage;
	}

	public String getUpdated() {
		return MMOItems.plugin.getLanguage().getMessage(path);
	}

	// toReplace length must be even
	public String formatRaw(ChatColor prefix, String... toReplace) {
		return format(prefix, toReplace).toString();
	}

	public PlayerMessage format(ChatColor prefix, String... toReplace) {
		return new PlayerMessage(getUpdated()).format(prefix, toReplace);
	}
}