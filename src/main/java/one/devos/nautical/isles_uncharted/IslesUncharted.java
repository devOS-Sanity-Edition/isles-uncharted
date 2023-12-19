package one.devos.nautical.isles_uncharted;

import static net.minecraft.commands.Commands.literal;

import java.util.List;

import net.fabricmc.api.ModInitializer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

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
		CommandRegistrationCallback.EVENT.register((dispatcher, context, env) -> {
			dispatcher.register(literal("isles").then(literal("test").executes(ctx -> {
				CommandSourceStack source = ctx.getSource();
				ServerPlayer player = source.getPlayerOrException();
				Fantasy fantasy = Fantasy.get(source.getServer());

				RegistryAccess registries = source.getServer().registryAccess();
				RegistryLookup<Biome> biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
				Reference<Biome> biome = biomeRegistry.getOrThrow(Biomes.PLAINS);
				BiomeSource biomes = new FixedBiomeSource(biome);

				NoiseGeneratorSettings noiseSettings = new NoiseGeneratorSettings(
						NoiseSettings.create(0, 128, 1, 1),
						Blocks.STONE.defaultBlockState(),
						Blocks.WATER.defaultBlockState(),
						makeSomeNoise(registries),
						SurfaceRules.state(null),
						List.of(),
						24,
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

	private static NoiseRouter makeSomeNoise(RegistryAccess registries) {
		DensityFunction zero = DensityFunctions.zero();
		DensityFunction finalDensity = new DensityFunction.SimpleFunction() {
			@Override
			public double compute(FunctionContext context) {
				double radius = 30;
				double dist = Math.sqrt(Math.pow(context.blockX(), 2) + Math.pow(context.blockZ(), 2));
				return -(dist / radius) + 1;
			}

			@Override
			public double minValue() {
				return -1_000_000;
			}

			@Override
			public double maxValue() {
				return 1;
			}

			@Override
			public KeyDispatchDataCodec<? extends DensityFunction> codec() {
				throw new UnsupportedOperationException("please don't call this");
			}
		};
		return new NoiseRouter(
				zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, finalDensity, zero, zero, zero
		);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
