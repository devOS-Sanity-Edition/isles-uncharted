package one.devos.nautical.isles_uncharted;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import one.devos.nautical.isles_uncharted.content.IslesUnchartedEntities;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntity;
import one.devos.nautical.isles_uncharted.content.ship.ShipEntityRenderer;
import one.devos.nautical.isles_uncharted.content.ship.VirtualStructure;
import one.devos.nautical.isles_uncharted.content.ship.VirtualStructureMesh;

public class IslesUnchartedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(IslesUnchartedEntities.SHIP, ShipEntityRenderer::new);
		ClientTickEvents.START_WORLD_TICK.register(ShipEntityRenderer::tickMeshes);
		InvalidateRenderStateCallback.EVENT.register(() -> {
			ShipEntityRenderer.MESHES.values().forEach(VirtualStructureMesh::close);
			ShipEntityRenderer.MESHES.clear();
		});

		ClientPlayNetworking.registerGlobalReceiver(ShipEntity.SPAWN_PACKET, (client, listener, buf, sender) -> {
			buf.retain();
			client.execute(() -> {
				try {
					var ship = new ShipEntity(new VirtualStructure(buf.readBlockPos()), client.level);
					ship.setId(buf.readVarInt());
					ship.setUUID(buf.readUUID());

					ship.structure.loadFromBuf(buf, client.level);

					var pos = buf.readVec3();
					ship.syncPacketPositionCodec(pos.x, pos.y, pos.z);
					ship.moveTo(pos.x, pos.y, pos.z);

					client.level.addEntity(ship);
				} finally {
					buf.release();
				}
			});
		});
	}
}
