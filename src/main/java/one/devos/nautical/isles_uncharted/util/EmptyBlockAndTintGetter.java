package one.devos.nautical.isles_uncharted.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface EmptyBlockAndTintGetter extends BlockAndTintGetter {
	static final Minecraft MC = Minecraft.getInstance();
	static final LayerLightEventListener FULL_BRIGHT_LAYER = new LayerLightEventListener() {
		@Override
		public void checkBlock(BlockPos pos) {

		}

		@Override
		public boolean hasLightWork() {
			return false;
		}

		@Override
		public int runLightUpdates() {
			return 0;
		}

		@Override
		public void updateSectionStatus(SectionPos pos, boolean notReady) {

		}

		@Override
		public void setLightEnabled(ChunkPos pos, boolean enabled) {

		}

		@Override
		public void propagateLightSources(ChunkPos pos) {

		}

		@Override
		public DataLayer getDataLayerData(SectionPos pos) {
			return null;
		}

		@Override
		public int getLightValue(BlockPos pos) {
			return 15;
		}
	};
	static final LevelLightEngine FULL_BRIGHT_ENGINE = new LevelLightEngine(new LightChunkGetter() {
		@Override
		public LightChunk getChunkForLighting(int chunkX, int chunkZ) {
			return null;
		}

		@Override
		public BlockGetter getLevel() {
			return MC.level;
		}
	}, false, false) {
		@Override
		public LayerLightEventListener getLayerListener(LightLayer lightType) {
			return lightType == LightLayer.SKY ? FULL_BRIGHT_LAYER : LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
		}
	};
	static final Supplier<Biome> BIOME = Suppliers.memoize(
		() -> MC.level.registryAccess().registryOrThrow(Registries.BIOME).get(Biomes.PLAINS)
	);

	@Override
	default BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	default BlockState getBlockState(BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	default FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	default int getHeight() {
		return 15;
	}

	@Override
	default int getMinBuildHeight() {
		return 0;
	}

	@Override
	default float getShade(Direction direction, boolean shaded) {
		return MC.level.getShade(direction, shaded);
	}

	@Override
	default LevelLightEngine getLightEngine() {
		return FULL_BRIGHT_ENGINE;
	}

	@Override
	default int getBlockTint(BlockPos pos, ColorResolver biomeColorProvider) {
		return biomeColorProvider.getColor(BIOME.get(), pos.getX(), pos.getZ());
	}
}
