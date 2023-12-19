package one.devos.nautical.isles_uncharted;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import one.devos.nautical.isles_uncharted.data.IslesDensityFunctions;
import one.devos.nautical.isles_uncharted.util.DynamicRegistryBootstraps;

public class IslesDatagen implements DataGeneratorEntrypoint {
	public static final DynamicRegistryBootstraps BOOTSTRAPS = DynamicRegistryBootstraps.builder()
			.put(Registries.DENSITY_FUNCTION, IslesDensityFunctions::boostrap)
			.build();

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator gen) {
		FabricDataGenerator.Pack pack = gen.createPack();
		pack.addProvider(BOOTSTRAPS::getDataProvider);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		BOOTSTRAPS.forEach(registryBuilder::add);
	}
}
