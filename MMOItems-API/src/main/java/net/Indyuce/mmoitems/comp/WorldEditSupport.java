package net.Indyuce.mmoitems.comp;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.util.MushroomState;

public class WorldEditSupport {
	public WorldEditSupport() {
		WorldEdit.getInstance().getBlockFactory().register(new WECustomBlockInputParser());
	}

	public class WECustomBlockInputParser extends InputParser<BaseBlock> {
		public WECustomBlockInputParser() {
			super(WorldEdit.getInstance());
		}

		@Override
		@SuppressWarnings("unchecked")
		public BaseBlock parseFromInput(String input, ParserContext context) {
			input = input.toLowerCase();
			BlockType type;
			if (!input.startsWith("mmoitems-"))
				return null;

			int id;
			try {
				id = Integer.parseInt(input.split("-")[1]);
			} catch (NumberFormatException e) {
				return null;
			}

			CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(id);
			if (block == null)
				return null;
			MushroomState mush = block.getState();

			switch (mush.getType()) {
			case MUSHROOM_STEM:
				type = BlockTypes.MUSHROOM_STEM;
				break;
			case BROWN_MUSHROOM_BLOCK:
				type = BlockTypes.BROWN_MUSHROOM_BLOCK;
				break;
			case RED_MUSHROOM_BLOCK:
				type = BlockTypes.RED_MUSHROOM_BLOCK;
				break;
			default:
				return null;
			// throw new
			// NoMatchException(TranslatableComponent.of("worldedit.error.unknown-block",
			// TextComponent.of(input)));
			}

			BlockState state = type.getDefaultState().with((Property<Boolean>) type.getPropertyMap().get("up"), mush.getSide("up"))
					.with((Property<Boolean>) type.getPropertyMap().get("down"), mush.getSide("down"))
					.with((Property<Boolean>) type.getPropertyMap().get("north"), mush.getSide("north"))
					.with((Property<Boolean>) type.getPropertyMap().get("south"), mush.getSide("south"))
					.with((Property<Boolean>) type.getPropertyMap().get("east"), mush.getSide("east"))
					.with((Property<Boolean>) type.getPropertyMap().get("west"), mush.getSide("west"));
			return state.toBaseBlock();
		}
	}

}
