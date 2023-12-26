package one.devos.nautical.isles_uncharted.content.ship;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;

public class VirtualStructureMesh {
	private static final SectionBufferBuilderPack BUILDERS = new SectionBufferBuilderPack();
	private final Reference2ReferenceOpenHashMap<RenderType, VertexBuffer> BUFFERS = new Reference2ReferenceOpenHashMap<>();

	private final VirtualStructure structure;

	public boolean shouldBuild = true;

	VirtualStructureMesh(VirtualStructure structure) {
		this.structure = structure;
	}

	public void build() {
		BUFFERS.values().forEach(buffer -> buffer.close());
		BUFFERS.clear();

		ModelBlockRenderer.enableCaching();

		var renderedTypes = new ReferenceArraySet<RenderType>(RenderType.chunkBufferLayers().size());
		var randomSource = RandomSource.create();
		var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		var bakeStack = new PoseStack();
		for (var pos : BlockPos.betweenClosed(BlockPos.ZERO, structure.bounds.offset(-1, -1, -1))) {
			var state = structure.getBlock(pos);
			if (state.getRenderShape() != RenderShape.INVISIBLE) {
				var renderType = ItemBlockRenderTypes.getChunkRenderType(state);
				var builder = BUILDERS.builder(renderType);
				if (renderedTypes.add(renderType))
					builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

				bakeStack.pushPose();
				bakeStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderDispatcher.renderBatched(state, pos, structure, bakeStack, builder, true, randomSource);
				bakeStack.popPose();
			}
		}

		ModelBlockRenderer.clearCache();

		for (var renderType : renderedTypes) {
			var builder = BUILDERS.builder(renderType).endOrDiscardIfEmpty();
			if (builder != null) {
				var buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
				buffer.bind();
				buffer.upload(builder);
				VertexBuffer.unbind();
				BUFFERS.put(renderType, buffer);
			}
		}

		shouldBuild = false;
	}

	public void render(PoseStack matrices) {
		if (shouldBuild) build();
		var modelViewMat = matrices.last().pose();
		for (Reference2ReferenceMap.Entry<RenderType, VertexBuffer> entry : BUFFERS.reference2ReferenceEntrySet()) {
			var layer = entry.getKey();
			var buf = entry.getValue();
			buf.bind();
			layer.setupRenderState();
			buf.drawWithShader(modelViewMat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
			layer.clearRenderState();
			VertexBuffer.unbind();
		}
	}

	public void close() {
		BUFFERS.values().forEach(buffer -> buffer.close());
		BUFFERS.clear();
	}
}
