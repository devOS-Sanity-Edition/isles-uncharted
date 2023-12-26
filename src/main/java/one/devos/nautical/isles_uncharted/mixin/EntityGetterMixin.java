package one.devos.nautical.isles_uncharted.mixin;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;
import one.devos.nautical.isles_uncharted.util.EntityExtensions;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@Overwrite
	// TODO: a inject and wrap with condition instead of overwrite
	default List<VoxelShape> getEntityCollisions(Entity entity, AABB box) {
		if (box.getSize() < 1.0E-7) {
			return List.of();
		} else {
			Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
			List<Entity> list = ((EntityGetter) this).getEntities(entity, box.inflate(1.0E-7), predicate);
			if (entity != null) {
				var intersectingShips = ((EntityExtensions) entity).islesUncharted$intersectingShips(false);
				if (intersectingShips != null) list.addAll(intersectingShips);
			}
			if (list.isEmpty()) {
				return List.of();
			} else {
				ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());

				for(Entity entity2 : list) {
					if (entity2 instanceof ShipEntity ship) {
						ship.collider.collide(box, builder::add);
					} else {
						builder.add(Shapes.create(entity2.getBoundingBox()));
					}
				}

				return builder.build();
			}
		}
	}
}
