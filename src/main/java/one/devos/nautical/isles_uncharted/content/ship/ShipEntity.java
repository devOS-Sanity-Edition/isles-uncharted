package one.devos.nautical.isles_uncharted.content.ship;

import org.joml.Quaternionf;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import one.devos.nautical.isles_uncharted.IslesUncharted;
import one.devos.nautical.isles_uncharted.content.IslesUnchartedEntities;
import one.devos.nautical.isles_uncharted.util.EntityExtensions;

public class ShipEntity extends Entity {
	public static final ResourceLocation SPAWN_PACKET = IslesUncharted.id("ship_spawn");

	public final ReferenceArraySet<Entity> intersectingEntities = new ReferenceArraySet<>();
	public VirtualStructure structure;
	public ShipCollider collider;

	private final Quaternionf rotation = new Quaternionf().rotateY(45 * Mth.DEG_TO_RAD);

	public ShipEntity(VirtualStructure structure, Level world) {
		super(IslesUnchartedEntities.SHIP, world);
		this.structure = structure;
		if (structure != null)
			this.collider = new ShipCollider(this);
	}

	public ShipEntity(EntityType<?> variant, Level world) {
		this((VirtualStructure) null, world);
	}

	@Override
	protected void defineSynchedData() {

	}

	@Override
	public void setPos(double x, double y, double z) {
		setPosRaw(x, y, z);
		if (structure != null) setBoundingBox(makeBoundingBox());
	}

	public Quaternionf rotation() {
		return this.rotation;
	}

	@Override
	public void tick() {
		rotation.rotateY(.1f * Mth.DEG_TO_RAD);
		collider.update();
		setBoundingBox(makeBoundingBox());

		var intersectingEntities = level().getEntities(this, getBoundingBox().inflate(2), EntitySelector.NO_SPECTATORS.and(e -> !(e instanceof ShipEntity)));
		for (Entity intersectingEntity : intersectingEntities) {
			var intersectingShips = ((EntityExtensions) intersectingEntity).islesUncharted$intersectingShips(true);
			if (!intersectingShips.contains(this)) {
				this.intersectingEntities.add(intersectingEntity);
				intersectingShips.add(this);
			}
		}

		this.intersectingEntities.removeIf(e -> {
			if (!intersectingEntities.contains(e)) {
				((EntityExtensions) e).islesUncharted$intersectingShips(true).remove(this);
				return true;
			}
			return false;
		});
	}

	@Override
	protected AABB makeBoundingBox() {
		return collider.rotated.move(position());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		var structureTag = nbt.getCompound("structure");
		structure = new VirtualStructure(NbtUtils.readBlockPos(structureTag.getCompound("bounds")));
		structure.loadFromTag(structureTag, level());
		collider = new ShipCollider(this);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		var structureTag = structure.saveToTag();
		structureTag.put("bounds", NbtUtils.writeBlockPos(structure.bounds));
		nbt.put("structure", structureTag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		var buf = PacketByteBufs.create();
		buf.writeBlockPos(structure.bounds);
		buf.writeVarInt(getId());
		buf.writeUUID(uuid);
		structure.writeToBuf(buf);
		buf.writeVec3(position());
		return (Packet<ClientGamePacketListener>) (Object) ServerPlayNetworking.createS2CPacket(SPAWN_PACKET, buf);
	}
}
