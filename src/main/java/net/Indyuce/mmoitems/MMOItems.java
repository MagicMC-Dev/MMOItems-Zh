package net.Indyuce.mmoitems;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.command.AdvancedWorkbenchCommand;
import net.Indyuce.mmoitems.command.MMOItemsCommand;
import net.Indyuce.mmoitems.command.UpdateItemCommand;
import net.Indyuce.mmoitems.command.completion.MMOItemsCompletion;
import net.Indyuce.mmoitems.command.completion.UpdateItemCompletion;
import net.Indyuce.mmoitems.comp.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.MMOItemsMetrics;
import net.Indyuce.mmoitems.comp.MMOItemsRewardTypes;
import net.Indyuce.mmoitems.comp.RealDualWieldHook;
import net.Indyuce.mmoitems.comp.flags.DefaultFlags;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin;
import net.Indyuce.mmoitems.comp.flags.ResidenceFlags;
import net.Indyuce.mmoitems.comp.flags.WorldGuardFlags;
import net.Indyuce.mmoitems.comp.holograms.CMIPlugin;
import net.Indyuce.mmoitems.comp.holograms.HologramSupport;
import net.Indyuce.mmoitems.comp.holograms.HologramsPlugin;
import net.Indyuce.mmoitems.comp.holograms.HolographicDisplaysPlugin;
import net.Indyuce.mmoitems.comp.inventory.DefaultPlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.OrnamentPlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.PlayerInventory;
import net.Indyuce.mmoitems.comp.inventory.RPGInventoryHook;
import net.Indyuce.mmoitems.comp.itemglow.ItemGlowListener;
import net.Indyuce.mmoitems.comp.itemglow.NoGlowListener;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsHook;
import net.Indyuce.mmoitems.comp.placeholderapi.DefaultParser;
import net.Indyuce.mmoitems.comp.placeholderapi.PlaceholderAPIParser;
import net.Indyuce.mmoitems.comp.placeholderapi.PlaceholderParser;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.AdvancedWorkbenchListener;
import net.Indyuce.mmoitems.listener.CustomBlockListener;
import net.Indyuce.mmoitems.listener.CustomDurability;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import net.Indyuce.mmoitems.listener.DisableInteractions;
import net.Indyuce.mmoitems.listener.ElementListener;
import net.Indyuce.mmoitems.listener.ItemUse;
import net.Indyuce.mmoitems.listener.MitigationListener;
import net.Indyuce.mmoitems.listener.PlayerListener;
import net.Indyuce.mmoitems.listener.version.Listener_v1_13;
import net.Indyuce.mmoitems.manager.AbilityManager;
import net.Indyuce.mmoitems.manager.BlockManager;
import net.Indyuce.mmoitems.manager.ConfigManager;
import net.Indyuce.mmoitems.manager.CraftingManager;
import net.Indyuce.mmoitems.manager.DamageManager;
import net.Indyuce.mmoitems.manager.DropTableManager;
import net.Indyuce.mmoitems.manager.EntityManager;
import net.Indyuce.mmoitems.manager.ItemManager;
import net.Indyuce.mmoitems.manager.PluginUpdateManager;
import net.Indyuce.mmoitems.manager.RecipeManager;
import net.Indyuce.mmoitems.manager.SetManager;
import net.Indyuce.mmoitems.manager.StatManager;
import net.Indyuce.mmoitems.manager.TierManager;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.manager.UpdaterManager;
import net.Indyuce.mmoitems.manager.UpgradeManager;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import net.Indyuce.mmoitems.version.ServerVersion;
import net.Indyuce.mmoitems.version.SpigotPlugin;
import net.Indyuce.mmoitems.version.nms.NMSHandler;

public class MMOItems extends JavaPlugin {
	public static MMOItems plugin;

	private ServerVersion version;
	private RecipeManager recipeManager;
	private ConfigManager configManager;
	private StatManager statManager;
	private EntityManager entityManager;
	private DamageManager damageManager;
	private DropTableManager dropTableManager;
	private UpdaterManager itemUpdaterManager;
	private TypeManager typeManager;
	private TierManager tierManager;
	private ItemManager itemManager;
	private SetManager setManager;
	private UpgradeManager upgradeManager;
	private WorldGenManager worldGenManager;
	private BlockManager blockManager;
	private AbilityManager abilityManager = new AbilityManager();
	private CraftingManager stationRecipeManager = new CraftingManager();
	private PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();

	private RPGHandler rpgPlugin;
	private PlaceholderParser placeholderParser = new DefaultParser();
	private HologramSupport hologramSupport;
	private FlagPlugin flagPlugin = new DefaultFlags();
	private PlayerInventory inventory = new DefaultPlayerInventory();
	private NMSHandler nms;

	public void onLoad() {
		plugin = this;
		version = new ServerVersion(Bukkit.getServer().getClass());

		try {
			getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
			nms = (NMSHandler) Class.forName("net.Indyuce.mmoitems.version.nms.NMSHandler_" + version.toString().substring(1)).newInstance();
		} catch (Exception e) {
			getLogger().log(Level.INFO, "Your server version is not compatible.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
			// nms = new NMSHandler_Reflection();
		}

		try {
			if (getServer().getPluginManager().getPlugin("WorldGuard") != null && version.isStrictlyHigher(1, 12)) {
				flagPlugin = new WorldGuardFlags();
				getLogger().log(Level.INFO, "Hooked onto WorldGuard");
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Could not initialize support with WorldGuard 7+");
		}

		if (Bukkit.getPluginManager().getPlugin("MMOCore") != null)
			new MMOCoreMMOLoader();

		saveDefaultConfig();
		statManager = new StatManager();
		typeManager = new TypeManager();
	}

	public void onEnable() {
		new SpigotPlugin(39267, this).checkForUpdate();

		new MMOItemsMetrics();

		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		abilityManager.registerDefaultAbilities();
		abilityManager.stopRegistration();

		configManager = new ConfigManager();
		itemManager = new ItemManager(getConfig().getBoolean("use-item-caching"));
		tierManager = new TierManager();
		setManager = new SetManager();
		upgradeManager = new UpgradeManager();
		if (version.isStrictlyHigher(1, 12)) {
			worldGenManager = new WorldGenManager();
			blockManager = new BlockManager();
		}

		getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
		stationRecipeManager.reload();

		Bukkit.getPluginManager().registerEvents(entityManager = new EntityManager(), this);
		Bukkit.getPluginManager().registerEvents(damageManager = new DamageManager(), this);
		Bukkit.getPluginManager().registerEvents(dropTableManager = new DropTableManager(), this);
		Bukkit.getPluginManager().registerEvents(itemUpdaterManager = new UpdaterManager(), this);
		Bukkit.getPluginManager().registerEvents(new ItemUse(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new MitigationListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomSoundListener(), this);
		Bukkit.getPluginManager().registerEvents(new CustomDurability(), this);
		Bukkit.getPluginManager().registerEvents(new DisableInteractions(), this);
		Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getPluginManager().registerEvents(new ElementListener(), this);
		if (version.isStrictlyHigher(1, 12))
			Bukkit.getPluginManager().registerEvents(new Listener_v1_13(), this);

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
		Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).checkForInventoryUpdate()), 100, getConfig().getInt("inventory-update-delay"));

		if (!getConfig().getBoolean("disable-craftings.advanced"))
			Bukkit.getPluginManager().registerEvents(new AdvancedWorkbenchListener(), this);

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

		if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
			hologramSupport = new HolographicDisplaysPlugin();
			getLogger().log(Level.INFO, "Hooked onto HolographicDisplays");
		} else if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
			hologramSupport = new CMIPlugin();
			getLogger().log(Level.INFO, "Hooked onto CMI Holograms");
		} else if (Bukkit.getPluginManager().getPlugin("Holograms") != null) {
			hologramSupport = new HologramsPlugin();
			getLogger().log(Level.INFO, "Hooked onto Holograms");
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
			placeholderParser = new PlaceholderAPIParser();
		}

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			Bukkit.getPluginManager().registerEvents(new MythicMobsHook(), this);
			getLogger().log(Level.INFO, "Hooked onto MythicMobs");
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

			/*
			 * runs async because of plugin loading order issues, this way it
			 * only registers after BossShop is initialized
			 */
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
				new MMOItemsRewardTypes().register();
				getLogger().log(Level.INFO, "Hooked onto BossShopPro (async)");
			});
		}

		findRpgPlugin();

		// compatibility with /reload
		Bukkit.getOnlinePlayers().forEach(player -> PlayerData.load(player));

		// advanced recipes
		getLogger().log(Level.INFO, "Loading recipes, please wait...");
		recipeManager = new RecipeManager();

		// commands
		getCommand("mmoitems").setExecutor(new MMOItemsCommand());
		getCommand("advancedworkbench").setExecutor(new AdvancedWorkbenchCommand());
		getCommand("updateitem").setExecutor(new UpdateItemCommand());

		// tab completion
		getCommand("mmoitems").setTabCompleter(new MMOItemsCompletion());
		getCommand("updateitem").setTabCompleter(new UpdateItemCompletion());
	}

	public void onDisable() {

		// save player data
		PlayerData.getLoaded().forEach(data -> data.save());

		// save item updater data
		ConfigFile updater = new ConfigFile("/dynamic", "updater");
		updater.getConfig().getKeys(false).forEach(key -> updater.getConfig().set(key, null));
		itemUpdaterManager.getDatas().forEach(data -> data.save(updater.getConfig()));
		updater.save();

		// drop abandonned soulbound items
		SoulboundInfo.getAbandonnedInfo().forEach(info -> info.dropItems());

		// close inventories
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory)
				player.closeInventory();
	}

	public String getPrefix() {
		return ChatColor.YELLOW + "MI" + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY;
	}

	public File getJarFile() {
		return plugin.getFile();
	}

	public CraftingManager getCrafting() {
		return stationRecipeManager;
	}

	public UpdaterManager getUpdater() {
		return itemUpdaterManager;
	}

	public SetManager getSets() {
		return setManager;
	}

	public NMSHandler getNMS() {
		return nms;
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

	public void setRPG(RPGHandler handler) {
		rpgPlugin = handler;
	}

	public PluginUpdateManager getUpdates() {
		return pluginUpdateManager;
	}

	public PlayerInventory getInventory() {
		return inventory;
	}

	public void setPlayerInventory(PlayerInventory value) {
		inventory = value;
	}

	public ServerVersion getVersion() {
		return version;
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

	public DamageManager getDamage() {
		return damageManager;
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

	public ItemManager getItems() {
		return itemManager;
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

	public boolean isBlacklisted(Material material) {
		return getConfig().getStringList("block-blacklist").contains(material.name());
	}

	public void debug(Object... message) {
		if (!getConfig().getBoolean("debug"))
			return;

		for (Object line : message) {
			getLogger().log(Level.INFO, "Debug> " + line.toString());
			Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColor.YELLOW + "Debug> " + ChatColor.WHITE + line.toString()));
		}
	}
}