package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class EditionInventory extends PluginInventory {

    /**
     * Item template currently being edited. This field is not final as it is
     * refreshed every time the item is edited (after applying a config change,
     * MMOItems updates the registered template and removes the old one)
     */
    protected MMOItemTemplate template;

    /**
     * Config file being edited. It is cached when the edition inventory is
     * opened and can only be accessed through the getEditedSection() method
     */
    private final ConfigFile configFile;

    /**
     * Template modifier being edited, if it is null then the player is directly
     * base item data
     *
     * @deprecated Not being used atm, the item editor only lets the user
     *         edit the base item data
     */
    @Deprecated
    private TemplateModifier editedModifier;

    private ItemStack cachedItem;
    int previousPage;

    public EditionInventory(@NotNull Player player, @NotNull MMOItemTemplate template) {
        super(player);

        // For logging back to the player
        ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
        ffp.activatePrefix(true, "Edition");

        // For building the Inventory
        this.template = template;
        this.configFile = template.getType().getConfigFile();
        player.getOpenInventory();
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof EditionInventory)
            this.cachedItem = ((EditionInventory) player.getOpenInventory().getTopInventory().getHolder()).cachedItem;
    }

    public MMOItemTemplate getEdited() {
        return template;
    }

    /**
     * @return The currently edited configuration section. It depends on if the
     *         player is editing the base item data or editing a modifier. This
     *         config section contains item data (either the 'base' config
     *         section or the 'stats' section for modifiers).
     */
    public ConfigurationSection getEditedSection() {
        ConfigurationSection config = configFile.getConfig().getConfigurationSection(template.getId());
        Validate.notNull(config, "Could not find config section associated to the template '" + template.getType().getId() + "." + template.getId()
                + "': make sure the config section name is in capital letters");
        return config.getConfigurationSection(editedModifier == null ? "base" : "modifiers." + editedModifier.getId() + ".stats");
    }

    /**
     * Used in edition GUIs to display the current stat data of the edited
     * template.
     *
     * @param stat The stat which data we are looking for
     * @return Optional which contains the corresponding random stat data
     */
    public <R extends RandomStatData<S>, S extends StatData> Optional<R> getEventualStatData(ItemStat<R, S> stat) {

        /*
         * The item data map used to display what the player is currently
         * editing. If he is editing a stat modifier, use the modifier item data
         * map. Otherwise, use the base item data map
         */
        Map<ItemStat, RandomStatData> map = editedModifier != null ? editedModifier.getItemData() : template.getBaseItemData();
        return map.containsKey(stat) ? Optional.of((R) map.get(stat)) : Optional.empty();
    }

    public void registerTemplateEdition() {
        configFile.registerTemplateEdition(template);

        /*
         * After the edition was registered, update the
         * cached itemStack before opening the menu again
         *
         * Some more optimization could be done here by only recalculating the
         * item in the GUI that corresponds to the stat that was edited but it's
         * pretty much useless since the heaviest step is to regenerate the item
         */
        template = MMOItems.plugin.getTemplates().getTemplate(template.getType(), template.getId());
        editedModifier = editedModifier != null ? template.getModifier(editedModifier.getId()) : null;

        /*
         * The instant task brings the Bukkit event call from the
         * updateCachedItem() method back to SYNC since registerTemplateEdition()
         * is called ASYNC inside of an AsyncChatEvent
         */
        Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {
            updateCachedItem();
            open();
        });
    }

    /**
     * Method used when the player gets the item using the chest item so that he
     * can reroll the stats.
     */
    public void updateCachedItem() {
        cachedItem = template.newBuilder(PlayerData.get(getPlayer()).getRPG()).build().newBuilder().buildSilently();
    }

    public ItemStack getCachedItem() {
        if (cachedItem != null)
            return cachedItem;

        updateCachedItem();
        return cachedItem;
    }

    public void addEditionInventoryItems(Inventory inv, boolean displayBack) {
        ItemStack get = new ItemStack(Material.CHEST);
        ItemMeta getMeta = get.getItemMeta();
        getMeta.addItemFlags(ItemFlag.values());
        getMeta.setDisplayName(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub);
        List<String> getLore = new ArrayList<>();
        getLore.add(ChatColor.GRAY + "");
        getLore.add(ChatColor.GRAY + "You may also use /mi give " + template.getType().getId() + " " + template.getId());
        getLore.add(ChatColor.GRAY + "");
        getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to get the item.");
        getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to reroll its stats.");
        getMeta.setLore(getLore);
        get.setItemMeta(getMeta);

        if (displayBack) {
            ItemStack back = new ItemStack(Material.BARRIER);
            ItemMeta backMeta = back.getItemMeta();
            backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Back");
            back.setItemMeta(backMeta);

            inv.setItem(6, back);
        }

        inv.setItem(2, get);
        inv.setItem(4, getCachedItem());
    }

    public void open(int page) {
        previousPage = page;
        open();
    }

    public int getPreviousPage() {
        return previousPage;
    }

    @NotNull
    final FriendlyFeedbackProvider ffp;

    @NotNull
    public FriendlyFeedbackProvider getFFP() {
        return ffp;
    }
}
