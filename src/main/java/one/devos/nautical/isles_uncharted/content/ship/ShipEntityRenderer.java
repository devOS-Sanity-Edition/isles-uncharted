package one.devos.nautical.isles_uncharted.content.ship;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import one.devos.nautical.isles_uncharted.util.AABBUtils;

public class ShipEntityRenderer extends EntityRenderer<ShipEntity> {
	public static final Int2ReferenceOpenHashMap<VirtualStructureMesh> MESHES = new Int2ReferenceOpenHashMap<>();
	private static int cleanupTick = 0;

	private static VirtualStructureMesh getMesh(ShipEntity entity) {
		var mesh = MESHES.get(entity.getId());
		if (mesh == null) {
			mesh = new VirtualStructureMesh(entity.structure);
			MESHES.put(entity.getId(), mesh);
		}
		return mesh;
	}

	public static void tickMeshes(Level world) {
		if (++cleanupTick >= 20) {
			MESHES.keySet().removeIf(id -> world.getEntity(id) == null);
			cleanupTick = 0;
		}
	}

	private final EntityRenderDispatcher entityRenderDispatcher;

	public ShipEntityRenderer(Context ctx) {
		super(ctx);
		this.entityRenderDispatcher = ctx.getEntityRenderDispatcher();
		this.shadowRadius = 0;
	}

	@Override
	public void render(ShipEntity ship, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		matrices.pushPose();
		matrices.mulPose(ship.rotation());
		var bounds = ship.structure.bounds;
		matrices.translate(-bounds.getX() / 2d, -bounds.getY() / 2d, -bounds.getZ() / 2d);
		getMesh(ship).render(matrices);
		matrices.popPose();

		if (entityRenderDispatcher.shouldRenderHitBoxes() && !Minecraft.getInstance().showOnlyReducedInfo()) {
			matrices.pushPose();
			matrices.translate(-bounds.getX() / 2d, -bounds.getY() / 2d, -bounds.getZ() / 2d);
			for (Entity entity : ship.intersectingEntities) {
				var box = AABBUtils.localAABB(ship.rotation(), ship.getBoundingBox(), entity.getBoundingBox());;
				LevelRenderer.renderLineBox(matrices, vertexConsumers.getBuffer(RenderType.LINES), box, 1, 0, 1, 1);
			}
			matrices.mulPose(ship.rotation());
			LevelRenderer.renderLineBox(matrices, vertexConsumers.getBuffer(RenderType.LINES), ship.collider.neutral, 0, 0, 1, 1);
			matrices.popPose();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(ShipEntity entity) {
		return null;
	}
}
