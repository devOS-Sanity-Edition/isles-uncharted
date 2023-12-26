package one.devos.nautical.isles_uncharted.util;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;

public interface EntityExtensions {
	@Nullable
	Set<ShipEntity> islesUncharted$intersectingShips(boolean create);
}
