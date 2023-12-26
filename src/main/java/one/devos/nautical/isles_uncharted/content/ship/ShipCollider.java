package one.devos.nautical.isles_uncharted.content.ship;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.isles_uncharted.util.AABBUtils;

public class ShipCollider {
	private final ShipEntity ship;

	public final AABB neutral;
	public AABB rotated;

	ShipCollider(ShipEntity ship) {
		this.ship = ship;

		var bounds = ship.structure.bounds;
		this.neutral = AABB.ofSize(Vec3.ZERO, bounds.getX(), bounds.getY(), bounds.getZ());
		this.update();
	}

	public void collide(AABB box, Consumer<VoxelShape> shapeConsumer) {
		var localBox = AABBUtils.localAABB(ship.rotation(), ship.getBoundingBox(), box);
		var entityShape = Shapes.create(box);

		var bounds = ship.structure.bounds;
		for (BlockPos pos : BlockPos.betweenClosed(
			Mth.floor(localBox.minX),
			Mth.floor(localBox.minY),
			Mth.floor(localBox.minZ),
			Mth.floor(localBox.maxX),
			Mth.floor(localBox.maxY),
			Mth.floor(localBox.maxZ)
		)) {
			if ((pos.getX() < 0 || pos.getX() >= bounds.getX()) || (pos.getY() < 0 || pos.getY() >= bounds.getY()) || (pos.getZ() < 0 || pos.getZ() >= bounds.getZ()))
				continue;

			// AAAAAAAAAAA
			// var worldPos =
			// var block = ship.structure.getBlock(pos);
			// var collisionShape = block.getCollisionShape(ship.structure, pos);
			// var movedCollisionShape = collisionShape.move(worldPos.x, worldPos.y, worldPos.z);
			// if (collisionShape == Shapes.block()) {
			// 	if (box.intersects(worldPos.x, worldPos.y, worldPos.z, worldPos.x + 1, worldPos.y + 1, worldPos.z + 1))
			// 		shapeConsumer.accept(movedCollisionShape);
			// } else if (!collisionShape.isEmpty()) {
			// 	if (Shapes.joinIsNotEmpty(movedCollisionShape, entityShape, BooleanOp.AND))
			// 		shapeConsumer.accept(movedCollisionShape);
			// }
		}
	}

	public void update() {
		rotated = AABBUtils.rotateAABB(ship.rotation(), neutral);
	}
}
