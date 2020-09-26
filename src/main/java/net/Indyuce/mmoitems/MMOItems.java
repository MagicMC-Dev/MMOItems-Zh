package net.Indyuce.mmoitems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.RecipeBookUtil;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.command.UpdateItemCommand;
import net.Indyuce.mmoitems.command.completion.UpdateItemCompletion;
import net.Indyuce.mmoitems.comp.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.MMOItemsMetrics;
import net.Indyuce.mmoitems.comp.MMOItemsRewardTypes;
import net.Indyuce.mmoitems.comp.RealDualWieldHook;
import net.Indyuce.mmoitems.comp.WorldEditSupport;
import net.Indyuce.mmoitems.comp.eco.VaultSupport;
import net.Indyuce.mmoitems.comp.flags.DefaultFlags;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin;
import net.Indyuce.mmoitems.comp.flags.ResidenceFlags;
import net.Indyuce.mmoitems.comp.flags.WorldGuardFlags;
import net.Indyuce.mmoitems.comp.holograms.CMIPlugin;
import net.Indyuce.mmoitems.comp.holograms.HologramSupport;
import net.Indyuce.mmoitems.comp.holograms.HologramsPlugin;
import net.Indyuce.mmoitems.comp.holograms.HolographicDisplaysPlugin;
import net.Indyuce.mmoitems.comp.holograms.TrHologramPlugin;
import net.Indyuce.mmoitems.comp.inventory.DefaultPlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.OrnamentPlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.RPGInventoryHook;
import net.Indyuce.mmoitems.comp.itemglow.ItemGlowListener;
import net.Indyuce.mmoitems.comp.itemglow.NoGlowListener;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mmoinventory.MMOInventorySupport;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsLoader;
import net.Indyuce.mmoitems.comp.parse.IridescentParser;
import net.Indyuce.mmoitems.comp.parse.StringInputParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.DefaultPlaceholderParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderAPIParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderParser;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.CraftingListener;
import net.Indyuce.mmoitems.listener.CustomBlockListener;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import net.Indyuce.mmoitems.listener.DisableInteractions;
import net.Indyuce.mmoitems.listener.DurabilityListener;
import net.Indyuce.mmoitems.listener.ElementListener;
import net.Indyuce.mmoitems.listener.ItemUse;
import net.Indyuce.mmoitems.listener.PlayerListener;
import net.Indyuce.mmoitems.manager.AbilityManager;
import net.Indyuce.mmoitems.manager.BlockManager;
import net.Indyuce.mmoitems.manager.ConfigManager;
import net.Indyuce.mmoitems.manager.CraftingManager;
import net.Indyuce.mmoitems.manager.DropTableManager;
import net.Indyuce.mmoitems.manager.EntityManager;
import net.Indyuce.mmoitems.manager.ItemManager;
import net.Indyuce.mmoitems.manager.LayoutManager;
import net.Indyuce.mmoitems.manager.PluginUpdateManager;
import net.Indyuce.mmoitems.manager.RecipeManager;
import net.Indyuce.mmoitems.manager.SetManager;
import net.Indyuce.mmoitems.manager.StatManager;
import net.Indyuce.mmoitems.manager.TemplateManager;
import net.Indyuce.mmoitems.manager.TierManager;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.manager.UpdaterManager;
import net.Indyuce.mmoitems.manager.UpgradeManager;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import net.mmogroup.mmolib.api.player.MMOPlayerData;
import net.mmogroup.mmolib.version.SpigotPlugin;

public class MMOItems extends JavaPlugin {

	/*
	 * Introducing: The commit comment! Just change this a tiny bit each time
	 * you need to push a new build. It's convenient!
	 */
	public static MMOItems plugin;

	private final PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();
	private final CraftingManager stationRecipeManager = new CraftingManager();
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
	private UpdaterManager dynamicUpdater;
	private ConfigManager configManager;
	private BlockManager blockManager;
	private TierManager tierManager;
	private StatManager statManager;
	private SetManager setManager;

	private final List<StringInputParser> stringInputParsers = new ArrayList<>();
	private PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
	private PlayerInventory inventory = new DefaultPlayerInventory();
	private FlagPlugin flagPlugin = new DefaultFlags();
	private HologramSupport hologramSupport;
	private RPGHandler rpgPlugin;

	public void onLoad() {
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
	}

	public void onEnable() {
		new SpigotPlugin(39267, this).checkForUpdate();

		new MMOItemsMetrics();

		abilityManager.initialize();
		configManager = new ConfigManager();

		// registering here so the stats will load with the templates
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			new MythicMobsLoader();
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
		tierManager = new TierManager();
		setManager = new SetManager();
		upgradeManager = new UpgradeManager();
		templateManager.postloadTemplates();

		dropTableManager = new DropTableManager();
		dynamicUpdater = new UpdaterManager();
		worldGenManager = new WorldGenManager();
		blockManager = new BlockManager();

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			new VaultSupport();
			getLogger().log(Level.INFO, "Hooked onto Vault");
		}

		getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
		layoutManager.reload();
		stationRecipeManager.reload();

		Bukkit.getPluginManager().registerEvents(entityManager, this);
		Bukkit.getPluginManager().registerEvents(dropTableManager, this);
		Bukkit.getPluginManager().registerEvents(dynamicUpdater, this);
		Bukkit.getPluginManager().registerEvents(new ItemUse(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomSoundListener(), this);
		Bukkit.getPluginManager().registerEvents(new DurabilityListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableInteractions(), this);
		Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getPluginManager().registerEvents(new ElementListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), this);

		/*
		 * this class implements the Listener, if the option
		 * perm-effects-apply-on-move is enabled the loop will not apply perm
		 * effects and this class will be registered as a listener. starts with
		 * a 5s delay to let the other plugins time to load nicely
		 */
		Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).updateEffects()), 100, 20);

		/*
		 * this tasks updates twice a second player inventories on the server.
		 * allows now to use a glitchy itemEquipEvent. must be called after
		 * loading the config since it checks for a config option
		 */
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers())
					PlayerData.get(player).checkForInventoryUpdate();
			}
		}, 100, getConfig().getInt("inventory-update-delay"));

		if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
			flagPlugin = new ResidenceFlags();
			getLogger().log(Level.INFO, "Hooked onto Residence");
		}

		if (Bukkit.getPluginManager().getPlugin("RPGInventory") != null) {
			inventory = new RPGInventoryHook();
			getLogger().log(Level.INFO, "Hooked onto RPGInventory");
		} else if (MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory"))
			inventory = new OrnamentPlayerInventory();

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
		Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.load(player)));

		if (getConfig().getBoolean("recipes.recipe-amounts")) {
			RecipeBookUtil.enableAmounts();
			Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
		}
		if (getConfig().getBoolean("recipes.use-recipe-book"))
			RecipeBookUtil.enableBook();

		// amount and bukkit recipes
		getLogger().log(Level.INFO, "Loading recipes, please wait...");
		recipeManager.loadRecipes();

		// main command
		MMOItemsCommandTreeRoot mmoitemsCommand = new MMOItemsCommandTreeRoot();
		getCommand("mmoitems").setExecutor(mmoitemsCommand);
		getCommand("mmoitems").setTabCompleter(mmoitemsCommand);

		// update item command
		getCommand("updateitem").setExecutor(new UpdateItemCommand());
		getCommand("updateitem").setTabCompleter(new UpdateItemCompletion());
	}

	public void onDisable() {

		// save player data
		MMOPlayerData.getLoaded().stream().filter(data -> data.getMMOItems() != null).forEach(data -> data.getMMOItems().save());

		// save item updater data
		ConfigFile updater = new ConfigFile("/dynamic", "updater");
		updater.getConfig().getKeys(false).forEach(key -> updater.getConfig().set(key, null));
		dynamicUpdater.collectActive().forEach(data -> {
			updater.getConfig().createSection(data.getPath());
			data.save(updater.getConfig().getConfigurationSection(data.getPath()));
		});
		updater.save();

		// drop abandonned soulbound items
		SoulboundInfo.getAbandonnedInfo().forEach(info -> info.dropItems());

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

	public UpdaterManager getUpdater() {
		return dynamicUpdater;
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
	 * @param handler
	 *            Your RPGHandler instance
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

	public PlayerInventory getInventory() {
		return inventory;
	}

	/**
	 * The PlayerInventory interface lets MMOItems knows what items to look for
	 * in player inventories when doing inventory updates. By default, it only
	 * checks held items + armor slots. However other plugins like MMOInv do
	 * implement custom slots and therefore must register a custom
	 * PlayerInventory instance.
	 *
	 * Default instance is DefaultPlayerInventory in comp.inventory
	 *
	 * @param value
	 *            The player inventory subclass
	 */
	public void setPlayerInventory(PlayerInventory value) {
		inventory = value;
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

	public TemplateManager getTemplates() {
		return templateManager;
	}

	@Deprecated
	public ItemManager getItems() {
		return itemManager;
	}

	public List<StringInputParser> getStringInputParsers() {
		return stringInputParsers;
	}

	public void findRpgPlugin() {
		if (rpgPlugin != null)
			return;

		for (RPGHandler.PluginEnum plugin : RPGHandler.PluginEnum.values())
			if (Bukkit.getPluginManager().getPlugin(plugin.getName()) != null) {
				setRPG(plugin.load());
				getLogger().log(Level.INFO, "Hooked onto " + plugin.getName());
				return;
			}

		setRPG(new DefaultHook());
	}

	/**
	 * @return Generates an item given an item template. The item level will
	 *         scale according to the player RPG level if the template has the
	 *         'level-item' option. The item will pick a random tier if the
	 *         template has the 'tiered' option
	 */
	public MMOItem getMMOItem(Type type, String id, PlayerData player) {
		return templateManager.getTemplate(type, id).newBuilder(player.getRPG()).build();
	}

	public ItemStack getItem(Type type, String id, PlayerData player) {
		return getMMOItem(type, id, player).newBuilder().build();
	}

	/**
	 * @param itemLevel
	 *            The desired item level
	 * @param itemTier
	 *            The desired item tier, can be null
	 * @return Generates an item given an item template with a specific item
	 *         level and item tier
	 */
	public MMOItem getMMOItem(Type type, String id, int itemLevel, @Nullable ItemTier itemTier) {
		return templateManager.getTemplate(type, id).newBuilder(itemLevel, itemTier).build();
	}

	public ItemStack getItem(Type type, String id, int itemLevel, @Nullable ItemTier itemTier) {
		return getMMOItem(type, id, itemLevel, itemTier).newBuilder().build();
	}

	/**
	 * @return Generates an item given an item template. The item level will be
	 *         0 and the item will have no item tier unless one is specified in
	 *         the base item data.
	 */
	public MMOItem getMMOItem(Type type, String id) {
		return templateManager.getTemplate(type, id).newBuilder(0, null).build();
	}

	public ItemStack getItem(Type type, String id) {
		return getMMOItem(type, id).newBuilder().build();
	}
}