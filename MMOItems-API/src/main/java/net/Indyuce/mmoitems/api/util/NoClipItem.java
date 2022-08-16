package net.Indyuce.mmoitems.api.util;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;

import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;

public class NoClipItem implements Listener {
	private final Item item;

	/**
	 * Util class which creates an item which cannot be picked up. Item is
	 * removed if it tries to go through a nether portal
	 *
	 * @param loc
	 *            Spawn location of the item
	 * @param item
	 *            ItemStack used to summon the entity
	 */
	public NoClipItem(Location loc, ItemStack item) {
		this.item = loc.getWorld().dropItem(loc, stripItemData(item));
		this.item.setPickupDelay(1000000);

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	public Item getEntity() {
		return item;
	}

	public void close() {
		item.remove();

		EntityPortalEnterEvent.getHandlerList().unregister(this);
		InventoryPickupItemEvent.getHandlerList().unregister(this);
		EntityPickupItemEvent.getHandlerList().unregister(this);
	}

	// Stops items from being picked up by hoppers/portals and then duping them.
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(InventoryPickupItemEvent event) {
		if (event.getItem().equals(item)) {
			event.setCancelled(true);
			close();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void b(EntityPortalEnterEvent event) {
		if (event.getEntity().equals(item)) {
			close();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void c(EntityPickupItemEvent event) {
		if (event.getItem().equals(item)) {
			close();
		}
	}

	/*
	 * Method used to strip item data from the ItemStack.
	 *
	 * This creates a new ItemStack with only data needed for the item to look and work like normal
	 * such as its Material, Custom Model Data, Enchantments (Shiny look), Glow and Glow Color from its tier,
	 * skull textures, leather colors and also most importantly adds "MMOITEMS_NO_CLIP_ITEM" to
	 * its NBT so even *IF* the items are duped we can and server owners can detect these items super, super easily.
	 * https://i.imgur.com/oLjkeoD.png
	 */
	private ItemStack stripItemData(ItemStack oldItem) {
		final NBTItem oldItemNBT = MythicLib.plugin.getVersion().getWrapper().getNBTItem(oldItem);

		// Make new item.
		final ItemStack newItem = new ItemStack(oldItem.getType());
		final ItemMeta newItemMeta = newItem.getItemMeta();
		newItem.setAmount(1);

		/*
		 * Copy Enchantments
		 * Adds Luck 0 if the item has any enchantments so it looks shiny.
		 * Hiding the enchantment doesn't really matter but thought it'll be better
		 * to look like a vanilla item if a player somehow picks it up and Luck 0 does nothing.
		 */
		if (oldItem.getItemMeta().hasEnchants()) {
			newItemMeta.addEnchant(Enchantment.LUCK, 0, true);
			newItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		// Copy CustomModelData. Only if 1.14+
		if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13) && oldItem.getItemMeta().hasCustomModelData() && oldItem.getItemMeta().getCustomModelData() != 0) {
			newItemMeta.setCustomModelData(oldItem.getItemMeta().getCustomModelData());
		}

		// Copy Skull textures
		if (oldItem.getItemMeta() instanceof SkullMeta) {
			try {
				final Field oldProfileField = oldItem.getItemMeta().getClass().getDeclaredField("profile");
				oldProfileField.setAccessible(true);
				final GameProfile oldProfile = (GameProfile) oldProfileField.get(oldItem.getItemMeta());

				final Field profileField = newItemMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(newItemMeta, oldProfile);
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not set skull texture on stripItemData method in the NoClipItem class. Please report this issue!");
			}
		}

		// Copy Leather colors
		if (oldItem.getItemMeta() instanceof LeatherArmorMeta && newItemMeta instanceof LeatherArmorMeta) {
			((LeatherArmorMeta) newItemMeta).setColor(((LeatherArmorMeta) oldItem.getItemMeta()).getColor());
		}

		// Set ItemMeta and get item as NBTItem to continue work.
		newItem.setItemMeta(newItemMeta);
		final NBTItem newItemNBT = MythicLib.plugin.getVersion().getWrapper().getNBTItem(newItem);

		// Copy Tier for item Glow.
		newItemNBT.addTag(new ItemTag("MMOITEMS_TIER", oldItemNBT.getString("MMOITEMS_TIER").trim().isEmpty() ? null : oldItemNBT.getString("MMOITEMS_TIER")));

		// Make them not stack together, we NEVER want them to stack. Was only used on Throw up.
		final Random random = new Random();
		newItemNBT.addTag(new ItemTag("MMOITEMS_NO_STACK", random.nextInt(Integer.MAX_VALUE)));

		// Safety tag
		newItemNBT.addTag(new ItemTag("MMOITEMS_NO_CLIP_ITEM", true));

		return newItemNBT.toItem();
	}
}
