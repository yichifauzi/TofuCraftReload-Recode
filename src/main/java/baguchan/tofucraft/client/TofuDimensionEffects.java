package baguchan.tofucraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TofuDimensionEffects extends DimensionSpecialEffects {
	public TofuDimensionEffects() {
		super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 p_108908_, float p_108909_) {
		return p_108908_.multiply((double) (p_108909_ * 0.94F + 0.06F), (double) (p_108909_ * 0.94F + 0.06F), (double) (p_108909_ * 0.91F + 0.09F));
	}

	@Override
	public boolean isFoggyAt(int p_108905_, int p_108906_) {
		return false;
	}

	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
		PoseStack posestack = new PoseStack();
		return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
}
