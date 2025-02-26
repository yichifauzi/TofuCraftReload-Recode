package baguchan.tofucraft.client.render.entity;

import baguchan.tofucraft.TofuCraftReload;
import baguchan.tofucraft.client.TofuModelLayers;
import baguchan.tofucraft.client.model.TofuPufferModel;
import baguchan.tofucraft.entity.TofuPuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class TofuPufferRender<T extends TofuPuffer> extends MobRenderer<T, TofuPufferModel<T>> {
	private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(TofuCraftReload.MODID, "textures/entity/tofu_puffer/tofu_puffer.png");

	public TofuPufferRender(EntityRendererProvider.Context p_173956_) {
		super(p_173956_, new TofuPufferModel<>(p_173956_.bakeLayer(TofuModelLayers.TOFU_PUFFER)), 0.45F);
		//this.addLayer(new TofuPufferLayer<>(this));
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		//液体の見た目をつくる関数を呼び出す
		if (!entity.getFluidStack().isEmpty()) {
			renderFluid(poseStack, entity, buffer, packedLight);
		}
		//親モデルをスタックから取り出して、子モデルの編集をおわる
		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}


	private static void renderFluid(PoseStack poseStack, TofuPuffer puffer, MultiBufferSource bufferSource, int light) {
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
		renderOrb(poseStack, puffer, consumer, light);
	}

	private static void renderOrb(PoseStack poseStack, TofuPuffer puffer, VertexConsumer consumer, int light) {
		float orbSize = ((float) puffer.getFluidStack().getAmount() / 1000) * 0.3F;
		float rotationOffset = puffer.tickCount * 10 % 360;//entity.rotationOffset;
		float waveOffset = 0;//entity.waveOffset;

		//必要な座標を用意
		final float topY = orbSize * ((float) Math.sqrt(1.5)) / 2, sideX = (float) (orbSize * Math.sqrt(1f / 3f)), sideY = topY / 3;
		float offsetY = (orbSize * 0.5F + 0.05F) * puffer.getScale() * puffer.getAgeScale();//orbSize * 0.5f-0.0f;
		//場合分け
		//方向1 2 3
		for (int i = 0; i < 3; i++) {
			//上面
			Vector3f[] vertPos0 = {
					new Vector3f(0, topY + offsetY + cos(waveOffset + 25f, orbSize * 0.03f), 0),
					new Vector3f(cos(60 + i * 120 + rotationOffset, sideX), sideY + offsetY + cos(waveOffset, orbSize * 0.03f), sin(60 + i * 120 + rotationOffset, sideX)),
					new Vector3f(cos(i * 120 + rotationOffset, sideX), -sideY + offsetY + cos(waveOffset - 25f, orbSize * 0.03f), sin(i * 120 + rotationOffset, sideX)),
					new Vector3f(cos(-60 + i * 120 + rotationOffset, sideX), sideY + offsetY + cos(waveOffset, orbSize * 0.03f), sin(-60 + i * 120 + rotationOffset, sideX))
			};
			Vector2f[] vertUV0 = {
					new Vector2f(8 - (orbSize * 4), 8),
					new Vector2f(8, 8 + (orbSize * 4)),
					new Vector2f(8 + (orbSize * 4), 8),
					new Vector2f(8, 8 - (orbSize * 4))
			};
			//メッシュを定義する関数を呼び出す
			renderQuads(vertPos0, vertUV0, puffer, poseStack, consumer, light);

			//下面
			Vector3f[] vertPos1 = {
					new Vector3f(cos(60 + i * 120 + rotationOffset, sideX), sideY + offsetY + cos(waveOffset, orbSize * 0.03f), sin(60 + i * 120 + rotationOffset, sideX)),
					new Vector3f(cos(120 + i * 120 + rotationOffset, sideX), -sideY + offsetY + cos(waveOffset - 25f, orbSize * 0.03f), sin(120 + i * 120 + rotationOffset, sideX)),
					new Vector3f(0, -topY + offsetY + cos(waveOffset - 50f, orbSize * 0.03f), 0),
					new Vector3f(cos(i * 120 + rotationOffset, sideX), -sideY + offsetY + cos(waveOffset - 25f, orbSize * 0.03f), sin(i * 120 + rotationOffset, sideX))
			};
			Vector2f[] vertUV1 = {

					new Vector2f(8 - (orbSize * 4), 8),
					new Vector2f(8, 8 + (orbSize * 4)),
					new Vector2f(8 + (orbSize * 4), 8),
					new Vector2f(8, 8 - (orbSize * 4))
			};
			//メッシュを定義する関数を呼び出す
			renderQuads(vertPos1, vertUV1, puffer, poseStack, consumer, light);
		}
	}

	private static void renderQuads(Vector3f[] vertexPos, Vector2f[] vertexUV, TofuPuffer puffer, PoseStack poseStack, VertexConsumer consumer, int light) {
		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer buffer = consumer;


		IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(puffer.getFluidStack().getFluid());

		TextureAtlasSprite sprite = getFluidSprite(fluidTypeExtensions, puffer.getFluidStack());
		Vec3 offset = new Vec3(-0.5, -0.5, -0.5F);

		//カラーデータを変換
		//alpha *= (color >> 24 & 255) / 255f;
		float red = 1F;
		float green = 1F;
		float blue = 1F;

		float uMin = sprite.getU0();
		float uMax = sprite.getU1();
		float vMin = sprite.getV0();
		float vMax = sprite.getV1();
		float uDif = uMax - uMin;
		float vDif = vMax - vMin;

		//頂点を決める
		for (int i = 0; i < 4; i++) {
			buffer.addVertex(matrix, 0.5f + vertexPos[i].x + (float) offset.x, 0.5f + vertexPos[i].y + (float) offset.y, 0.5f + vertexPos[i].z + (float) offset.z)
					.setColor(red, green, blue, 1f)
					.setUv(sprite.getU(vertexUV[i].x / 8 * uDif), sprite.getV(vertexUV[i].y / 8 * vDif))
					.setLight(light).setNormal(0, 1, 0);

		}
	}


	//ブロックの光レベルの取得
	private static int calcLight(int combinedLight, FluidStack fluidStack) {
		int skyLight = combinedLight >> 20 & 15;
		int blockLight = combinedLight >> 4 & 15;
		//液体の明るさの取得
		int fluidLight = fluidStack.getFluid().getFluidType().getLightLevel();
		//計算
		int maxBlockLight = Math.max(blockLight, fluidLight);
		return (skyLight << 20 | maxBlockLight << 4);
	}

	//アニメーション用の波形をつくる
	private static float sin(float waveOffset, float amplitude) {
		return (amplitude * Mth.sin((float) Math.toRadians(waveOffset)));
	}

	private static float cos(float waveOffset, float amplitude) {
		return (amplitude * Mth.cos((float) Math.toRadians(waveOffset)));
	}

	private static TextureAtlasSprite getFluidSprite(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
		return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTypeExtensions.getFlowingTexture(fluidStack));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return LOCATION;
	}
}
