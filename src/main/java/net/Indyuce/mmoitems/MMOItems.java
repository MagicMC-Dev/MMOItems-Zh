package net.Indyuce.mmoitems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.command.MMOItemsCommand;
import net.Indyuce.mmoitems.command.UpdateItemCommand;
import net.Indyuce.mmoitems.command.completion.MMOItemsCompletion;
import net.Indyuce.mmoitems.command.completion.UpdateItemCompletion;
import net.Indyuce.mmoitems.comp.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.MMOItemsMetrics;
import net.Indyuce.mmoitems.comp.MMOItemsRewardTypes;
import net.Indyuce.mmoitems.comp.RealDualWieldHook;
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
import net.Indyuce.mmoitems.listener.version.Listener_v1_13;
import net.Indyuce.mmoitems.manager.AbilityManager;
import net.Indyuce.mmoitems.manager.BlockManager;
import net.Indyuce.mmoitems.manager.ConfigManager;
import net.Indyuce.mmoitems.manager.CraftingManager;
import net.Indyuce.mmoitems.manager.DropTableManager;
import net.Indyuce.mmoitems.manager.EntityManager;
import net.Indyuce.mmoitems.manager.ItemGenManager;
import net.Indyuce.mmoitems.manager.ItemManager;
import net.Indyuce.mmoitems.manager.PluginUpdateManager;
import net.Indyuce.mmoitems.manager.SetManager;
import net.Indyuce.mmoitems.manager.StatManager;
import net.Indyuce.mmoitems.manager.TierManager;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.manager.UpdaterManager;
import net.Indyuce.mmoitems.manager.UpgradeManager;
import net.Indyuce.mmoitems.manager.WorldGenManager;
import net.Indyuce.mmoitems.manager.recipe.RecipeManager;
import net.Indyuce.mmoitems.manager.recipe.RecipeManagerDefault;
import net.Indyuce.mmoitems.manager.recipe.RecipeManagerLegacy;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.player.MMOPlayerData;
import net.mmogroup.mmolib.version.SpigotPlugin;

public class MMOItems extends JavaPlugin {
	public static MMOItems plugin;
 
	private final PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();
	private final CraftingManager stationRecipeManager = new CraftingManager();
	private final AbilityManager abilityManager = new AbilityManager();
	private final ItemGenManager itemGenerator = new ItemGenManager();
	private final EntityManager entityManager = new EntityManager();
	private final TypeManager typeManager = new TypeManager();
 
	private DropTableManager dropTableManager;
	private WorldGenManager worldGenManager;
	private UpgradeManager upgradeManager;
	private UpdaterManager dynamicUpdater;
	private ConfigManager configManager;
	private RecipeManager recipeManager;
	private BlockManager blockManager;
	private TierManager tierManager;
	private StatManager statManager;
	private ItemManager itemManager;
	private SetManager setManager;

	private PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
	private PlayerInventory inventory = new DefaultPlayerInventory();
	private FlagPlugin flagPlugin = new DefaultFlags();
	private final List<StringInputParser> stringInputParsers = new ArrayList<>();
	private HologramSupport hologramSupport;
	private RPGHandler rpgPlugin;

	public void onLoad() {
		plugin = this;

		try {
			if (getServer().getPluginManager().getPlugin("WorldGuard") != null && MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)) {
				flagPlugin = new WorldGuardFlags();
				getLogger().log(Level.INFO, "Hooked onto WorldGuard");
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Could not initialize support with WorldGuard 7+");
		}

		/*
		 * stat manager must be initialized before MMOCore compatibility
		 * initializes so that MMOCore can register its stats
		 */
		saveDefaultConfig();
		statManager = new StatManager();
		typeManager.reload();

		if (Bukkit.getPluginManager().getPlugin("MMOCore") != null)
			new MMOCoreMMOLoader();
	}

	public void onEnable() {
		new SpigotPlugin(39267, this).checkForUpdate();

		new MMOItemsMetrics();

		abilityManager.initialize();
		configManager = new ConfigManager();
		itemManager = new ItemManager();
		tierManager = new TierManager();
		setManager = new SetManager();
		upgradeManager = new UpgradeManager();
		dropTableManager = new DropTableManager();
		dynamicUpdater = new UpdaterManager();
		itemGenerator.reload();
		if (MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)) {
			worldGenManager = new WorldGenManager();
			blockManager = new BlockManager();
		}

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			new VaultSupport();
			getLogger().log(Level.INFO, "Hooked onto Vault");
		}

		getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
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
		Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
		if (MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)) {
			Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), this);
			Bukkit.getPluginManager().registerEvents(new Listener_v1_13(), this);
		}

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
				for(Player player : Bukkit.getOnlinePlayers())
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
// Will be used in the future.
//
//		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
//			new MythicMobsLoader();
//			getLogger().log(Level.INFO, "Hooked onto MythicMobs");
//		}

		findRpgPlugin();

		// compatibility with /reload
		Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.load(player)));

		// advanced recipes
		getLogger().log(Level.INFO, "Loading recipes, please wait...");
		recipeManager = MMOLib.plugin.getVersion().isStrictlyHigher(1, 12) ? new RecipeManagerDefault() : new RecipeManagerLegacy();

		// commands
		getCommand("mmoitems").setExecutor(new MMOItemsCommand());
		getCommand("updateitem").setExecutor(new UpdateItemCommand());

		// tab completion
		getCommand("mmoitems").setTabCompleter(new MMOItemsCompletion());
		getCommand("updateitem").setTabCompleter(new UpdateItemCompletion());
	}

	public void onDisable() {

		// save player data
		MMOPlayerData.getLoaded().stream().filter(data -> data.getMMOItems() != null).forEach(data -> data.getMMOItems().save());

		// save item updater data
		ConfigFile updater = new ConfigFile("/dynamic", "updater");
		updater.getConfig().getKeys(false).forEach(key -> updater.getConfig().set(key, null));
		dynamicUpdater.getActive().forEach(data -> {
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
		return ChatColor.YELLOW + "MI" + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY;
	}

	public File getJarFile() {
		return plugin.getFile();
	}

	public CraftingManager getCrafting() {
		return stationRecipeManager;
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

	public ItemGenManager getItemGenerator() {
		return itemGenerator;
	}

	public void setFlags(FlagPlugin value) {
		flagPlugin = value;
	}

	public RPGHandler getRPG() {
		return rpgPlugin;
	}

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

	public boolean isBlacklisted(Material material) {
		return getConfig().getStringList("block-blacklist").contains(material.name());
	}

	/***
	 * Parses an ItemStack from a string. Can be used to both get a vanilla
	 * material or an MMOItem. Used by the recipe manager.
	 */
	public ItemStack parseStack(String parse) {
		ItemStack stack = null;
		String[] split = parse.split("\\:");
		String input = split[0];

		if (input.contains(".")) {
			String[] typeId = input.split("\\.");
			String typeFormat = typeId[0].toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(getTypes().has(typeFormat), "Could not find type " + typeFormat);

			MMOItem mmo = getItems().getMMOItem(MMOItems.plugin.getTypes().get(typeFormat), typeId[1]);
			if (mmo != null)
				stack = mmo.newBuilder().build();
		} else {
			Material mat = Material.AIR;
			try {
				mat = Material.valueOf(input.toUpperCase().replace("-", "_").replace(" ", "_"));
			} catch (IllegalArgumentException e) {
				getLogger().warning("Couldn't parse material from '" + parse + "'!");
			}

			if (mat != Material.AIR)
				stack = new ItemStack(mat);
		}

		try {
			if (stack != null && split.length > 1)
				stack.setAmount(Integer.parseInt(split[1]));
		} catch (NumberFormatException e) {
			getLogger().warning("Couldn't parse amount from '" + parse + "'!");
		}

		return stack;
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