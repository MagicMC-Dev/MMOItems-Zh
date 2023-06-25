package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.version.SpigotPlugin;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.DeathItemsHandler;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.comp.MMOItemsMetrics;
import net.Indyuce.mmoitems.comp.MMOItemsRewardTypes;
import net.Indyuce.mmoitems.comp.McMMONonRPGHook;
import net.Indyuce.mmoitems.comp.WorldEditSupport;
import net.Indyuce.mmoitems.comp.eco.VaultSupport;
import net.Indyuce.mmoitems.comp.enchants.CrazyEnchantsStat;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import net.Indyuce.mmoitems.comp.enchants.MythicEnchantsSupport;
import net.Indyuce.mmoitems.comp.enchants.advanced_enchants.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.inventory.*;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mmoinventory.MMOInventorySupport;
import net.Indyuce.mmoitems.comp.mythicmobs.LootsplosionListener;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsCompatibility;
import net.Indyuce.mmoitems.comp.placeholders.MMOItemsPlaceholders;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.HeroesHook;
import net.Indyuce.mmoitems.comp.rpg.McMMOHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.manager.*;
import net.Indyuce.mmoitems.manager.data.PlayerDataManager;
import net.Indyuce.mmoitems.util.PluginUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class MMOItems extends JavaPlugin {
    public static MMOItems plugin;

    private final PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();
    private final CraftingManager stationRecipeManager = new CraftingManager();
    private final LoreFormatManager formatManager = new LoreFormatManager();
    private final TemplateManager templateManager = new TemplateManager();
    private final SkillManager skillManager = new SkillManager();
    private final EntityManager entityManager = new EntityManager();
    private final RecipeManager recipeManager = new RecipeManager();
    private final LayoutManager layoutManager = new LayoutManager();
    private final TypeManager typeManager = new TypeManager();
    private final ItemManager itemManager = new ItemManager();
    private final PlayerInventoryHandler inventory = new PlayerInventoryHandler();
    private final List<EnchantPlugin<? extends Enchantment>> enchantPlugins = new ArrayList<>();
    private final StatManager statManager = new StatManager();

    private PlayerDataManager playerDataManager;
    private DropTableManager dropTableManager;
    private WorldGenManager worldGenManager;
    private UpgradeManager upgradeManager;
    private ConfigManager configManager;
    private BlockManager blockManager;
    private TierManager tierManager;
    private SetManager setManager;
    private VaultSupport vaultSupport;
    private final List<RPGHandler> rpgPlugins = new ArrayList<>();

    /**
     * Startup issues usually prevent the plugin from loading and just
     * call {@link #onDisable()} directly afterwards which prints out
     * another error log.
     * <p>
     * To prevent this, MMOItems stores a field to check if the plugin
     * has successfully enabled before trying to call {@link #onDisable()}
     */
    private boolean hasLoadedSuccessfully;

    public MMOItems() {
        plugin = this;
    }

    @Override
    public void onLoad() {
        getLogger().log(Level.INFO, "Plugin file is called '" + getFile().getName() + "'");

        PluginUtils.isDependencyPresent("WorldEdit", u -> {
            try {
                new WorldEditSupport();
                getLogger().log(Level.INFO, "Hooked onto WorldEdit");
            } catch (Exception exception) {
                getLogger().log(Level.WARNING, "Could not initialize support with WorldEdit 7: ", exception);
            }
        });

        // Initialize default config files
        saveDefaultConfig();
        configManager = new ConfigManager();

        statManager.load();
        typeManager.reload();
        templateManager.preloadTemplates();

        PluginUtils.isDependencyPresent("MMOCore", u -> new MMOCoreMMOLoader());
        PluginUtils.isDependencyPresent("mcMMO", u -> statManager.register(McMMOHook.disableMcMMORepair));
        PluginUtils.isDependencyPresent("AdvancedEnchantments", u -> {
            statManager.register(AdvancedEnchantmentsHook.ADVANCED_ENCHANTMENTS);
            statManager.register(AdvancedEnchantmentsHook.DISABLE_ADVANCED_ENCHANTMENTS);
        });
        PluginUtils.isDependencyPresent("MythicEnchants", u -> enchantPlugins.add(new MythicEnchantsSupport()));
        PluginUtils.isDependencyPresent("Heroes", u -> statManager.register(HeroesHook.MAX_STAMINA));
    }

    @Override
    public void onEnable() {
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
        PluginUtils.hookDependencyIfPresent("MythicMobs", unused -> {
            new MythicMobsCompatibility();
            if (getConfig().getBoolean("lootsplosion.enabled"))
                Bukkit.getPluginManager().registerEvents(new LootsplosionListener(), this);
        });
        PluginUtils.hookDependencyIfPresent("MMOInventory", unused -> new MMOInventorySupport());

        findRpgPlugins();

        /*
         * After tiers, sets and upgrade templates are loaded, MI template data
         * can be fully loaded
         */
        statManager.loadElements();
        formatManager.reload();
        tierManager = new TierManager();
        setManager = new SetManager();
        upgradeManager = new UpgradeManager();
        templateManager.postloadTemplates();

        dropTableManager = new DropTableManager();
        worldGenManager = new WorldGenManager();
        blockManager = new BlockManager();
        statManager.reload(false);

        PluginUtils.hookDependencyIfPresent("Vault", u -> vaultSupport = new VaultSupport());

        getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
        layoutManager.reload();
        stationRecipeManager.reload();

        // This ones are not implementing Reloadable
        NumericStatFormula.reload();
        MMOItemReforger.reload();

        Bukkit.getPluginManager().registerEvents(entityManager, this);
        Bukkit.getPluginManager().registerEvents(dropTableManager, this);

        // Load Dist module
        // Load MMOCore-Bukkit module
        try {
            Class.forName("net.Indyuce.mmoitems.MMOItemsBukkit").getConstructor(MMOItems.class).newInstance(this);
        } catch (Throwable exception) {
            throw new RuntimeException("Cannot run an API build on Spigot!");
        }

        /*
         * This tasks updates twice a second player inventories on the server.
         * allows now to use a glitchy itemEquipEvent. Must be called after
         * loading the config since it checks for a config option
         */
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                PlayerData.get(player).getInventory().updateCheck();
        }, 100, getConfig().getInt("inventory-update-delay"));

        PluginUtils.isDependencyPresent("mcMMO", unused -> Bukkit.getPluginManager().registerEvents(new McMMONonRPGHook(), this));

        /*
         * Registers Player Inventories. Each of these add locations
         * of items to search for when doing inventory updates.
         */
        getInventory().register(new DefaultPlayerInventory());
        PluginUtils.hookDependencyIfPresent("RPGInventory", unused -> getInventory().register(new RPGInventoryHook()));
        if (MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory"))
            getInventory().register(new OrnamentPlayerInventory());

        PluginUtils.hookDependencyIfPresent("CrazyEnchantments", unused -> getStats().register(new CrazyEnchantsStat()));
        PluginUtils.hookDependencyIfPresent("AdvancedEnchantments", unused -> Bukkit.getPluginManager().registerEvents(new AdvancedEnchantmentsHook(), this));
        PluginUtils.hookDependencyIfPresent("PlaceholderAPI", unused -> new MMOItemsPlaceholders().register());

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
        playerDataManager = new PlayerDataManager();
        playerDataManager.initialize(EventPriority.NORMAL, EventPriority.HIGHEST);

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
    public void onDisable() {
        // Support for early plugin disabling
        if (!hasLoadedSuccessfully)
            return;

        // Save player data
        playerDataManager.saveAll(false);

        // Drop abandoned items
        DeathItemsHandler.getActive().forEach(info -> info.giveItems(true));

        // Close inventories
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory)
                player.closeInventory();

        // WorldGen
        this.worldGenManager.unload();
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

    @Deprecated
    public RPGHandler getRPG() {
        return getMainRPG();
    }

    @Nullable
    public RPGHandler getMainRPG() {
        Validate.isTrue(!rpgPlugins.isEmpty(), "No RPG plugin was found");
        return rpgPlugins.get(0);
    }

    public List<RPGHandler> getRPGs() {
        return rpgPlugins;
    }

    /**
     * Decide by which system will the RPG Requirements of the player will be checked.
     * <p>
     * For example, required level, is that vanilla XP levels, MMOCore levels, McMMO Leves or what?
     * <p>
     * This method is called on server startup and will try to read the preferred RPG
     * provider in the main plugin config. If it can't be found, it will look for RPG
     * plugins in the installed plugin list.
     */
    public void findRpgPlugins() {
        Validate.isTrue(rpgPlugins.isEmpty(), "RPG hooks have already been computed");

        // Default hook
        rpgPlugins.add(new DefaultHook());

        // Find preferred provider
        final @NotNull String preferredName = plugin.getConfig().getString("preferred-rpg-provider");

        // Look through installed plugins
        for (RPGHandler.PluginEnum enumPlugin : RPGHandler.PluginEnum.values())
            if (Bukkit.getPluginManager().getPlugin(enumPlugin.getName()) != null)
                try {
                    final RPGHandler handler = enumPlugin.load();
                    rpgPlugins.add(handler);
                    getLogger().log(Level.INFO, "Hooked onto " + enumPlugin.getName());

                    // Register as main RPG plugin
                    if (preferredName.equalsIgnoreCase(enumPlugin.name())) {
                        Collections.swap(rpgPlugins, 0, rpgPlugins.size() - 1);
                        getLogger().log(Level.INFO, "Now using " + enumPlugin.getName() + " as RPG core plugin");
                    }

                } catch (Exception exception) {
                    MMOItems.plugin.getLogger().log(Level.WARNING, "Could not initialize RPG plugin compatibility with " + enumPlugin.getName() + ":");
                    exception.printStackTrace();
                }

        // Register listener for preferred provider
        final @NotNull RPGHandler preferred = rpgPlugins.get(0);
        if (rpgPlugins.get(0) instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) preferred, this);
    }

    /**
     * The RPGHandler interface lets MMOItems fetch and manipulate RPG data like
     * player level, class, resources like mana and stamina for item or skill
     * costs, item restrictions, etc.
     *
     * @param handler Your RPGHandler instance
     */
    public void setRPG(@NotNull RPGHandler handler) {
        Validate.notNull(handler, "RPGHandler cannot be null");

        // Unregister old events
        if (getMainRPG() instanceof Listener && isEnabled())
            HandlerList.unregisterAll((Plugin) getMainRPG());

        rpgPlugins.add(0, handler);
        getLogger().log(Level.INFO, "Now using " + handler.getClass().getSimpleName() + " as RPG provider");

        // Register new events
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

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
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

    //region Easy-Access API

    /**
     * @return Generates an item given an item template. The item level will
     * scale according to the player RPG level if the template has the
     * 'level-item' option. The item will pick a random tier if the
     * template has the 'tiered' option
     */
    @Nullable
    public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, @Nullable PlayerData player) {
        if (type == null || id == null) {
            return null;
        }

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
        if (type == null || id == null) {
            return null;
        }

        // Valid template?
        MMOItemTemplate found = getTemplates().getTemplate(type, id);
        if (found == null) return null;

        // Build if found
        return found.newBuilder(itemLevel, itemTier).build();
    }

    /**
     * @param itemLevel The desired item level
     * @param itemTier  The desired item tier, can be null
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
    public MMOItem getMMOItem(@Nullable Type type, @Nullable String id) {
        return getMMOItem(type, id, 0, null);
    }

    /**
     * @return Generates an item given an item template. The item level will be
     * 0 and the item will have no item tier unless one is specified in
     * the base item data.
     * <p></p>
     * Will return <code>null</code> if such MMOItem does not exist.
     */

    @Nullable
    public ItemStack getItem(@Nullable String type, @Nullable String id) {
        if (type == null || id == null) {
            return null;
        }
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
        if (type == null || id == null) {
            return null;
        }

        // Valid MMOItem?
        MMOItem m = getMMOItem(type, id);
        if (m == null) {
            return null;
        }

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
     * <p>
     * Note that {@link #print(Level, String, String, String...)} is used for actual warnings
     * or such that the users may see, so dont delete that one either.
     *
     * @author Gunging
     */
    public static void log(@Nullable String message, @NotNull String... replaces) {
        print(null, message, null, replaces);
    }

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