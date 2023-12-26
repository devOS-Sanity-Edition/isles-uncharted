package one.devos.nautical.isles_uncharted.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;
import one.devos.nautical.isles_uncharted.util.EntityExtensions;

@Mixin(Entity.class)
public class EntityMixin implements EntityExtensions {
	private Set<ShipEntity> islesUncharted$intersectingShips;

	@Override
	public Set<ShipEntity> islesUncharted$intersectingShips(boolean create) {
		if (islesUncharted$intersectingShips == null && create)
			islesUncharted$intersectingShips = new ReferenceArraySet<>();
		return islesUncharted$intersectingShips;
	}
}
