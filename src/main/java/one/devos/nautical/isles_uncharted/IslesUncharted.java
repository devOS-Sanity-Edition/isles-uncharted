package one.devos.nautical.isles_uncharted;

import static net.minecraft.commands.Commands.literal;

import java.util.List;

import net.fabricmc.api.ModInitializer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.isles_uncharted.content.IslesUnchartedEntities;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;
import one.devos.nautical.isles_uncharted.content.ship.VirtualStructure;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class IslesUncharted implements ModInitializer {
	public static final String ID = "isles_uncharted";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		IslesUnchartedEntities.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, context, env) -> {
			dispatcher.register(literal("isles").then(literal("shiptest").then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).executes(ctx -> {
				try {
					var source = ctx.getSource();
					var level = source.getLevel();
					var boundingBox = BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(ctx, "from"), BlockPosArgument.getLoadedBlockPos(ctx, "to"));
					var boundingBoxOrigin = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
					var structure = new VirtualStructure(new BlockPos(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan()));
					for (var pos : BlockPos.betweenClosed(BlockPos.ZERO, structure.bounds.offset(-1, -1, -1))) {
						structure.setBlock(pos, level.getBlockState(boundingBoxOrigin.offset(pos)));
					}
					var ship = new ShipEntity(structure, level);
					ship.setPos(source.getPosition());
					level.addFreshEntity(ship);
				} catch (Exception e) {
					ctx.getSource().sendFailure(Component.literal(e.getMessage()));
				}
				return 0;
			})))));

			dispatcher.register(literal("isles").then(literal("shipsizetest").executes(ctx -> {
				try {
					var source = ctx.getSource();
					var level = source.getLevel();
					var structure = new VirtualStructure(new BlockPos(32, 32, 32));
					for (var pos : BlockPos.betweenClosed(BlockPos.ZERO, structure.bounds.offset(-1, -1, -1))) {
						structure.setBlock(pos, Blocks.STONE.defaultBlockState());
					}
					var ship = new ShipEntity(structure, level);
					ship.setPos(source.getPosition());
					level.addFreshEntity(ship);
				} catch (Exception e) {
					ctx.getSource().sendFailure(Component.literal(e.toString()));
				}
				return 0;
			})));

			dispatcher.register(literal("isles").then(literal("test").executes(ctx -> {
				CommandSourceStack source = ctx.getSource();
				ServerPlayer player = source.getPlayerOrException();
				Fantasy fantasy = Fantasy.get(source.getServer());

				RegistryAccess registries = source.getServer().registryAccess();
				RegistryLookup<Biome> biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
				Reference<Biome> biome = biomeRegistry.getOrThrow(Biomes.PLAINS);
				BiomeSource biomes = new FixedBiomeSource(biome);

				NoiseGeneratorSettings noiseSettings = new NoiseGeneratorSettings(
						NoiseSettings.create(-10, 10, 1, 1),
						Blocks.STONE.defaultBlockState(),
						Blocks.WATER.defaultBlockState(),
						null,
						null,
						List.of(),
						0,
						false,
						false,
						false,
						false
				);

				ChunkGenerator generator = new NoiseBasedChunkGenerator(biomes, Holder.direct(noiseSettings));

				RuntimeWorldConfig config = new RuntimeWorldConfig()
						.setDimensionType(BuiltinDimensionTypes.OVERWORLD)
						.setGenerator(generator);
				RuntimeWorldHandle world = fantasy.openTemporaryWorld(config);
				ServerLevel level = world.asWorld();
				FabricDimensions.teleport(player, level, new PortalInfo(Vec3.ZERO, Vec3.ZERO, 0, 0));
				return 0;
			})));
		});
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
