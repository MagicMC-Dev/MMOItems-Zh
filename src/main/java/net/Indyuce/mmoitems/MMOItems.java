package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.version.SpigotPlugin;
import io.lumine.mythic.utils.plugin.LuminePlugin;
import net.Indyuce.mmoitems.api.*;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.comp.*;
import net.Indyuce.mmoitems.comp.eco.VaultSupport;
import net.Indyuce.mmoitems.comp.flags.DefaultFlags;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin;
import net.Indyuce.mmoitems.comp.flags.ResidenceFlags;
import net.Indyuce.mmoitems.comp.flags.WorldGuardFlags;
import net.Indyuce.mmoitems.comp.holograms.*;
import net.Indyuce.mmoitems.comp.inventory.*;
import net.Indyuce.mmoitems.comp.itemglow.ItemGlowListener;
import net.Indyuce.mmoitems.comp.itemglow.NoGlowListener;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mmoinventory.MMOInventorySupport;
import net.Indyuce.mmoitems.comp.mythicmobs.LootsplosionListener;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsLoader;
import net.Indyuce.mmoitems.comp.parse.IridescentParser;
import net.Indyuce.mmoitems.comp.parse.StringInputParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.DefaultPlaceholderParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderAPIParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderParser;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.McMMOHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.*;
import net.Indyuce.mmoitems.manager.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
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
	private final EntityManager entityManager = new EntityManager();
	private final RecipeManager recipeManager = new RecipeManager();
	private final LayoutManager layoutManager = new LayoutManager();
	private final TypeManager typeManager = new TypeManager();
	private final ItemManager itemManager = new ItemManager();

	private DropTableManager dropTableManager;
	private WorldGenManager worldGenManager;
	private UpgradeManager upgradeManager;
	private ConfigManager configManager;
	private BlockManager blockManager;
	private TierManager tierManager;
	private StatManager statManager;
	private SetManager setManager;
	private EquipListener equipListener;

	private final List<StringInputParser> stringInputParsers = new ArrayList<>();
	private PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
	private PlayerInventoryHandler inventory = new PlayerInventoryHandler();
	private FlagPlugin flagPlugin = new DefaultFlags();
	private HologramSupport hologramSupport;
	private VaultSupport vaultSupport;
	private RPGHandler rpgPlugin;

	@Override
	public void load() {
		plugin = this;

		if (getServer().getPluginManager().getPlugin("WorldGuard") != null)
			try {
				flagPlugin = new WorldGuardFlags();
				getLogger().log(Level.INFO, "Hooked onto WorldGuard");
			} catch (Exception exception) {
				getLogger().log(Level.WARNING, "Could not initialize support with WorldGuard 7: " + exception.getMessage());
			}

		if (getServer().getPluginManager().getPlugin("WorldEdit") != null)
			try {
				new WorldEditSupport();
				getLogger().log(Level.INFO, "Hooked onto WorldEdit");
			} catch (Exception exception) {
				getLogger().log(Level.WARNING, "Could not initialize support with WorldEdit 7: " + exception.getMessage());
			}

		saveDefaultConfig();

		/*
		 * stat manager must be initialized before MMOCore compatibility
		 * initializes so that MMOCore can register its stats
		 */
		statManager = new StatManager();
		typeManager.reload();
		templateManager.preloadTemplates();

		if (Bukkit.getPluginManager().getPlugin("MMOCore") != null)
			new MMOCoreMMOLoader();

		if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
			statManager.register(McMMOHook.disableMcMMORepair);
	}
	@Override
	public void enable() {
		new SpigotPlugin(39267, this).checkForUpdate();
		new MMOItemsMetrics();

		abilityManager.initialize();
		configManager = new ConfigManager();

		final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
		final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
		if (configVersion != defConfigVersion || MMOItems.plugin.getLanguage().arruinarElPrograma) {
			getLogger().warning("You may be using an outdated config.yml!");
			getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '"
					+ (MMOItems.plugin.getLanguage().arruinarElPrograma ? "steelballrun" : defConfigVersion) + "')");
		}

		// registering here so the stats will load with the templates
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			new MythicMobsLoader();
			if (getConfig().getBoolean("lootsplosion.enabled"))
				Bukkit.getPluginManager().registerEvents(new LootsplosionListener(), this);
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
		equipListener = new EquipListener();

		if (Bukkit.getPluginManager().getPlugin("Vault") != null)
			vaultSupport = new VaultSupport();

		getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
		layoutManager.reload();
		stationRecipeManager.reload();

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
			Bukkit.getPluginManager().registerEvents(new PhatLootsHook(), this); }
		MMOItemUIFilter.register();

		Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).updateStats()), 100, 20);

		if (MMOItems.plugin.getLanguage().arruinarElPrograma)
			Bukkit.getScheduler().runTaskTimer(this, ClaseMuyImportante::metodoMuyImportante, 780000L, 780000L);

		/*
		 * this tasks updates twice a second player inventories on the server.
		 * allows now to use a glitchy itemEquipEvent. must be called after
		 * loading the config since it checks for a config option
		 */
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers())
				PlayerData.get(player).getInventory().updateCheck();
		}, 100, getConfig().getInt("inventory-update-delay"));

		if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
			flagPlugin = new ResidenceFlags();
			getLogger().log(Level.INFO, "Hooked onto Residence");
		}

		if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
			Bukkit.getPluginManager().registerEvents(new McMMONonRPGHook(), this);

		/*
		 * Registers Player Inventories. Each of these add locations of items to search for
		 * when doing inventory updates.
		 */
		registerPlayerInventory(new DefaultPlayerInventory());
		if (Bukkit.getPluginManager().getPlugin("RPGInventory") != null) {
			registerPlayerInventory(new RPGInventoryHook());
			getLogger().log(Level.INFO, "Hooked onto RPGInventory");
		}
		if (MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory")) {
			registerPlayerInventory(new OrnamentPlayerInventory());
		}

		if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") != null) {
			Bukkit.getPluginManager().registerEvents(new AdvancedEnchantmentsHook(), this);
			getLogger().log(Level.INFO, "Hooked onto AdvancedEnchantments");
		}

		if (Bukkit.getPluginManager().getPlugin("Iridescent") != null) {
			stringInputParsers.add(new IridescentParser());
			getLogger().log(Level.INFO, "Hooked onto Iridescent");
		}

		if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
			hologramSupport = new HolographicDisplaysPlugin();
			getLogger().log(Level.INFO, "Hooked onto HolographicDisplays");
		} else if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
			hologramSupport = new CMIPlugin();
			getLogger().log(Level.INFO, "Hooked onto CMI Holograms");
		} else if (Bukkit.getPluginManager().getPlugin("Holograms") != null) {
			hologramSupport = new HologramsPlugin();
			getLogger().log(Level.INFO, "Hooked onto Holograms");
		} else if (Bukkit.getPluginManager().getPlugin("TrHologram") != null) {
			hologramSupport = new TrHologramPlugin();
			getLogger().log(Level.INFO, "Hooked onto TrHologram");
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
			placeholderParser = new PlaceholderAPIParser();
		}

		if (getConfig().getBoolean("item-glow")) {
			if (Bukkit.getPluginManager().getPlugin("GlowAPI") != null && Bukkit.getPluginManager().getPlugin("PacketListenerApi") != null) {
				Bukkit.getPluginManager().registerEvents(new ItemGlowListener(), this);
				getLogger().log(Level.INFO, "Hooked onto GlowAPI (Item Glow)");
			} else
				Bukkit.getPluginManager().registerEvents(new NoGlowListener(), this);
		}

		if (Bukkit.getPluginManager().getPlugin("RealDualWield") != null) {
			Bukkit.getPluginManager().registerEvents(new RealDualWieldHook(), this);
			getLogger().log(Level.INFO, "Hooked onto RealDualWield");
		}

		if (Bukkit.getPluginManager().getPlugin("BossShopPro") != null) {
			new MMOItemsRewardTypes().register();
			getLogger().log(Level.INFO, "Hooked onto BossShopPro");
		}

		// compatibility with /reload
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
		if (amounts)
			Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);

		// amount and bukkit recipes
		getLogger().log(Level.INFO, "Loading recipes, please wait...");
		recipeManager.loadRecipes();

		// main command
		MMOItemsCommandTreeRoot mmoitemsCommand = new MMOItemsCommandTreeRoot();
		getCommand("mmoitems").setExecutor(mmoitemsCommand);
		getCommand("mmoitems").setTabCompleter(mmoitemsCommand);

		// update item command DISABLED
		//getCommand("updateitem").setExecutor(new UpdateItemCommand());
		//getCommand("updateitem").setTabCompleter(new UpdateItemCompletion());
	}

	@Override
	public void disable() {

		// save player data
		PlayerData.getLoaded().forEach(PlayerData::save);

		// save item updater data
		ConfigFile updater = new ConfigFile("/dynamic", "updater");
		updater.getConfig().getKeys(false).forEach(key -> updater.getConfig().set(key, null));
		updater.save();

		// drop abandonned soulbound items
		SoulboundInfo.getAbandonnedInfo().forEach(SoulboundInfo::dropItems);

		// close inventories
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

	public FlagPlugin getFlags() {
		return flagPlugin;
	}

	public void setFlags(FlagPlugin value) {
		flagPlugin = value;
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

		// unregister events from current rpgPlugin instance
		if (rpgPlugin != null && rpgPlugin instanceof Listener && isEnabled())
			HandlerList.unregisterAll((Listener) rpgPlugin);

		rpgPlugin = handler;
		if (handler instanceof Listener && isEnabled())
			Bukkit.getPluginManager().registerEvents((Listener) handler, this);
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
	 *             search equipment within, you must add your inventory to the
	 *             handler with <code>getInventory().Register()</code>. This method
	 *             will clear all other PlayerInventories for now, as to keep
	 *             backwards compatibility.
	 */
	public void setPlayerInventory(PlayerInventory value) {

		// Unregisters those previously registered
		getInventory().unregisterAll();

		// Registers this as the only
		getInventory().register(value);
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

	public AbilityManager getAbilities() {
		return abilityManager;
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

	public HologramSupport getHolograms() {
		return hologramSupport;
	}
	public EquipListener getEquipListener(){
		return equipListener;
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
	 * <p></p>
	 * For example, required level, is that vanilla XP levels, MMOCore levels, McMMO Leves or what?
	 */
	public void findRpgPlugin() {
		if (rpgPlugin != null)
			return;

		String preferred = MMOItems.plugin.getConfig().getString("preferred-rpg-provider", null);
		if (preferred != null) {

			try {
				RPGHandler.PluginEnum preferredRPG = RPGHandler.PluginEnum.valueOf(preferred.toUpperCase());
				// Found the plugin?
				if (Bukkit.getPluginManager().getPlugin(preferredRPG.getName()) != null) {

					// Load that one
					setRPG(preferredRPG.load());

					// Mention it
					print(null, "Using $s{0}$b as RPGPlayer provider", "RPG Provider", preferredRPG.getName());
					return;
				} else {

					print(null, "Preferred RPGPlayer provider $r{0}$b is not installed!", "RPG Provider", preferred); }

			} catch (IllegalArgumentException ignored) {

				// Log error
				FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
				ffp.activatePrefix(true, "RPG Provider");
				ffp.log(FriendlyFeedbackCategory.ERROR, "Invalid RPG Provider '$u{0}$b' --- These are the supported ones:", preferred);
				for (RPGHandler.PluginEnum pgrep : RPGHandler.PluginEnum.values()) { ffp.log(FriendlyFeedbackCategory.ERROR, " $r+ $b{0}", pgrep.getName()); }
				ffp.sendTo(FriendlyFeedbackCategory.ERROR, getConsole());
			}
		}

		// For each supported plugin
		for (RPGHandler.PluginEnum pluginEnum : RPGHandler.PluginEnum.values()) {

			// Found the plugin?
			if (Bukkit.getPluginManager().getPlugin(pluginEnum.getName()) != null) {

				// Load that one
				setRPG(pluginEnum.load());

				// Mention it
				print(null, "Using $s{0}$b as RPGPlayer provider", "RPG Provider", pluginEnum.getName());
				return;
			}
		}

		// Just use the default
		setRPG(new DefaultHook());
	}

	/**
	 * @return Generates an item given an item template. The item level will
	 *         scale according to the player RPG level if the template has the
	 *         'level-item' option. The item will pick a random tier if the
	 *         template has the 'tiered' option
	 */
	@Nullable public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, @NotNull PlayerData player) {
		if (type == null || id == null) { return null; }

		// Valid template?
		MMOItemTemplate found = templateManager.getTemplate(type, id);
		if (found == null) { return null; }

		// Build if found
		return found.newBuilder(player.getRPG()).build();
	}

	/**
	 * @return Generates an item given an item template. The item level will
	 *         scale according to the player RPG level if the template has the
	 *         'level-item' option. The item will pick a random tier if the
	 *         template has the 'tiered' option
	 */
	@Nullable public ItemStack getItem(@Nullable Type type, @Nullable String id, @NotNull PlayerData player) {
		if (type == null || id == null) { return null; }

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id, player);
		if (m == null) { return null; }

		// Build if found
		return m.newBuilder().build();
	}

	/**
	 * @param  itemLevel The desired item level
	 * @param  itemTier  The desired item tier, can be null
	 * @return           Generates an item given an item template with a
	 *                   specific item level and item tier
	 */
	@Nullable public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, int itemLevel, @Nullable ItemTier itemTier) {
		if (type == null || id == null) { return null; }

		// Valid template?
		MMOItemTemplate found = templateManager.getTemplate(type, id);
		if (found == null) { return null; }

		// Build if found
		return found.newBuilder(itemLevel, itemTier).build();
	}

	/**
	 * @param  itemLevel The desired item level
	 * @param  itemTier  The desired item tier, can be null
	 * @return           Generates an item given an item template with a
	 *                   specific item level and item tier
	 */
	@Nullable public ItemStack getItem(@Nullable Type type, @Nullable String id, int itemLevel, @Nullable ItemTier itemTier) {
		if (type == null || id == null) { return null; }

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id, itemLevel, itemTier);
		if (m == null) { return null; }

		// Build if found
		return m.newBuilder().build();
	}

	/**
	 * @return Generates an item given an item template. The item level will be
	 *         0 and the item will have no item tier unless one is specified in
	 *         the base item data.
	 *         <p></p>
	 *         Will return <code>null</code> if such MMOItem does not exist.
	 */
	@Nullable public MMOItem getMMOItem(@Nullable Type type, @Nullable String id) {
		if (type == null || id == null) { return null; }

		// Valid template?
		MMOItemTemplate found = templateManager.getTemplate(type, id);
		if (found == null) { return null; }

		// Build if found
		return found.newBuilder(0, null).build();
	}

	/**
	 * @return Generates an item given an item template. The item level will be
	 *         0 and the item will have no item tier unless one is specified in
	 *         the base item data.
	 *         <p></p>
	 *         Will return <code>null</code> if such MMOItem does not exist.
	 */

	@Nullable public ItemStack getItem(@Nullable String type, @Nullable String id) {
		if (type == null || id == null) { return null; }
		return getItem(getType(type), id);
	}
	/**
	 * @return Generates an item given an item template. The item level will be
	 *         0 and the item will have no item tier unless one is specified in
	 *         the base item data.
	 *         <p></p>
	 *         Will return <code>null</code> if such MMOItem does not exist.
	 */
	@Nullable public ItemStack getItem(@Nullable Type type, @Nullable String id) {
		if (type == null || id == null) { return null; }

		// Valid MMOItem?
		MMOItem m = getMMOItem(type, id);
		if (m == null) { return null; }

		// Build if found
		return m.newBuilder().build();
	}

	@Nullable public Type getType(@Nullable NBTItem nbtitem) {
		if (nbtitem == null || !nbtitem.hasType()) { return null; }

		// Try that one instead
		return getType(nbtitem.getType());
	}
	@Nullable public String getID(@Nullable NBTItem nbtitem) {
		if (nbtitem == null || !nbtitem.hasType()) { return null; }

		ItemTag type = ItemTag.getTagAtPath("MMOITEMS_ITEM_ID", nbtitem, SupportedNBTTagValues.STRING);
		if (type == null) { return null; }
		return (String) type.getValue();
	}

	/**
	 * Shorthand to get the specified type.
	 *
	 * @param type What do you think its called
	 *
	 * @return A type if such exists.
	 */
	@Nullable public Type getType(@Nullable String type) {
		if (type == null) { return null; }
		return getTypes().get(type);
	}

	/**
	 * Logs something into the console with a cool [MMOItems] prefix :)
	 * <p></p>
	 * Parses color codes. <b>Mostly for DEV testing</b>. these may removed any release.
	 *
	 * @author Gunging
	 */
	public static void log(@Nullable String message) {
		if (message == null) { message = "< null >"; }
		plugin.getServer().getConsoleSender().sendMessage("\u00a78[" + ChatColor.YELLOW + "MMOItems\u00a78] \u00a77" + message);
	}

	/**
	 * Easily log something using the FriendlyFeedbackProvider, nice!
	 *
	 * @author Gunging
	 */
	public static void print(@Nullable Level level, @Nullable String message, @Nullable String prefix, @NotNull String... replaces) {
		if (message == null) { message = "< null >"; }
		if (level != null) { MMOItems.plugin.getLogger().log(level, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), message, replaces));
		} else {
			FriendlyFeedbackMessage p = new FriendlyFeedbackMessage("", prefix);
			FriendlyFeedbackMessage r = FriendlyFeedbackProvider.generateMessage(p, message, replaces);
			getConsole().sendMessage(r.forConsole(FFPMMOItems.get())); }
	}

	/**
	 * @return The server's console sender.
	 *
	 * @author Gunging
	 */
	@NotNull public static ConsoleCommandSender getConsole() { return plugin.getServer().getConsoleSender(); }
	//endregion
}