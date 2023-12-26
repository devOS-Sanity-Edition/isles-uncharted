package one.devos.nautical.isles_uncharted.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import one.devos.nautical.isles_uncharted.IslesUncharted;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;

public final class IslesUnchartedEntities {
	public static final EntityType<ShipEntity> SHIP = register("ship", FabricEntityTypeBuilder.<ShipEntity>create(MobCategory.MISC, ShipEntity::new).disableSummon());

	private static <T extends Entity> EntityType<T> register(String id, FabricEntityTypeBuilder<T> type) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, IslesUncharted.id(id), type.build());
	}

	public static void init() { }
}
