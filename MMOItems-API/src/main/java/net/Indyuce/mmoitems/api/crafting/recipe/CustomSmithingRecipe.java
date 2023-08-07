package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.crafting.ingredients.*;
import io.lumine.mythic.lib.api.crafting.outputs.MRORecipe;
import io.lumine.mythic.lib.api.crafting.outputs.MythicRecipeOutput;
import io.lumine.mythic.lib.api.crafting.recipes.MythicCachedResult;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipe;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.VanillaInventoryMapping;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
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
    public CustomSmithingRecipe(@NotNull MMOItemTemplate outputItem, boolean dropGemstones, @NotNull SmithingCombinationType enchantmentTreatment, @NotNull SmithingCombinationType upgradeTreatment, int outputAmount) {
        this.outputItem = outputItem;
        this.dropGemstones = dropGemstones;
        this.enchantmentTreatment = enchantmentTreatment;
        this.upgradeTreatment = upgradeTreatment;
        this.outputAmount = outputAmount;
    }

    //region Advanced Variant

    /**
     * If this is not null, then the ingredients themselves will change as this output resolves
     * (like milk buckets turning into normal buckets when crafting a cake).
     */
    @Nullable
    MythicRecipe mainInputConsumption;
    /**
     * If this is not null, then the ingredients themselves will change as this output resolves
     * (like milk buckets turning into normal buckets when crafting a cake).
     */
    @Nullable public MythicRecipe getMainInputConsumption() { return mainInputConsumption; }
    /**
     * @param mic If this is not null, then the ingredients themselves will change as this output resolves
     *            (like milk buckets turning into normal buckets when crafting a cake).
     */
    public void setMainInputConsumption(@Nullable MythicRecipe mic) {  mainInputConsumption = nullifyIfEmpty(mic); }

    /**
     * @return If the ingredients themselves will change as this output resolves
     *         (like milk buckets turning into normal buckets when crafting a cake).
     */
    public boolean hasInputConsumption() { return ingotInputConsumption != null || mainInputConsumption != null; }

    /**
     * @param mic Some mythic recipe
     *
     * @return <code>null</code> if there is not a single actual item in this MythicRecipe,
     *         or the MythicRecipe itself.
     */
    @Nullable public MythicRecipe nullifyIfEmpty(@Nullable MythicRecipe mic) {

        // Null is just null
        if (mic == null) { return null; }

        // Anything not air will count a success
        for (MythicRecipeIngredient mri : mic.getIngredients()) {
            if (mri == null) { continue; }
            if (mri.getIngredient().isDefinesItem()) { return mic; } }

        // Nope, nothing that wasn't air
        return null;
    }

    /**
     * If this is not null, then the ingredients themselves will change as this output resolves
     * (like milk buckets turning into normal buckets when crafting a cake).
     */
    @Nullable
    MythicRecipe ingotInputConsumption;
    /**
     * If this is not null, then the ingredients themselves will change as this output resolves
     * (like milk buckets turning into normal buckets when crafting a cake).
     */
    @Nullable public MythicRecipe getIngotInputConsumption() { return ingotInputConsumption; }
    /**
     * @param mic If this is not null, then the ingredients themselves will change as this output resolves
     *            (like milk buckets turning into normal buckets when crafting a cake).
     */
    public void setIngotInputConsumption(@Nullable MythicRecipe mic) { ingotInputConsumption = nullifyIfEmpty(mic); }


    /**
     * Generates a new, independent MythicRecipeInventory
     * from the recipe, with random output where possible.
     *
     * @return A new result to be given to the player.
     */
    @NotNull MythicRecipeInventory generateResultOf(@NotNull MythicRecipe mythicRecipe) {

        // Rows yes
        HashMap<Integer, ItemStack[]> rowsInformation = new HashMap<>();

        // Ok it doesn't exist lets build it
        for (MythicRecipeIngredient mmIngredient : mythicRecipe.getIngredients()) {

            // Ignore
            if (mmIngredient == null) { continue; }

            // Identify Ingredient
            ShapedIngredient shaped = ((ShapedIngredient) mmIngredient);
            MythicIngredient ingredient = mmIngredient.getIngredient();

            // Does not define an item? I sleep
            if (!ingredient.isDefinesItem()) { continue; }

            // Any errors yo?
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
            ffp.activatePrefix(true, "Recipe of " + getOutputItem().getType().getId() + " " + getOutputItem().getId());

            /*
             * First we must get the material of the base, a dummy
             * item basically (since this is for display) which we
             * may only display if its the only substitute of this
             * ingredient.
             *
             * If the ingredient has more substitutes, the ingredient
             * description will be used instead, replacing the meta of
             * this item entirely.
             */
            ItemStack gen = mmIngredient.getIngredient().getRandomSubstituteItem(ffp);

            // Valid?
            if (gen != null) {

                // Get current row
                ItemStack[] row = rowsInformation.get(-shaped.getVerticalOffset());
                if (row == null) { row = new ItemStack[(shaped.getHorizontalOffset() + 1)]; }
                if (row.length < (shaped.getHorizontalOffset() + 1)) {
                    ItemStack[] newRow = new ItemStack[(shaped.getHorizontalOffset() + 1)];
                    //noinspection ManualArrayCopy
                    for (int r = 0; r < row.length; r++) { newRow[r] = row[r]; }
                    row = newRow;
                }

                // Yes
                row[shaped.getHorizontalOffset()] = gen;

                // Put
                rowsInformation.put(-shaped.getVerticalOffset(), row);

                // Log those
            } else {

                // All those invalid ones should log.
                ffp.sendTo(FriendlyFeedbackCategory.ERROR, MythicLib.plugin.getServer().getConsoleSender());
            }
        }

        // Add all rows into new
        MythicRecipeInventory ret = new MythicRecipeInventory();
        for (Integer h : rowsInformation.keySet()) { ret.setRow(h, rowsInformation.get(h)); }

        // Yes
        return ret;
    }
    //endregion

    /**
     * The amount of output produced by one smithing
     */
    int outputAmount;
    /**
     * @return The amount of output produced by one smithing
     */
    public int getOutputAmount() { return outputAmount; }
    /**
     * @param amount The amount of output produced by one smithing
     */
    public void setOutputAmount(int amount) { outputAmount = amount; }

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
            if (MMOItems.getType(itemNBT) != null) { return new LiveMMOItem(item); } }

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

        // Get the display
        MMOItem display = fromCombinationWith(item, ingot, (Player) eventTrigger.getWhoClicked(), null);

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
        Player player = (Player) eventTrigger.getWhoClicked();

        // Get the two combinant items
        ItemStack item = otherInventories.getMainInventory().getFirst();
        ItemStack ingot = firstFromFirstSide(otherInventories);

        // Get the display
        Ref<ArrayList<ItemStack>> droppedGemstones = new Ref<>();
        MMOItem display = fromCombinationWith(item, ingot, player, droppedGemstones);

        //RDR// MythicCraftingManager.log("\u00a78RDR \u00a748\u00a77 Custom Smithing Recipe Result\u00a7e" + times + "\u00a77 times\u00a78 ~\u00a71 " + eventTrigger.getAction().toString());

        /*
         * Crafting the item only once allows to put it in the cursor.
         *
         * Otherwise, this stuff will have to
         * 1 Calculate how many times until it runs out of inventory slots
         * 2 Move it to those inventory slots
         */
        if (times == 1 && (eventTrigger.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

            // Result
            MythicRecipeInventory result = otherInventories.getResultInventory().clone();
            result.setItemAt(map.getResultWidth(map.getResultInventoryStart()), map.getResultHeight(map.getResultInventoryStart()), display.newBuilder().build());

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
            processInventory(resultInventory, result, 1);
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

            // Consume ingredients
            consumeIngredients(otherInventories, cache, eventTrigger.getInventory(), map, 1);

            /*
             * Ok now, the ingredients have been consumed, the item is now in the cursor of the player.
             *
             * We must now read each of the affected inventories again and apply them with changes.
             */
            if (hasInputConsumption()) {

                // Items to spit back to the player
                ArrayList<ItemStack> inputConsumptionOverflow = new ArrayList<>();

                // Changes in the main inventory?
                if (getMainInputConsumption() != null) {

                    // Extract the new values
                    MythicRecipeInventory mainRead = map.getMainMythicInventory(eventTrigger.getInventory());

                    // Generate a result from the main input consumption
                    MythicRecipeInventory addedStuff = generateResultOf(getMainInputConsumption());

                    // Include overflow
                    inputConsumptionOverflow.addAll(MRORecipe.stackWhatsPossible(mainRead, addedStuff));

                    // Apply
                    map.applyToMainInventory(eventTrigger.getInventory(), mainRead, false);
                }

                // Changes in the main inventory?
                if (getIngotInputConsumption() != null) {

                    // Extract the new values
                    MythicRecipeInventory sideRead = map.getSideMythicInventory("ingot", eventTrigger.getInventory());

                    // Generate a result from the main input consumption
                    MythicRecipeInventory addedStuff = generateResultOf(getIngotInputConsumption());

                    // Include overflow
                    inputConsumptionOverflow.addAll(MRORecipe.stackWhatsPossible(sideRead, addedStuff));

                    // Apply
                    map.applyToSideInventory(eventTrigger.getInventory(), sideRead, "ingot", false);
                }

                // Distribute in inventory call
                MRORecipe.distributeInInventoryOrDrop(eventTrigger.getWhoClicked().getInventory(), inputConsumptionOverflow, eventTrigger.getWhoClicked().getLocation());
            }

        // Player is crafting to completion - move to inventory style.
        } else {

            /*
             * Set the result into the result slots.
             */
            //RDR//MythicCraftingManager.log("\u00a78RDR \u00a747\u00a77 Reading/Generating Result");

            // Build the result
            HashMap<Integer, ItemStack> modifiedInventory = null;
            Inventory inven = player.getInventory();
            int trueTimes = 0;

            // For every time
            for (int t = 1; t <= times; t++) {
                //RDR//MythicCraftingManager.log("\u00a78RDR \u00a748\u00a77 Iteration \u00a7c#" + t);

                // Result
                MythicRecipeInventory result = otherInventories.getResultInventory().clone();
                result.setItemAt(map.getResultWidth(map.getResultInventoryStart()), map.getResultHeight(map.getResultInventoryStart()), display.newBuilder().build());
                ArrayList<ItemStack> localOutput = MRORecipe.toItemsList(result);

                //RR//for (String str : localResult.toStrings("\u00a78Result \u00a79RR-")) { io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager.log(str); }

                /*
                 * Is this generating other kinds of output? Account for them
                 */
                if (hasInputConsumption()) {

                    // Changes in the main inventory?
                    if (getMainInputConsumption() != null) {

                        // Generate a result from the main input consumption
                        MythicRecipeInventory addedStuff = generateResultOf(getMainInputConsumption());

                        // Add these to the output
                        localOutput.addAll(MRORecipe.toItemsList(addedStuff));
                    }

                    // Changes in the ingot inventory?
                    if (getIngotInputConsumption() != null) {

                        // Generate a result from the main input consumption
                        MythicRecipeInventory addedStuff = generateResultOf(getIngotInputConsumption());

                        // Add these to the output
                        localOutput.addAll(MRORecipe.toItemsList(addedStuff));
                    }
                }

                // Send to
                HashMap<Integer, ItemStack> localIterationResult = MRORecipe.distributeInInventory(inven, localOutput, modifiedInventory);

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

            // Consume ingredients
            consumeIngredients(otherInventories, cache, eventTrigger.getInventory(), map, times);
        }

        // Drop?
        if (isDropGemstones() && (droppedGemstones.getValue() != null) && (player.getLocation().getWorld() != null)) {

            // Give the gems back
            for (ItemStack drop : player.getInventory().addItem(
                    droppedGemstones.getValue().toArray(new ItemStack[0])).values()) {

                // Not air right
                if (SilentNumbers.isAir(drop)) { continue; }

                // Drop to the world
                player.getWorld().dropItem(player.getLocation(), drop); } }
    }

    /**
     * @param itemStack The item you are upgrading
     * @param ingotStack The second item you are upgrading
     *
     * @return What would the output be if combined with this other MMOItem?
     */
    @NotNull MMOItem fromCombinationWith(@Nullable ItemStack itemStack, @Nullable ItemStack ingotStack, @NotNull Player p, @Nullable Ref<ArrayList<ItemStack>> rem) {

        // Read MMOItems
        MMOItem item = fromStack(itemStack);
        MMOItem ingot = fromStack(ingotStack);

        // Generate
        MMOItem gen = getOutputItem().newBuilder(0, null).build();

        /*
         * Things must be merged:
         *
         * 1 Gem Stones - Checks which gem stones can still fit
         *
         * 2 Upgrades - Performs an operation with the combination of them both
         *
         * 3 Enchantments - Operation witht he combination of them both
         */

        // Extract gemstones
        ArrayList<MMOItem> compGemstones = new ArrayList<>();
        if (item != null)
            item.extractGemstones().forEach(pair -> compGemstones.add(pair.getValue()));
        if (ingot != null)
            ingot.extractGemstones().forEach(pair -> compGemstones.add(pair.getValue()));

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
            if (res.getType() == GemStone.ResultType.SUCCESS && (res.getResultAsMMOItem() != null)) {

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
        //ECH// MMOItems.log("§8Smith §8ENCH§7 Combination: \u00a73" + getEnchantmentTreatment().toString());
        if (getEnchantmentTreatment() != SmithingCombinationType.NONE) {

            // Get enchantment data
            EnchantListData genEnchants = (EnchantListData) gen.getData(ItemStats.ENCHANTS); if (genEnchants == null) { genEnchants = (EnchantListData) ItemStats.ENCHANTS.getClearStatData(); }
            EnchantListData itemEnchants = item != null ? (EnchantListData) item.getData(ItemStats.ENCHANTS) : Enchants.fromVanilla(itemStack);
            EnchantListData ingotEnchants = ingot != null ? (EnchantListData) ingot.getData(ItemStats.ENCHANTS) : Enchants.fromVanilla(ingotStack);

            // For every enchant
            for (Enchantment observedEnchantment : Enchantment.values()) {
                //ECH// MMOItems.log("§8Smith §8ENCH§7 Treating \u00a7b" + observedEnchantment.getName());

                int genLevel = genEnchants.getLevel(observedEnchantment);
                int itemLevel = itemEnchants.getLevel(observedEnchantment);
                int ingotLevel = ingotEnchants.getLevel(observedEnchantment);
                int finalLevel;

                //ECH// MMOItems.log("§8Smith §8ENCH§7 Gen:\u00a73 " + genLevel + "\u00a77, Item:\u00a7e " + itemLevel + "\u00a77, Ingot:\u00a7b " + ingotLevel);

                switch (getEnchantmentTreatment()) {
                    case ADDITIVE: finalLevel = itemLevel + ingotLevel + genLevel; break;
                    case MAXIMUM: if (genLevel == 0) { genLevel = itemLevel; } finalLevel = Math.max(genLevel, Math.max(itemLevel, ingotLevel)); break;
                    case MINIMUM: if (genLevel == 0) { genLevel = itemLevel; } finalLevel = Math.max(genLevel, Math.min(itemLevel, ingotLevel)); break;
                    default:  if (genLevel == 0) { finalLevel = SilentNumbers.ceil((itemLevel + ingotLevel) / 2D); } else { finalLevel = SilentNumbers.ceil((itemLevel + ingotLevel + genLevel) / 3D); }  break; }

                //ECH// MMOItems.log("§8Smith §8ENCH§7 Result: \u00a7a" + finalLevel);
                genEnchants.addEnchant(observedEnchantment, finalLevel);
            }

            // Set data :wazowksibruhmoment:
            gen.setData(ItemStats.ENCHANTS, genEnchants);
        }

        // All right whats the level stuff up now
        if (gen.hasUpgradeTemplate() && getUpgradeTreatment() != SmithingCombinationType.NONE) {

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

