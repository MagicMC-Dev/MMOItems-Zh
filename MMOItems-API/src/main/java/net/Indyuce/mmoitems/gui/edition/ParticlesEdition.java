package net.Indyuce.mmoitems.gui.edition;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.particle.api.ParticleType;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ParticlesEdition extends EditionInventory {
    public ParticlesEdition(Player player, MMOItemTemplate template) {
        super(player, template);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, "Particles E.: " + template.getId());
        int[] slots = {37, 38, 39, 40, 41, 42, 43};
        int n = 0;

        @Nullable ParticleType particleType = null;
        try {
            particleType = ParticleType.valueOf(getEditedSection().getString("item-particles.type"));
        } catch (Exception ignored) {
        }

        ItemStack particleTypeItem = VersionMaterial.PINK_STAINED_GLASS.toItem();
        ItemMeta particleTypeItemMeta = particleTypeItem.getItemMeta();
        particleTypeItemMeta.setDisplayName(ChatColor.GREEN + "Particle Pattern");
        List<String> particleTypeItemLore = new ArrayList<>();
        particleTypeItemLore.add(ChatColor.GRAY + "The particle pattern defines how");
        particleTypeItemLore.add(ChatColor.GRAY + "particles behave, what pattern they follow");
        particleTypeItemLore.add(ChatColor.GRAY + "when displayed or what shape they form.");
        particleTypeItemLore.add("");
        particleTypeItemLore.add(ChatColor.GRAY + "Current Value: "
                + (particleType == null ? ChatColor.RED + "No type selected." : ChatColor.GOLD + particleType.getDefaultName()));
        if (particleType != null) {
            particleTypeItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + particleType.getDescription());
        }
        particleTypeItemLore.add("");
        particleTypeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
        particleTypeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to change this value.");
        particleTypeItemMeta.setLore(particleTypeItemLore);
        particleTypeItem.setItemMeta(particleTypeItemMeta);

        if (particleType != null) {
            ConfigurationSection psection = getEditedSection().getConfigurationSection("item-particles");
            for (String modifier : particleType.getModifiers()) {
                ItemStack modifierItem = VersionMaterial.GRAY_DYE.toItem();
                ItemMeta modifierItemMeta = modifierItem.getItemMeta();
                modifierItemMeta.setDisplayName(ChatColor.GREEN + MMOUtils.caseOnWords(modifier.toLowerCase().replace("-", " ")));
                List<String> modifierItemLore = new ArrayList<>();
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "This is a pattern modifier.");
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "Changing this value will slightly");
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "customize the particle pattern.");
                modifierItemLore.add("");
                modifierItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD
                        + (psection.contains(modifier) ? psection.getDouble(modifier) : particleType.getModifier(modifier)));
                modifierItemMeta.setLore(modifierItemLore);
                modifierItem.setItemMeta(modifierItemMeta);

                modifierItem = NBTItem.get(modifierItem).addTag(new ItemTag("patternModifierId", modifier)).toItem();

                inv.setItem(slots[n++], modifierItem);
            }
        }

        @Nullable Particle particle = null;
        try {
            particle = Particle.valueOf(getEditedSection().getString("item-particles.particle"));
        } catch (Exception ignored) {
        }

        ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta particleItemMeta = particleItem.getItemMeta();
        particleItemMeta.setDisplayName(ChatColor.GREEN + "Particle");
        List<String> particleItemLore = new ArrayList<>();
        particleItemLore.add(ChatColor.GRAY + "Defines what particle is used");
        particleItemLore.add(ChatColor.GRAY + "in the particle effect.");
        particleItemLore.add("");
        particleItemLore.add(ChatColor.GRAY + "Current Value: " + (particle == null ? ChatColor.RED + "No particle selected."
                : ChatColor.GOLD + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " "))));
        particleItemLore.add("");
        particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
        particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to change this value.");
        particleItemMeta.setLore(particleItemLore);
        particleItem.setItemMeta(particleItemMeta);

        if (particle != null && MMOUtils.isColorable(particle)) {
            int red = getEditedSection().getInt("item-particles.color.red");
            int green = getEditedSection().getInt("item-particles.color.green");
            int blue = getEditedSection().getInt("item-particles.color.blue");

            ItemStack colorItem = VersionMaterial.RED_DYE.toItem();
            ItemMeta colorItemMeta = colorItem.getItemMeta();
            colorItemMeta.setDisplayName(ChatColor.GREEN + "Particle Color");
            List<String> colorItemLore = new ArrayList<>();
            colorItemLore.add(ChatColor.GRAY + "The RGB color of your particle.");
            colorItemLore.add("");
            colorItemLore.add(ChatColor.GRAY + "Current Value (R-G-B):");
            colorItemLore.add("" + ChatColor.RED + ChatColor.BOLD + red + ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green
                    + ChatColor.GRAY + " - " + ChatColor.BLUE + ChatColor.BOLD + blue);
            colorItemLore.add("");
            colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
            colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to change this value.");
            colorItemMeta.setLore(colorItemLore);
            colorItem.setItemMeta(colorItemMeta);

            inv.setItem(25, colorItem);
        }

        ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "- No Modifier -");
        glass.setItemMeta(glassMeta);

        while (n < slots.length)
            inv.setItem(slots[n++], glass);

        addEditionInventoryItems(inv, true);
        inv.setItem(21, particleTypeItem);
        inv.setItem(23, particleItem);

        return inv;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Particle")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL)
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle").enable("Write in the chat the particle you want.");

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.particle")) {
                    getEditedSection().set("item-particles.particle", null);
                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the particle.");
                }
            }
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Particle Color")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL)
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle-color").enable("Write in the chat the RGB color you want.",
                        ChatColor.AQUA + "Format: [RED] [GREEN] [BLUE]");

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.color")) {
                    getEditedSection().set("item-particles.color", null);
                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the particle color.");
                }
            }
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Particle Pattern")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle-type").enable("Write in the chat the particle type you want.");
                player.sendMessage("");
                player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Available Particles Patterns");
                for (ParticleType type : ParticleType.values())
                    player.sendMessage("* " + ChatColor.GREEN + type.name());
            }

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.type")) {
                    getEditedSection().set("item-particles.type", null);

                    // reset other modifiers
                    for (String key : getEditedSection().getConfigurationSection("item-particles").getKeys(false))
                        if (!key.equals("particle"))
                            getEditedSection().set("item-particles." + key, null);

                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the particle pattern.");
                }
            }
        }

        String tag = NBTItem.get(item).getString("patternModifierId");
        if (tag.equals(""))
            return;

        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(this, ItemStats.ITEM_PARTICLES, tag).enable("Write in the chat the value you want.");

        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (getEditedSection().contains("item-particles." + tag)) {
                getEditedSection().set("item-particles." + tag, null);
                registerTemplateEdition();
                player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset " + ChatColor.GOLD + tag + ChatColor.GRAY + ".");
            }
        }
    }
}