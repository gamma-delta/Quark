/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 17, 2019, 20:06 AM (EST)]
 */
package vazkii.quark.world.client.render;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.quark.base.client.ClientReflectiveAccessor;
import vazkii.quark.world.base.ChainHandler;

@SideOnly(Side.CLIENT)
public class ChainRenderer {
	private static final TIntObjectMap<Entity> RENDER_MAP = new TIntObjectHashMap<>();

	public static void drawChainSegment(double x, double y, double z, BufferBuilder bufferbuilder, double offsetX, double offsetY, double offsetZ, double xOff, double zOff, float baseR, float baseG, float baseB, double height) {
		for (int seg = 0; seg <= 24; ++seg) {
			float r = baseR;
			float g = baseG;
			float b = baseB;

			if (seg % 2 == 0) {
				r *= 0.7F;
				g *= 0.7F;
				b *= 0.7F;
			}

			float amount = seg / 24.0F;
			bufferbuilder.pos(x + offsetX * amount + 0.0D, y + offsetY * (amount * amount + amount) * 0.5D + ((24.0F - seg) / 18.0F + 0.125F) * height + xOff, z + offsetZ * amount).color(r, g, b, 1.0F).endVertex();
			bufferbuilder.pos(x + offsetX * amount + 0.025D, y + offsetY * (amount * amount + amount) * 0.5D + ((24.0F - seg) / 18.0F + 0.125F) * height + zOff, z + offsetZ * amount + xOff).color(r, g, b, 1.0F).endVertex();
		}
	}

	public static void renderChain(Render render, double x, double y, double z, Entity entity, float partTicks) {
		if (!ClientReflectiveAccessor.renderOutlines(render)) {
			renderChain(entity, x, y, z, partTicks);
		}
	}

	private static double interp(double start, double end, double pct)
	{
		return start + (end - start) * pct;
	}

	private static void renderChain(Entity cart, double x, double y, double z, float partialTicks) {
		Entity entity = RENDER_MAP.get(cart.getEntityId());

		if (entity != null) {
			y += cart.height / 4;
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buf = tess.getBuffer();
			double yaw = interp(entity.prevRotationYaw, entity.rotationYaw, (partialTicks * 0.5F)) * Math.PI / 180;
			double pitch = interp(entity.prevRotationPitch, entity.rotationPitch, (partialTicks * 0.5F)) * Math.PI / 180;
			double rotX = Math.cos(yaw);
			double rotZ = Math.sin(yaw);
			double rotY = Math.sin(pitch);

			double height = entity instanceof EntityLivingBase ? entity.getEyeHeight() * 0.7 : 0;

			double pitchMod = Math.cos(pitch);
			double xLocus = interp(entity.prevPosX, entity.posX, partialTicks);
			double yLocus = interp(entity.prevPosY, entity.posY, partialTicks) + height;
			double zLocus = interp(entity.prevPosZ, entity.posZ, partialTicks);

			if (entity instanceof EntityLivingBase) {
				xLocus += -rotX * 0.7D - rotZ * 0.5D * pitchMod;
				yLocus += -rotY * 0.5D - 0.25D;
				zLocus += -rotZ * 0.7D + rotX * 0.5D * pitchMod;
			}

			double targetX = interp(cart.prevPosX, cart.posX, partialTicks);
			double targetY = interp(cart.prevPosY, cart.posY, partialTicks);
			double targetZ = interp(cart.prevPosZ, cart.posZ, partialTicks);
			if (entity instanceof EntityLivingBase) {
				xLocus -= rotX;
				zLocus -= rotZ;
			}
			double offsetX = ((float) (xLocus - targetX));
			double offsetY = ((float) (yLocus - targetY));
			double offsetZ = ((float) (zLocus - targetZ));
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

			drawChainSegment(x, y, z, buf, offsetX, offsetY, offsetZ, 0.025, 0, 0.3f, 0.3f, 0.3f, height);

			tess.draw();
			buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

			drawChainSegment(x, y, z, buf, offsetX, offsetY, offsetZ, 0, 0.025, 0.3f, 0.3f, 0.3f, height);

			tess.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
		}
	}

	public static void updateTick() {
		RENDER_MAP.clear();

		World world = Minecraft.getMinecraft().world;
		if (world == null)
			return;

		for (Entity entity : world.getEntities(Entity.class, ChainHandler::canBeLinked)) {
			Entity other = ChainHandler.getLinked(entity);
			if (other != null)
				RENDER_MAP.put(entity.getEntityId(), other);
		}
	}
}