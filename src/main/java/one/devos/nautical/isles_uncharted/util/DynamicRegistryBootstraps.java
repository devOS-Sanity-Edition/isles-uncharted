package one.devos.nautical.isles_uncharted.util;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DynamicRegistryBootstraps {
	private final List<Entry<?>> entries;

	private DynamicRegistryBootstraps(List<Entry<?>> entries) {
		this.entries = entries;
	}

	@SuppressWarnings("unchecked")
	public <T> void forEach(Consumer<Entry<T>> consumer) {
		for (Entry<?> entry : entries) {
			consumer.accept((Entry<T>) entry);
		}
	}

	public <T> void forEach(BiConsumer<ResourceKey<Registry<T>>, RegistrySetBuilder.RegistryBootstrap<T>> consumer) {
		forEach((Entry<T> entry) -> consumer.accept(entry.registry, entry.bootstrap));
	}

	public DataProvider getDataProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		return new Provider(output, registries);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Entry<?>> entries = new ArrayList<>();

		public <T> Builder put(ResourceKey<Registry<T>> registry, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
			entries.add(new Entry<>(registry, bootstrap));
			return this;
		}

		public DynamicRegistryBootstraps build() {
			return new DynamicRegistryBootstraps(entries);
		}
	}

	public record Entry<T>(ResourceKey<Registry<T>> registry, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
	}

	public class Provider extends FabricDynamicRegistryProvider {
		private final String modId;

		public Provider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
			this.modId = output.getModId();
		}

		@Override
		protected void configure(HolderLookup.Provider registries, Entries entries) {
			forEach((key, boostrap) -> entries.addAll(registries.lookupOrThrow(key)));
		}

		@Override
		@NotNull
		public String getName() {
			return "Dynamic Registry bootstraps for " + modId;
		}
	}
}
