package vazkii.quark.addons.oddities.client.render.be;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import vazkii.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import vazkii.quark.content.automation.client.render.QuarkPistonBlockEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class MagnetizedBlockRenderer implements BlockEntityRenderer<MagnetizedBlockBlockEntity> {

	private BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

	public MagnetizedBlockRenderer(BlockEntityRendererProvider.Context context) {

	}

	@Override
	public void render(MagnetizedBlockBlockEntity tileEntityIn, float partialTicks, @Nonnull PoseStack matrixStackIn, @Nonnull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Level world = tileEntityIn.getLevel();
		if (world != null) {
			BlockPos truepos = tileEntityIn.getBlockPos();
			BlockPos blockpos = truepos.relative(tileEntityIn.getFacing().getOpposite());
			BlockState blockstate = tileEntityIn.getMagnetState();
			if (!blockstate.isAir() && !(tileEntityIn.getProgress(partialTicks) >= 1.0F)) {
				BlockEntity subTile = tileEntityIn.getSubTile(tileEntityIn.getBlockPos());
				Vec3 offset = new Vec3(tileEntityIn.getOffsetX(partialTicks), tileEntityIn.getOffsetY(partialTicks), tileEntityIn.getOffsetZ(partialTicks));
				if(QuarkPistonBlockEntityRenderer.renderTESafely(world, truepos, blockstate, subTile, tileEntityIn, partialTicks, offset, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn))
					return;

				ModelBlockRenderer.enableCaching();
				matrixStackIn.pushPose();
				matrixStackIn.translate(offset.x, offset.y, offset.z);
				if (blockstate.getBlock() == Blocks.PISTON_HEAD && tileEntityIn.getProgress(partialTicks) <= 4.0F) {
					blockstate = blockstate.setValue(PistonHeadBlock.SHORT, Boolean.TRUE);
					renderStateModel(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
				} else {
					renderStateModel(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
				}

				matrixStackIn.popPose();
				ModelBlockRenderer.clearCache();
			}
		}
	}

	private void renderStateModel(BlockPos pos, BlockState state, PoseStack matrix, MultiBufferSource buffer, Level world, boolean checkSides, int packedOverlay) {
		RenderType.chunkBufferLayers().stream().filter(t -> ItemBlockRenderTypes.canRenderInLayer(state, t)).forEach(rendertype -> {
			ForgeHooksClient.setRenderType(rendertype);
			VertexConsumer ivertexbuilder = buffer.getBuffer(rendertype);
			if (blockRenderer == null)
				blockRenderer = Minecraft.getInstance().getBlockRenderer();

			blockRenderer.getModelRenderer().tesselateBlock(world, blockRenderer.getBlockModel(state), state, pos, matrix, ivertexbuilder, checkSides, RandomSource.create(), state.getSeed(pos), packedOverlay, EmptyModelData.INSTANCE);
		});
		ForgeHooksClient.setRenderType(null);
	}
}
