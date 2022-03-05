package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.version.SpigotPlugin;
import io.lumine.mythic.utils.plugin.LuminePlugin;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.comp.*;
import net.Indyuce.mmoitems.comp.eco.VaultSupport;
import net.Indyuce.mmoitems.comp.enchants.CrazyEnchantsStat;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import net.Indyuce.mmoitems.comp.enchants.MythicEnchantsSupport;
import net.Indyuce.mmoitems.comp.enchants.advanced_enchants.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.inventory.*;
import net.Indyuce.mmoitems.comp.itemglow.ItemGlowListener;
import net.Indyuce.mmoitems.comp.itemglow.NoGlowListener;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mmoinventory.MMOInventorySupport;
import net.Indyuce.mmoitems.comp.mythicmobs.LootsplosionListener;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsCompatibility;
import net.Indyuce.mmoitems.comp.parse.StringInputParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.DefaultPlaceholderParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderAPIParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderParser;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.McMMOHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.*;
import net.Indyuce.mmoitems.manager.*;
import net.Indyuce.mmoitems.skill.Shulker_Missile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MMOItems extends LuminePlugin {
	public static MMOItems plugin;

	// Increment this when making breaking changes to items.
	public static final int INTERNAL_REVISION_ID = 1;

	private final PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();
	private final CraftingManager stationRecipeManager = new CraftingManager();
	private final LoreFormatManager formatManager = new LoreFormatManager();
	private final TemplateManager templateManager = new TemplateManager();
	private final AbilityManager abilityManager = new AbilityManager();
	private final SkillManager skillManager = new SkillManager();
	private final EntityManager entityManager = new EntityManager();
	private final RecipeManager recipeManager = new RecipeManager();
	private final LayoutManager layoutManager = new LayoutManager();
	private final TypeManager typeManager = new TypeManager();
	private final ItemManager itemManager = new ItemManager();
	private final PlayerInventoryHandler inventory = new PlayerInventoryHandler();
	private final List<StringInputParser> stringInputParsers = new ArrayList<>();
	private final List<EnchantPlugin<? extends Enchantment>> enchantPlugins = new ArrayList<>();

	private DropTableManager dropTableManager;
	private WorldGenManager worldGenManager;
	private UpgradeManager upgradeManager;
	private ConfigManager configManager;
	private BlockManager blockManager;
	private TierManager tierManager;
	private StatManager statManager;
	private SetManager setManager;

	private PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
	private VaultSupport vaultSupport;
	private RPGHandler rpgPlugin;

    /**
     * Startup issues usually prevent the plugin from loading and just
     * call {@link #disable()} directly afterwards which prints out
     * another error log.
     * <p>
     * To prevent this, MMOItems stores a field to check if the plugin
     * has successfully enabled before trying to call {@link #disable()}
     */
    private boolean hasLoadedSuccessfully;

	private static final int MYTHICLIB_COMPATIBILITY_INDEX = 8;

	public MMOItems() { plugin = this; }

	@Override
	public void load() {

		// Check if the ML build matches
		if (MYTHICLIB_COMPATIBILITY_INDEX != MythicLib.MMOITEMS_COMPATIBILITY_INDEX) {
			getLogger().log(Level.WARNING, "Your versions of MythicLib and MMOItems do not match. Make sure you are using the latest builds of both plugins");
			setEnabled(false);
			return;
		}

		if (getServer().getPluginManager().getPlugin("WorldEdit") != null) try {
			new WorldEditSupport();
			getLogger().log(Level.INFO, "Hooked onto WorldEdit");
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, "Could not initialize support with WorldEdit 7: " + exception.getMessage());
		}

		// Initialize default config files
		saveDefaultConfig();
		configManager = new ConfigManager();

		/*
		 * Stat manager must be initialized before MMOCore compatibility
		 * initializes so that MMOCore can register its stats
		 */
		statManager = new StatManager();
		typeManager.reload();
		templateManager.preloadTemplates();

		if (Bukkit.getPluginManager().getPlugin("MMOCore") != null) new MMOCoreMMOLoader();

		if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
			//statManager.register(McMMOHook.MCMMO_SUPER_TOOL);
			statManager.register(McMMOHook.disableMcMMORepair);

		if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") != null) {
			statManager.register(AdvancedEnchantmentsHook.ADVANCED_ENCHANTMENTS);
			statManager.register(AdvancedEnchantmentsHook.DISABLE_ADVANCED_ENCHANTMENTS);
		}

		if (Bukkit.getPluginManager().getPlugin("MythicEnchants") != null) enchantPlugins.add(new MythicEnchantsSupport());
	}

	@Override
	public void enable() {
		new SpigotPlugin(39267, this).checkForUpdate();
		new MMOItemsMetrics();
		MMOItemUIFilter.register();

		RecipeBrowserGUI.registerNativeRecipes();
		skillManager.initialize(false);

		final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
		final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
		if (configVersion != defConfigVersion) {
			getLogger().warning("You may be using an outdated config.yml!");
			getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
		}

		// registering here so the stats will load with the templates
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			new MythicMobsCompatibility();
			if (getConfig().getBoolean("lootsplosion.enabled")) Bukkit.getPluginManager().registerEvents(new LootsplosionListener(), this);
			getLogger().log(Level.INFO, "Hooked onto MythicMobs");
		}

		if (Bukkit.getPluginManager().getPlugin("MMOInventory") != null) {
			new MMOInventorySupport();
			getLogger().log(Level.INFO, "Hooked onto MMOInventory");
		}

		findRpgPlugin();

		/*
		 * After tiers, sets and upgrade templates are loaded, MI template data
		 * can be fully loaded
		 */
		formatManager.reload();
		tierManager = new TierManager();
		setManager = new SetManager();
		upgradeManager = new UpgradeManager();
		templateManager.postloadTemplates();

		dropTableManager = new DropTableManager();
		worldGenManager = new WorldGenManager();
		blockManager = new BlockManager();

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) vaultSupport = new VaultSupport();

		getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
		layoutManager.reload();
		stationRecipeManager.reload();

		// This ones are not implementing Reloadable
		NumericStatFormula.reload();
		MMOItemReforger.reload();

		Bukkit.getPluginManager().registerEvents(entityManager, this);
		Bukkit.getPluginManager().registerEvents(dropTableManager, this);
		Bukkit.getPluginManager().registerEvents(new ItemUse(), this);
		Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomSoundListener(), this);
		Bukkit.getPluginManager().registerEvents(new DurabilityListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableInteractions(), this);
		Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getPluginManager().registerEvents(new ElementListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), this);
		if (Bukkit.getPluginManager().getPlugin("PhatLoots") != null) {
			Bukkit.getPluginManager().registerEvents(new PhatLootsHook(), this);
		}

		Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).updateStats()), 100, 20);

		/*
		 * This tasks updates twice a second player inventories on the server.
		 * allows now to use a glitchy itemEquipEvent. Must be called after
		 * loading the config since it checks for a config option
		 */
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers())
				PlayerData.get(player).getInventory().updateCheck();
		}, 100, getConfig().getInt("inventory-update-delay"));


		if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) Bukkit.getPluginManager().registerEvents(new McMMONonRPGHook(), this);

		/*
		 * Registers Player Inventories. Each of these add locations of items to search for
		 * when doing inventory updates.
		 */
		getInventory().register(new DefaultPlayerInventory());
		if (Bukkit.getPluginManager().getPlugin("RPGInventory") != null) {
			getInventory().register(new RPGInventoryHook());
			getLogger().log(Level.INFO, "Hooked onto RPGInventory");
		}
		if (MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory")) getInventory().register(new OrnamentPlayerInventory());

		if (Bukkit.getPluginManager().getPlugin("CrazyEnchantments") != null) {
			getStats().register(new CrazyEnchantsStat());
			getLogger().log(Level.INFO, "Hooked onto CrazyEnchantments");
		}

		if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") != null) {
			Bukkit.getPluginManager().registerEvents(new AdvancedEnchantmentsHook(), this);
			getLogger().log(Level.INFO, "Hooked onto AdvancedEnchantments");
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
			placeholderParser = new PlaceholderAPIParser();
		}

		if (getConfig().getBoolean("item-glow")) {
			if (Bukkit.getPluginManager().getPlugin("GlowAPI") != null && Bukkit.getPluginManager().getPlugin("PacketListenerApi") != null) {
				Bukkit.getPluginManager().registerEvents(new ItemGlowListener(), this);
				getLogger().log(Level.INFO, "Hooked onto GlowAPI (Item Glow)");
			} else Bukkit.getPluginManager().registerEvents(new NoGlowListener(), this);
		}

		if (Bukkit.getPluginManager().getPlugin("RealDualWield") != null) {
			Bukkit.getPluginManager().registerEvents(new RealDualWieldHook(), this);
			getLogger().log(Level.INFO, "Hooked onto RealDualWield");
		}

		if (Bukkit.getPluginManager().getPlugin("BossShopPro") != null) {
			getLogger().log(Level.INFO, "Hooked onto BossShopPro");
			(new BukkitRunnable() {
				public void run() {
					//noinspection ProhibitedExceptionCaught
					try {
						new MMOItemsRewardTypes().register();
					} catch (NullPointerException ignored) {
						getLogger().log(Level.INFO, "Could not Hook onto BossShopPro");
					}
				}
			}).runTaskLater(this, 1L);
		}

		/*if (Bukkit.getPluginManager().getPlugin("Denizen") != null) {
			new DenizenHook();
			getLogger().log(Level.INFO, "Hooked onto Denizen");
		}*/

		// Compatibility with /reload
		Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().forEach(PlayerData::load));

		boolean book = getConfig().getBoolean("recipes.use-recipe-book");
		boolean amounts = getConfig().getBoolean("recipes.recipe-amounts");

		if (book && amounts) {
			getLogger().warning("Tried to enable recipe book while amounts are active!");
			getLogger().warning("Please use only ONE of these options!");
			getLogger().warning("Disabling both options for now...");
			book = false;
			amounts = false;
		}

		recipeManager.load(book, amounts);
		if (amounts) Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);

		// Amount and bukkit recipes
		getLogger().log(Level.INFO, "Loading recipes, please wait...");
		recipeManager.loadRecipes();

		// Main command
		MMOItemsCommandTreeRoot mmoitemsCommand = new MMOItemsCommandTreeRoot();
		getCommand("mmoitems").setExecutor(mmoitemsCommand);
		getCommand("mmoitems").setTabCompleter(mmoitemsCommand);

		// Mark plugin as successfully enabled
		hasLoadedSuccessfully = true;
	}

	@Override
	public void disable() {

		// Support for early plugin disabling
		if (!hasLoadedSuccessfully)
			return;

		// Save player data
		PlayerData.getLoaded().forEach(PlayerData::save);

		// Drop abandonned soulbound items
		SoulboundInfo.getAbandonnedInfo().forEach(SoulboundInfo::dropItems);

		// Close inventories
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory)
				player.closeInventory();
	}

	public String getPrefix() {
		return ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "MMOItems" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	}

	public File getJarFile() {
		return plugin.getFile();
	}

	public CraftingManager getCrafting() {
		return stationRecipeManager;
	}

	public LayoutManager getLayouts() {
		return layoutManager;
	}

	public SetManager getSets() {
		return setManager;
	}

	public RPGHandler getRPG() {
		return rpgPlugin;
	}

	/**
	 * The RPGHandler interface lets MMOItems fetch and manipulate RPG data like
	 * player level, class, resources like mana and stamina for item or skill
	 * costs, item restrictions, etc.
	 *
	 * @param handler Your RPGHandler instance
	 */
	public void setRPG(RPGHandler handler) {
		Validate.notNull(handler, "RPGHandler cannot be null");

		// Unregister events from current RPGPlugin instance
		if (rpgPlugin != null && rpgPlugin instanceof Listener && isEnabled())
			HandlerList.unregisterAll((Listener) rpgPlugin);

		rpgPlugin = handler;

		// Register new events
		if (handler instanceof Listener && isEnabled())
			Bukkit.getPluginManager().registerEvents((Listener) handler, this);
	}

	/**
	 * @param potentialPlugin Some plugin that the user wants compatibility with
	 * @return If it worked
	 */
	public boolean setRPG(RPGHandler.PluginEnum potentialPlugin) {

		try {
			Validate.notNull(Bukkit.getPluginManager().getPlugin(potentialPlugin.getName()), "Plugin is not installed");
			setRPG(potentialPlugin.load());
			return true;

			// Some loading issue
		} catch (Exception exception) {
			MMOItems.plugin.getLogger().log(Level.WARNING, "Could not initialize RPG plugin compatibility with " + potentialPlugin.getName() + ":");
			exception.printStackTrace();
			return false;
		}
	}

	public PluginUpdateManager getUpdates() {
		return pluginUpdateManager;
	}

	public PlayerInventoryHandler getInventory() {
		return inventory;
	}

	/**
	 * The PlayerInventory interface lets MMOItems knows what items to look for
	 * in player inventories whe doing inventory updates. By default, it only
	 * checks held items + armor slots. However other plugins like MMOInv do
	 * implement custom slots and therefore must register a custom
	 * PlayerInventory instance that tells of additional items to look for.
	 */
	public void registerPlayerInventory(PlayerInventory value) {

		// Registers in the Inventory Handler
		getInventory().register(value);
	}

	/**
	 * The PlayerInventory interface lets MMOItems knows what items to look for
	 * in player inventories whe doing inventory updates. By default, it only
	 * checks held items + armor slots. However other plugins like MMOInv do
	 * implement custom slots and therefore must register a custom
	 * PlayerInventory instance.
	 * <p>
	 * Default instance is DefaultPlayerInventory in comp.inventory
	 *
	 * @param value The player inventory subclass
	 * @deprecated Rather than setting this to the only inventory MMOItems will
	 * search equipment within, you must add your inventory to the
	 * handler with <code>getInventory().register()</code>. This method
	 * will clear all other PlayerInventories for now, as to keep
	 * backwards compatibility.
	 */
	@Deprecated
	public void setPlayerInventory(PlayerInventory value) {

		// Unregisters those previously registered
		getInventory().unregisterAll();

		// Registers this as the only
		getInventory().register(value);
	}

	/**
	 * Plugins like MythicEnchants which utilize the Bukkit
	 * class Enchantment by extending it don't use any ItemStat
	 * to store their enchants and therefore need to be called
	 * to update the item lore when any item is built.
	 *
	 * @param enchantPlugin Enchantment plugin
	 */
	public void registerEnchantPlugin(EnchantPlugin enchantPlugin) {
		Validate.notNull(enchantPlugin, "Enchant plugin cannot be null");
		enchantPlugins.add(enchantPlugin);
	}

	public StatManager getStats() {
		return statManager;
	}

	public TierManager getTiers() {
		return tierManager;
	}

	public EntityManager getEntities() {
		return entityManager;
	}

	public DropTableManager getDropTables() {
		return dropTableManager;
	}

	@Deprecated
	public AbilityManager getAbilities() {
		return abilityManager;
	}

	public SkillManager getSkills() {
		return skillManager;
	}

	public BlockManager getCustomBlocks() {
		return blockManager;
	}

	public WorldGenManager getWorldGen() {
		return worldGenManager;
	}

	public RecipeManager getRecipes() {
		return recipeManager;
	}

	public ConfigManager getLanguage() {
		return configManager;
	}

	public TypeManager getTypes() {
		return typeManager;
	}

	public UpgradeManager getUpgrades() {
		return upgradeManager;
	}

	public PlaceholderParser getPlaceholderParser() {
		return placeholderParser;
	}

	public TemplateManager getTemplates() {
		return templateManager;
	}

	public LoreFormatManager getFormats() {
		return formatManager;
	}

	@Deprecated
	public ItemManager getItems() {
		return itemManager;
	}

	/*
	 * External API's
	 */
	public boolean hasPermissions() {
		return vaultSupport != null && vaultSupport.getPermissions() != null;
	}

	public List<EnchantPlugin<? extends Enchantment>> getEnchantPlugins() {
		return enchantPlugins;
	}

	public boolean hasEconomy() {
		return vaultSupport != null && vaultSupport.getEconomy() != null;
	}

	public VaultSupport getVault() {
		return vaultSupport;
	}

	public List<StringInputParser> getStringInputParsers() {
		return stringInputParsers;
	}

	//region Easy-Access API

	/**
	 * Decide by which system will the RPG Requirements of the player will be checked.
	 * <p>
	 * For example, required level, is that vanilla XP levels, MMOCore levels, McMMO Leves or what?
	 *
	 * This method is called on server startup and will try to read the preferred RPG
	 * provider in the main plugin config. If it can't be found, it will look for RPG
	 * plugins in the installed plugin list.
	 */
	public void findRpgPlugin() {
		if (rpgPlugin != null) return;

		// Preferred rpg provider
		String preferred = plugin.getConfig().getString("preferred-rpg-provider", null);
		if (preferred != null && setRPG(RPGHandler.PluginEnum.valueOf(preferred.toUpperCase())))
			return;

		// Look through installed plugins
		for (RPGHandler.PluginEnum pluginEnum : RPGHandler.PluginEnum.values())
			if (Bukkit.getPluginManager().getPlugin(pluginEnum.getName()) != null && setRPG(pluginEnum))
				return;

		// Just use the default
		setRPG(new DefaultHook());
	}

	/**
	 * @return Generates an item given an item template. The item level will
	 * scale according to the player RPG level if the template has the
	 * 'level-item' option. The item will pick a random tier if the
	 * template has the 'tiered' option
	 */
	@Nullable
	public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, @Nullable PlayerData player) {
		if (type == null || id == null) { return null; }

		// Valid template?
		MMOItemTemplate found = getTemplates().getTemplate(type, id);
		if (found == null) return null;

		// Build if found
		return found.newBuilder(player).build();
	}

	/**
	 * @return Generates an item given an item template. The item level will
	 * scale according to the player RPG level if the template has the
	 * 'level-item' option. The item will pick a random tier if the
	 * template has the 'tiered' option
	 */
	@Nullable
	public ItemStack getItem(@Nullable Type type, @Nullable String id, @NotNull PlayerData player) {

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id, player);
		if (m == null) return null;

		// Build if found
		return m.newBuilder().build();
	}

	/**
	 * @param itemLevel The desired item level
	 * @param itemTier  The desired item tier, can be null
	 * @return Generates an item given an item template with a
	 * specific item level and item tier
	 */
	@Nullable
	public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, int itemLevel, @Nullable ItemTier itemTier) {
		if (type == null || id == null) { return null; }

		// Valid template?
		MMOItemTemplate found = getTemplates().getTemplate(type, id);
		if (found == null) return null;

		// Build if found
		return found.newBuilder(itemLevel, itemTier).build();
	}

	/**
	 * @param itemLevel The desired item level
	 * @param itemTier  The desired item tier, can be null
	 *
	 * @return Generates an item given an item template with a
	 * specific item level and item tier
	 */
	@Nullable
	public ItemStack getItem(@Nullable Type type, @Nullable String id, int itemLevel, @Nullable ItemTier itemTier) {

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id, itemLevel, itemTier);
		if (m == null) return null;

		// Build if found
		return m.newBuilder().build();
	}

	/**
	 * @return Generates an item given an item template. The item level will be
	 * 0 and the item will have no item tier unless one is specified in
	 * the base item data.
	 * <p></p>
	 * Will return <code>null</code> if such MMOItem does not exist.
	 */
	@Nullable
	public MMOItem getMMOItem(@Nullable Type type, @Nullable String id) { return getMMOItem(type, id, 0, null); }

	/**
	 * @return Generates an item given an item template. The item level will be
	 * 0 and the item will have no item tier unless one is specified in
	 * the base item data.
	 * <p></p>
	 * Will return <code>null</code> if such MMOItem does not exist.
	 */

	@Nullable
	public ItemStack getItem(@Nullable String type, @Nullable String id) {
		if (type == null || id == null) { return null; }
		return getItem(getTypes().get(type), id);
	}

	/**
	 * @return Generates an item given an item template. The item level will be
	 * 0 and the item will have no item tier unless one is specified in
	 * the base item data.
	 * <p></p>
	 * Will return <code>null</code> if such MMOItem does not exist.
	 */
	@Nullable
	public ItemStack getItem(@Nullable Type type, @Nullable String id) {
		if (type == null || id == null) { return null; }

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id);
		if (m == null) { return null; }

		// Build if found
		return m.newBuilder().build();
	}

	//region Reading MMOItems from ItemStacks

	/**
	 * @param stack The stack you trying to read
	 * @return The MMOItems type of this stack, if it has one
	 * @see #getType(NBTItem)
	 */
	@Nullable
	public static Type getType(@Nullable ItemStack stack) {

		// Get from nbt
		return getType(NBTItem.get(stack));
	}

	/**
	 * @param nbt The NBTItem you trying to read
	 * @return The MMOItems type of this nbt, if it has one
	 */
	@Nullable
	public static Type getType(@Nullable NBTItem nbt) {

		// That's it
		return plugin.getTypes().get(getTypeName(nbt));
	}

	/**
	 * @param stack The stack you trying to read
	 * @return The MMOItems type of this stack, if it has one
	 * @see #getTypeName(NBTItem)
	 */
	@Nullable
	public static String getTypeName(@Nullable ItemStack stack) {

		// Get from nbt
		return getTypeName(NBTItem.get(stack));
	}

	/**
	 * @param nbt The NBTItem you trying to read
	 * @return The MMOItems type of this nbt, if it has one
	 */
	@Nullable
	public static String getTypeName(@Nullable NBTItem nbt) {

		// Straight up no
		if (nbt == null) {
			return null;
		}

		// Get from nbt
		if (!nbt.hasType()) {
			return null;
		}

		// That's it
		return nbt.getType();
	}

	/**
	 * @param nbt The ItemStack you trying to read
	 * @return The MMOItems ID of this stack, if it has one
	 * @see #getID(NBTItem)
	 */
	@Nullable
	public static String getID(@Nullable ItemStack nbt) {

		// That's it
		return getID(NBTItem.get(nbt));
	}

	/**
	 * @param nbt The NBTItem you trying to read
	 * @return The MMOItems ID of this nbt, if it has one
	 */
	@Nullable
	public static String getID(@Nullable NBTItem nbt) {

		// Straight up no
		if (nbt == null) {
			return null;
		}

		// That's it
		return nbt.getString("MMOITEMS_ITEM_ID");
	}
	//endregion

	/**
	 * Easily log something using the FriendlyFeedbackProvider, nice!
	 * <p></p>
	 * Use a null level to use the normal console sender.
	 *
	 * @author Gunging
	 */
	public static void print(@Nullable Level level, @Nullable String message, @Nullable String prefix, @NotNull String... replaces) {
		if (message == null) {
			message = "< null >";
		}
		if (level != null) {
			plugin.getLogger().log(level, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), message, replaces));
		} else {
			FriendlyFeedbackMessage p = new FriendlyFeedbackMessage("", prefix);
			FriendlyFeedbackMessage r = FriendlyFeedbackProvider.generateMessage(p, message, replaces);
			getConsole().sendMessage(r.forConsole(FFPMMOItems.get()));
		}
	}

	/**
	 * JULES DO NOT DELETE THIS AGAIN I KNOW ITS UNUSED PRECISELY BECAUSE I ALWAYS COMMENT
	 * ALL ITS USAGES BEFORE PUSHING ANY UPDATES, I USE IT FOR SPAMMY DEVELOPER MESSAGES
	 *
	 * Note that {@link #print(Level, String, String, String...)} is used for actual warnings
	 * or such that the users may see, so dont delete that one either.
	 *
	 * @author Gunging
	 */
	public static void log(@Nullable String message, @NotNull String... replaces) { print(null, message, null, replaces); }

	/**
	 * @return The server's console sender.
	 * @author Gunging
	 */
	@NotNull
	public static ConsoleCommandSender getConsole() {
		return plugin.getServer().getConsoleSender();
	}
	//endregion
}