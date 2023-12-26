package one.devos.nautical.isles_uncharted.util;

import org.joml.Matrix4d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import net.minecraft.world.phys.AABB;

public class AABBUtils {
	private static final Vector3d CENTER = new Vector3d();
	private static final Vector3d EXTENTS = new Vector3d();
	private static final Matrix4d MAT = new Matrix4d();

	public static AABB localAABB(Quaternionf rot, AABB origin, AABB worldAABB) {
		CENTER.set(worldAABB.maxX + worldAABB.minX, worldAABB.maxY + worldAABB.minY, worldAABB.maxZ + worldAABB.minZ).mul(.5);
		EXTENTS.set(worldAABB.maxX, worldAABB.maxY, worldAABB.maxZ).sub(CENTER);
		rot.transform(CENTER.sub(origin.minX, origin.minY, origin.minZ));
		return new AABB(
			CENTER.x - EXTENTS.x,
			CENTER.y - EXTENTS.y,
			CENTER.z - EXTENTS.z,
			CENTER.x + EXTENTS.x,
			CENTER.y + EXTENTS.y,
			CENTER.z + EXTENTS.z
		);
	}

	public static AABB rotateAABB(Quaternionf rotation, AABB neutral) {
		MAT.identity();
		MAT.rotate(rotation);

		CENTER.set(neutral.maxX + neutral.minX, neutral.maxY + neutral.minY, neutral.maxZ + neutral.minZ);

		MAT.transformPosition(CENTER);
		double maxX = Math.abs(MAT.m00()) * neutral.maxX + (Math.abs(MAT.m10()) * neutral.maxY + (Math.abs(MAT.m20()) * neutral.maxZ));
		double maxY = Math.abs(MAT.m01()) * neutral.maxX + (Math.abs(MAT.m11()) * neutral.maxY + (Math.abs(MAT.m21()) * neutral.maxZ));
		double maxZ = Math.abs(MAT.m02()) * neutral.maxX + (Math.abs(MAT.m12()) * neutral.maxY + (Math.abs(MAT.m22()) * neutral.maxZ));

		return new AABB(
			CENTER.x - maxX,
			CENTER.y - maxY,
			CENTER.z - maxZ,
			CENTER.x + maxX,
			CENTER.y + maxY,
			CENTER.z + maxZ
		);
	}
}
