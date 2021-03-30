package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.crafting.ingredients.MythicBlueprintInventory;
import io.lumine.mythic.lib.api.crafting.ingredients.MythicRecipeInventory;
import io.lumine.mythic.lib.api.crafting.outputs.MRORecipe;
import io.lumine.mythic.lib.api.crafting.outputs.MythicRecipeOutput;
import io.lumine.mythic.lib.api.crafting.recipes.MythicCachedResult;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.VanillaInventoryMapping;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Intended for use with Anvil / Smithing station, the combination of
 * two MMOItems, or upgrading a MMOItem with an ingot, also combines the
 * gems and upgrade level of the components.
 *
 * @author Gunging
 */
public class CustomSmithingRecipe extends MythicRecipeOutput {

    /**
     * @param outputItem The MMOItem that results from the completion of these recipes.
     * @param dropGemstones Should extra gemstones be dropped? (Otherwise lost)
     * @param enchantmentTreatment Should enchantments be destroyed?
     * @param upgradeTreatment How will upgrades combine?
     */
    public CustomSmithingRecipe(@NotNull MMOItemTemplate outputItem, boolean dropGemstones, @NotNull SmithingCombinationType enchantmentTreatment, @NotNull SmithingCombinationType upgradeTreatment) {
        this.outputItem = outputItem;
        this.dropGemstones = dropGemstones;
        this.enchantmentTreatment = enchantmentTreatment;
        this.upgradeTreatment = upgradeTreatment; }


    /**
     * The MMOItem that results from the completion of these recipes.
     */
    @NotNull final MMOItemTemplate outputItem;
    /**
     * @return The MMOItem that results from the completion of these recipes.
     */
    @NotNull public MMOItemTemplate getOutputItem() { return outputItem; }

    /**
     * Will the extra gemstones be dropped to the ground?
     */
    final boolean dropGemstones;
    /**
     * Will the enchantments be...??
     */
    @NotNull final SmithingCombinationType enchantmentTreatment;
    /**
     * Will the enchantments be...??
     */
    @NotNull public SmithingCombinationType getEnchantmentTreatment() { return enchantmentTreatment; }

    /**
     * @return Will the extra gemstones be dropped to the ground?
     */
    public boolean isDropGemstones() { return dropGemstones; }

    /**
     * @return How to treat upgrade level combinations?
     */
    @NotNull
    public SmithingCombinationType getUpgradeTreatment() { return upgradeTreatment; }
    /**
     * How to treat upgrade level combinations?
     */
    @NotNull final SmithingCombinationType upgradeTreatment;

    /**
     * Is this a MMOItem? Well fetch
     *
     * @param item Item Stack to transform
     * @return a MMOItem if appropriate.
     */
    @Nullable MMOItem fromStack(@Nullable ItemStack item) {

        if (item == null) { return null; } else {

            NBTItem itemNBT = NBTItem.get(item);
            if (itemNBT.hasType()) { return new LiveMMOItem(item); } }

        return null; }
    /**
     * Is there at least one item in the first side inventory? Well git
     *
     * @param b Blueprint
     * @return an ItemStack if any exists
     */
    @Nullable ItemStack firstFromFirstSide(@NotNull MythicBlueprintInventory b) {

        // No sides?
        if (b.getSideInventoryNames().size() == 0) { return null; }

        // Get
        return b.getSideInventory(b.getSideInventoryNames().get(0)).getFirst(); }

    @NotNull
    @Override
    public MythicRecipeInventory applyDisplay(@NotNull MythicBlueprintInventory mythicRecipeInventory, @NotNull InventoryClickEvent eventTrigger, @NotNull VanillaInventoryMapping mapping) {

        // Not supported
        if (!(eventTrigger.getWhoClicked() instanceof Player)) { return mythicRecipeInventory.getResultInventory(); }

        // Get the two combinant items
        MythicBlueprintInventory original = mapping.extractFrom(eventTrigger.getView().getTopInventory());
        ItemStack item = original.getMainInventory().getFirst();
        ItemStack ingot = firstFromFirstSide(original);

        MMOItem itemMMO = fromStack(item);
        MMOItem ingotMMO = fromStack(ingot);

        // Get the display
        MMOItem display = fromCombinationWith(itemMMO, ingotMMO, (Player) eventTrigger.getWhoClicked(), null);

        // Result
        MythicRecipeInventory result = mythicRecipeInventory.getResultInventory().clone();
        result.setItemAt(mapping.getResultWidth(mapping.getResultInventoryStart()), mapping.getResultHeight(mapping.getResultInventoryStart()), display.newBuilder().build());

        // Set the display
        return result;
    }

    @Override
    public void applyResult(@NotNull MythicRecipeInventory resultInventory, @NotNull MythicBlueprintInventory otherInventories, @NotNull MythicCachedResult cache, @NotNull InventoryClickEvent eventTrigger, @NotNull VanillaInventoryMapping map, int times) {

        /*
         * Listen, we'll take it from here. Cancel the original event.
         *
         * Now run Mythic Crafting version of the event.
         * Did anyone cancel it? Well I guess we'll touch nothing then, but you also cant craft this item >:I
         */
        eventTrigger.setCancelled(true);
        if (!(eventTrigger.getWhoClicked() instanceof Player)) { return; }

        // Get the two combinant items
        ItemStack item = otherInventories.getMainInventory().getFirst();
        ItemStack ingot = firstFromFirstSide(otherInventories);

        MMOItem itemMMO = fromStack(item);
        MMOItem ingotMMO = fromStack(ingot);

        // Get the display
        Ref<ArrayList<ItemStack>> droppedGemstones = new Ref<>();
        MMOItem display = fromCombinationWith(itemMMO, ingotMMO, (Player) eventTrigger.getWhoClicked(), droppedGemstones);

        // Result
        MythicRecipeInventory result = otherInventories.getResultInventory().clone();
        result.setItemAt(map.getResultWidth(map.getResultInventoryStart()), map.getResultHeight(map.getResultInventoryStart()), display.newBuilder().build());

        /*
         * Crafting the item only once allows to put it in the cursor.
         *
         * Otherwise, this stuff will have to
         * 1 Calculate how many times until it runs out of inventory slots
         * 2 Move it to those inventory slots
         */
        if (times == 1 && (eventTrigger.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

            /*
             * When crafting with the cursor, we must make sure that:
             *
             * 1 The player is holding nothing in the cursor
             * or
             *
             * 2 The item in their cursor is stackable with the result
             * and
             * 3 The max stacks would not be exceeded
             */
            ItemStack currentInCursor = eventTrigger.getCursor();

            /*
             * Set the result into the result slots.
             */

            //RR//for (String str : result.toStrings("\u00a78Result \u00a79RR-")) { MythicCraftingManager.log(str); }

            // Apply the result
            //RDR//MythicCraftingManager.log("\u00a78RDR \u00a748\u00a77 Processing Result Inventory");
            processInventory(resultInventory, result, times);
            //RR//for (String str : resultInventory.toStrings("\u00a78Result \u00a79PR-")) { MythicCraftingManager.log(str); }

            //RDR//MythicCraftingManager.log("\u00a78RDR \u00a749\u00a77 Finding item to put on cursor");

            // Get the zeroth entry, which will be put in the players cursor >:o
            ItemStack cursor = resultInventory.getItemAt(map.getResultWidth(eventTrigger.getSlot()), map.getResultHeight(eventTrigger.getSlot()));
            if (cursor == null) { cursor = new ItemStack(Material.AIR); }
            ItemStack actualCursor = cursor.clone();

            /*
             * All right, so, can the actual cursor stack with the current?
             */
            if (!SilentNumbers.isAir(currentInCursor)) {

                // Aye so they could stack
                if (currentInCursor.isSimilar(actualCursor)) {

                    // Exceeds max stacks?
                    int cAmount = currentInCursor.getAmount();
                    int aAmount = actualCursor.getAmount();
                    int maxAmount = actualCursor.getMaxStackSize();

                    // Cancel if their sum would exceed the max
                    if (cAmount + aAmount > maxAmount) { return; }

                    // All right recalculate amount then
                    actualCursor.setAmount(cAmount + aAmount);

                } else {

                    // Cancel this operation
                    return;
                }
            }

            //RR//MythicCraftingManager.log("\u00a78Result \u00a74C\u00a77 Found for cursor " + SilentNumbers.getItemName(actualCursor));

            // Deleting original item (now its going to be on cursor so)
            cursor.setAmount(0);

            // Apply result to the inventory
            //RDR//MythicCraftingManager.log("\u00a78RDR \u00a7410\u00a77 Actually applying to result inventory through map");
            map.applyToResultInventory(eventTrigger.getInventory(), resultInventory, false);

            // Apply result to the cursor
            eventTrigger.getView().setCursor(actualCursor);

            // Player is crafting to completion - move to inventory style.
        } else {

            /*
             * Set the result into the result slots.
             */
            //RDR//MythicCraftingManager.log("\u00a78RDR \u00a747\u00a77 Reading/Generating Result");

            // Build the result
            ArrayList<ItemStack> outputItems = MRORecipe.toItemsList(result);
            HashMap<Integer, ItemStack> modifiedInventory = null;
            Inventory inven = eventTrigger.getWhoClicked().getInventory();
            int trueTimes = 0;

            // For every time
            for (int t = 1; t <= times; t++) {
                //RDR//MythicCraftingManager.log("\u00a78RDR \u00a748\u00a77 Iteration \u00a7c#" + t);

                //RR//for (String str : localResult.toStrings("\u00a78Result \u00a79RR-")) { io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager.log(str); }

                // Send to
                HashMap<Integer, ItemStack> localIterationResult = MRORecipe.distributeInInventory(inven, outputItems, modifiedInventory);

                // Failed? Break
                if (localIterationResult == null) {

                    // No changes in the modified inventory, just break
                    //RR//io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager.log("\u00a78Result \u00a7cIC\u00a77 Iteration Cancelled: \u00a7cNo Inventory Space");
                    break;

                    // Prepare for next iteration
                } else {

                    // Store changes
                    modifiedInventory = localIterationResult;
                    trueTimes = t;
                    //RR//io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager.log("\u00a78Result \u00a7aIV\u00a77 Iteration Validated, total times: \u00a7a" + trueTimes);
                } }

            // True times is 0? Cancel this.
            if (trueTimes == 0) { return; }

            // All right apply
            times = trueTimes;
            for (Integer s : modifiedInventory.keySet()) {

                // Get Item
                ItemStack putt = modifiedInventory.get(s);
                //RR//io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager.log("\u00a78Result \u00a79IS\u00a77 Putting \u00a7b@" + s + "\u00a77 a " + SilentNumbers.getItemName(putt));

                // Set
                inven.setItem(s, putt); }
        }

        // Drop?
        if (isDropGemstones() && (droppedGemstones.getValue() != null) && (eventTrigger.getWhoClicked().getLocation().getWorld() != null)) {
            Location l = eventTrigger.getWhoClicked().getLocation();

            // Drop each yea
            for (ItemStack gem : droppedGemstones.getValue()) {

                if (SilentNumbers.isAir(gem)) { continue; }

                // God damn drop yo
                l.getWorld().dropItemNaturally(l, gem);
            } }

        // Consume ingredients
        consumeIngredients(otherInventories, cache, eventTrigger.getInventory(), map, times);
    }

    /**
     * @param item The item you are upgrading
     * @param ingot The second item you are upgrading
     *
     * @return What would the output be if combined with this other MMOItem?
     */
    @NotNull MMOItem fromCombinationWith(@Nullable MMOItem item, @Nullable MMOItem ingot, @NotNull Player p, @Nullable Ref<ArrayList<ItemStack>> rem) {

        // Generate
        MMOItem gen = getOutputItem().newBuilder(0, null).build();

        /*
         * Two things must be merged:
         *
         * 1 Gem Stones - Checks which gem stones can still fit
         *
         * 2 Upgrades - Performs an operation with the combination of them both
         */

        // Extract gemstones
        ArrayList<MMOItem> compGemstones = new ArrayList<>();
        if (item != null) { compGemstones.addAll(item.extractGemstones()); }
        if (ingot != null) { compGemstones.addAll(ingot.extractGemstones()); }

        // Which stones would not fit
        ArrayList<ItemStack> remainingStones = new ArrayList<>();
        for (MMOItem m : compGemstones) {
            //GEM// MMOItems.log("\u00a76 +\u00a77 Fitting \u00a7e" + m.getType().toString() + " " + m.getId());

            // What sockets are available?
            GemSocketsData genGemstones = (GemSocketsData) gen.getData(ItemStats.GEM_SOCKETS);

            // Abort lol
            if (genGemstones == null || (genGemstones.getEmptySlots().size() == 0)) {
                //GEM// MMOItems.log("\u00a7c !!\u00a77 Dropping: No more empty slots in target ");

                // Just keep as 'remaining'
                remainingStones.add(m.newBuilder().build());
                continue; }

            // Ok proceed
            GemStone asGem = new GemStone(p, m.newBuilder().buildNBT());

            // Put
            GemStone.ApplyResult res = asGem.applyOntoItem(gen, gen.getType(), "", false, true);

            // None?
            if (res.getType().equals(GemStone.ResultType.SUCCESS) && (res.getResultAsMMOItem() != null)) {

                // Success that's nice
                gen = res.getResultAsMMOItem();
                //GEM// MMOItems.log("\u00a7a W\u00a77 Socketed! ");

            // Didn't fit L
            } else {
                //GEM// MMOItems.log("\u00a7e !!\u00a77 Dropping: Does not fit socket ");
                remainingStones.add(m.newBuilder().build()); } }

        // Set value
        Ref.setValue(rem, remainingStones);

        // Enchantments?
        if (!getEnchantmentTreatment().equals(SmithingCombinationType.NONE)) {

            // Get enchantment data
            EnchantListData genEnchants = (EnchantListData) gen.getData(ItemStats.ENCHANTS);
            EnchantListData itemEnchants = item != null ? (EnchantListData) item.getData(ItemStats.ENCHANTS) : (EnchantListData) ItemStats.ENCHANTS.getClearStatData();
            EnchantListData ingotEnchants = ingot != null ? (EnchantListData) ingot.getData(ItemStats.ENCHANTS) : (EnchantListData) ItemStats.ENCHANTS.getClearStatData();

            // For every enchant
            for (Enchantment observedEnchantment : Enchantment.values()) {

                int genLevel = genEnchants.getLevel(observedEnchantment);
                int itemLevel = itemEnchants.getLevel(observedEnchantment);
                int ingotLevel = ingotEnchants.getLevel(observedEnchantment);
                int finalLevel;

                switch (getEnchantmentTreatment()) {
                    case ADDITIVE: finalLevel = itemLevel + ingotLevel + genLevel; break;
                    case MAXIMUM: if (genLevel == 0) { genLevel = itemLevel; } finalLevel = Math.max(genLevel, Math.max(itemLevel, ingotLevel)); break;
                    case MINIMUM: if (genLevel == 0) { genLevel = itemLevel; } finalLevel = Math.max(genLevel, Math.min(itemLevel, ingotLevel)); break;
                    default:  if (genLevel == 0) { finalLevel = SilentNumbers.ceil((itemLevel + ingotLevel) / 2D); } else { finalLevel = SilentNumbers.ceil((itemLevel + ingotLevel + genLevel) / 3D); }  break; }

                genEnchants.addEnchant(observedEnchantment, finalLevel);
            }
        }

        // All right whats the level stuff up now
        if (gen.hasUpgradeTemplate() && !(getUpgradeTreatment().equals(SmithingCombinationType.NONE))) {

            // All right get the levels of them both
            int itemLevel = 0; if (item != null) { itemLevel = item.getUpgradeLevel(); }
            int ingotLevel = 0; if (ingot != null) { ingotLevel = ingot.getUpgradeLevel(); }
            int finalLevel;

            switch (getUpgradeTreatment()) {
                case ADDITIVE: finalLevel = itemLevel + ingotLevel; break;
                case MAXIMUM: finalLevel = Math.max(itemLevel, ingotLevel); break;
                case MINIMUM: finalLevel = Math.min(itemLevel, ingotLevel); break;
                default: finalLevel = SilentNumbers.ceil((itemLevel + ingotLevel) / 2D); break; }

            // Upgrade yes
            gen.getUpgradeTemplate().upgradeTo(gen, Math.min(finalLevel, gen.getMaxUpgradeLevel())); }

        // That's it
        return gen;
    }
}

