package one.devos.nautical.isles_uncharted.content.ship;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import one.devos.nautical.isles_uncharted.util.EmptyBlockAndTintGetter;

public class VirtualStructure implements EmptyBlockAndTintGetter {
	private final BlockState[] blocks;

	private int xComponentShift;
	private int yComponentShift;
	private int zComponentShift;

	private final CrudeIncrementalIntIdentityHashBiMap<BlockState> blockPalette;

	public final BlockPos bounds;

	public VirtualStructure(BlockPos bounds) {
		if ((bounds.getX() <= 0 || bounds.getX() > 32) || (bounds.getY() <= 0 || bounds.getY() > 48) || (bounds.getZ() <= 0 || bounds.getZ() > 32))
			bounds = new BlockPos(1, 1, 1);

		int xBound = Mth.smallestEncompassingPowerOfTwo(bounds.getX());
		int yBound = Mth.smallestEncompassingPowerOfTwo(bounds.getY());
		int zBound = Mth.smallestEncompassingPowerOfTwo(bounds.getZ());
		this.blocks = new BlockState[xBound * yBound * zBound];
		Arrays.fill(this.blocks, Blocks.AIR.defaultBlockState());

		int xBits = Integer.bitCount(xBound - 1);
		int yBits = Integer.bitCount(yBound - 1);
		this.xComponentShift = xBits + yBits;
		this.yComponentShift = xBits;
		this.zComponentShift = 0;

		this.bounds = bounds;
		this.blockPalette = CrudeIncrementalIntIdentityHashBiMap.create(1 << 16);
	}

	private int encodePos(BlockPos pos) {
		return (pos.getX() << xComponentShift) | (pos.getY() << yComponentShift) | (pos.getZ() << zComponentShift);
	}

	public boolean setBlock(BlockPos pos, BlockState state) {
		var idx = encodePos(pos);
		final var oldState = blocks[idx];
		blocks[idx] = state;
		return oldState != state;
	}

	public BlockState getBlock(BlockPos pos) {
		return blocks[encodePos(pos)];
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if ((pos.getX() < 0 || pos.getX() >= bounds.getX()) || (pos.getY() < 0 || pos.getY() >= bounds.getY()) || (pos.getZ() < 0 || pos.getZ() >= bounds.getZ()))
			return Blocks.AIR.defaultBlockState();
		return getBlock(pos);
	}


	public CompoundTag saveToTag() {
		var tag = new CompoundTag();

		var blocksTag = new ListTag();
		for (int i = 0; i < blocks.length; i++) {
			var state = blocks[i];
			if (!state.is(Blocks.AIR)) {
				int id = blockPalette.getId(state);
				if (id == -1) id = blockPalette.add(state);

				var blockTag = new CompoundTag();
				blockTag.putInt("pos", i);
				blockTag.putInt("state", id);
				blocksTag.add(blockTag);
			}
		}
		tag.put("blocks", blocksTag);

		var paletteTag = new ListTag();
		for (int i = 0; i < blockPalette.size(); i++)
			paletteTag.add(NbtUtils.writeBlockState(blockPalette.byId(i)));
		tag.put("palette", paletteTag);

		return tag;
	}

	public void loadFromTag(CompoundTag tag, Level level) {
		tag.getList("palette", Tag.TAG_COMPOUND).forEach(v -> blockPalette.add(NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), (CompoundTag) v)));
		tag.getList("blocks", Tag.TAG_COMPOUND).forEach(v -> {
			var blockTag = (CompoundTag) v;
			blocks[blockTag.getInt("pos")] = blockPalette.byId(blockTag.getInt("state"));
		});
	}

	public void writeToBuf(FriendlyByteBuf buf) {
		int blockCount = 0;
		for (BlockState state : blocks) {
			if (!state.is(Blocks.AIR)) {
				if (blockPalette.getId(state) == -1) blockPalette.add(state);
				blockCount++;
			}
		}

		buf.writeVarInt(blockPalette.size());
		for (int i = 0; i < blockPalette.size(); i++) {
			var state = blockPalette.byId(i);
			var stateValues = state.getValues();

			buf.writeUtf(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
			buf.writeVarInt(stateValues.size());
			for(Map.Entry<Property<?>, Comparable<?>> entry : stateValues.entrySet()) {
				var property = entry.getKey();
				buf.writeUtf(property.getName());
				buf.writeUtf(getPropertyValueName(property, entry.getValue()));
			}
		}

		buf.writeVarInt(blockCount);
		for (int i = 0; i < blocks.length; i++) {
			var state = blocks[i];
			if (!state.is(Blocks.AIR)) {
				buf.writeVarInt(i);
				buf.writeVarInt(blockPalette.getId(state));
			}
		}
	}

	public void loadFromBuf(FriendlyByteBuf buf, Level level) {
		int paletteSize = buf.readVarInt();
		for (int i = 0; i < paletteSize; i++) {
			var blockId = new ResourceLocation(buf.readUtf());
			var blockOptional = level.holderLookup(Registries.BLOCK).get(ResourceKey.create(Registries.BLOCK, blockId));
			if (blockOptional.isEmpty()) {
				blockPalette.add(Blocks.AIR.defaultBlockState());
				continue;
			} else {
				var block = blockOptional.get().value();
				var state = block.defaultBlockState();
				var stateDefinition = block.getStateDefinition();

				int stateValueCount = buf.readVarInt();
				for (int j = 0; j < stateValueCount; j++) {
					Property<?> property = stateDefinition.getProperty(buf.readUtf());
					if (property != null)
						state = setPropertyValue(state, property, buf.readUtf());
				}

				blockPalette.add(state);
			}
		}

		int blockCount = buf.readVarInt();
		for (int i = 0; i < blockCount; i++) {
			blocks[buf.readVarInt()] = blockPalette.byId(buf.readVarInt());
		}
	}

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setPropertyValue(S state, Property<T> property, String valueName) {
		return property.getValue(valueName).map(value -> state.setValue(property, value)).orElse(state);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String getPropertyValueName(Property<T> property, Comparable<?> value) {
		return property.getName((T) value);
	}
}
