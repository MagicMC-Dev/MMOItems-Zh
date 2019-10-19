package net.Indyuce.mmoitems.version.nms;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.Indyuce.mmoitems.api.item.NBTItem;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.ContainerAnvil;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.SoundEffectType;
import net.minecraft.server.v1_12_R1.World;

public class NMSHandler_1_12_R1 implements NMSHandler {
	@Override
	public void sendJson(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message)));
	}

	@Override
	public void sendTitle(Player player, String msgTitle, String msgSubTitle, int fadeIn, int ticks, int fadeOut) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\": \"" + msgTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\": \"" + msgSubTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, ticks, fadeOut));
	}

	@Override
	public void sendActionBar(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"), ChatMessageType.GAME_INFO));
	}

	@Override
	public int getNextContainerId(Player player) {
		return ((CraftPlayer) player).getHandle().nextContainerCounter();
	}

	@Override
	public void handleInventoryCloseEvent(Player player) {
		CraftEventFactory.handleInventoryCloseEvent(((CraftPlayer) player).getHandle());
	}

	@Override
	public void sendPacketOpenWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
	}

	@Override
	public void sendPacketCloseWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
	}

	@Override
	public void setActiveContainerDefault(Player player) {
		((CraftPlayer) player).getHandle().activeContainer = ((CraftPlayer) player).getHandle().defaultContainer;
	}

	@Override
	public void setActiveContainer(Player player, Object container) {
		((CraftPlayer) player).getHandle().activeContainer = (Container) container;
	}

	@Override
	public void setActiveContainerId(Object container, int containerId) {
		((Container) container).windowId = containerId;
	}

	@Override
	public void addActiveContainerSlotListener(Object container, Player player) {
		((Container) container).addSlotListener(((CraftPlayer) player).getHandle());
	}

	@Override
	public Inventory toBukkitInventory(Object container) {
		return ((Container) container).getBukkitView().getTopInventory();
	}

	@Override
	public Object newContainerAnvil(Player player) {
		return new AnvilContainer(((CraftPlayer) player).getHandle());
	}

	private class AnvilContainer extends ContainerAnvil {
		public AnvilContainer(EntityHuman entityhuman) {
			super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
			this.checkReachable = false;
		}
	}

	@Override
	public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
		return new NBTItem_v1_12_1(item);
	}

	public class NBTItem_v1_12_1 extends NBTItem {
		private ItemStack nms;
		private NBTTagCompound compound;

		public NBTItem_v1_12_1(org.bukkit.inventory.ItemStack item) {
			super(item);
			nms = CraftItemStack.asNMSCopy(item);
			compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
		}

		@Override
		public String getString(String path) {
			return compound.getString(path);
		}

		@Override
		public boolean hasTag(String path) {
			return compound.hasKey(path);
		}

		@Override
		public boolean getBoolean(String path) {
			return compound.getBoolean(path);
		}

		@Override
		public double getDouble(String path) {
			return compound.getDouble(path);
		}

		@Override
		public int getInteger(String path) {
			return compound.getInt(path);
		}

		@Override
		public NBTItem addTag(List<ItemTag> tags) {
			tags.forEach(tag -> {
				if (tag.getValue() instanceof Boolean)
					compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
				else if (tag.getValue() instanceof Double)
					compound.setDouble(tag.getPath(), (double) tag.getValue());
				else if (tag.getValue() instanceof String)
					compound.setString(tag.getPath(), (String) tag.getValue());
				else if (tag.getValue() instanceof Integer)
					compound.setInt(tag.getPath(), (int) tag.getValue());
			});
			return this;
		}

		@Override
		public NBTItem removeTag(String... paths) {
			for (String path : paths)
				compound.remove(path);
			return this;
		}

		@Override
		public Set<String> getTags() {
			return compound.c();
		}

		@Override
		public org.bukkit.inventory.ItemStack toItem() {
			nms.setTag(compound);
			return CraftItemStack.asBukkitCopy(nms);
		}
	}

	@Override
	public boolean isInBoundingBox(Entity entity, Location loc) {
		AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox().grow(.2, .2, .2);
		return box.a < loc.getX() && box.d > loc.getX() && box.b < loc.getY() && box.e > loc.getY() && box.c < loc.getZ() && box.f > loc.getZ();
	}

	@Override
	public double distanceSquaredFromBoundingBox(Entity entity, Location loc) {
		AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox().grow(.2, .2, .2);

		double dx = loc.getX() > box.a && loc.getX() < box.d ? 0 : Math.min(Math.abs(box.a - loc.getX()), Math.abs(box.d - loc.getX()));
		double dy = loc.getY() > box.b && loc.getY() < box.e ? 0 : Math.min(Math.abs(box.b - loc.getY()), Math.abs(box.e - loc.getY()));
		double dz = loc.getZ() > box.c && loc.getZ() < box.f ? 0 : Math.min(Math.abs(box.c - loc.getZ()), Math.abs(box.f - loc.getZ()));

		return dx * dx + dx * dy + dz * dz;
	}

	@Override
	public void playArmAnimation(Player player) {
		EntityPlayer p = ((CraftPlayer) player).getHandle();
		PlayerConnection connection = p.playerConnection;
	    PacketPlayOutAnimation armSwing = new PacketPlayOutAnimation(p, 0);
	    connection.sendPacket(armSwing);
	    connection.a(new PacketPlayInArmAnimation(EnumHand.MAIN_HAND));
	}

	@Override
	public Sound getBlockPlaceSound(org.bukkit.block.Block block) {
		try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

            Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound();

            Field breakSound = SoundEffectType.class.getDeclaredField("y");
            breakSound.setAccessible(true);
            SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

            Field keyField = SoundEffect.class.getDeclaredField("a");
            keyField.setAccessible(true);
            MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

            return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return null;
	}
}
