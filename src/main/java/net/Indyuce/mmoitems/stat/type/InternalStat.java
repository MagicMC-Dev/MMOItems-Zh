package net.Indyuce.mmoitems.stat.type;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.jetbrains.annotations.NotNull;

/**
 * Internal stats can be used to store specific item data and cannot be
 * edited in the item edition GUI since they only exist once the item is
 * physically generated.
 */
public interface InternalStat { }
