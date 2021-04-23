package baguchan.tofucraft.message;

import baguchan.tofucraft.TofuCraftReload;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SoyMilkDrinkedMessage {
	private final int entityId;

	private final int level;

	public SoyMilkDrinkedMessage(LivingEntity entity, int level) {
		this.entityId = entity.getId();
		this.level = level;
	}

	public SoyMilkDrinkedMessage(int id, int level) {
		this.entityId = id;
		this.level = level;
	}

	public void serialize(PacketBuffer buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(this.level);
	}

	public static SoyMilkDrinkedMessage deserialize(PacketBuffer buffer) {
		int entityId = buffer.readInt();
		int level = buffer.readInt();
		return new SoyMilkDrinkedMessage(entityId, level);
	}

	public static boolean handle(SoyMilkDrinkedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
			context.enqueueWork(() -> {
				Entity entity = (Minecraft.getInstance()).player.level.getEntity(message.entityId);
				if (entity != null && entity instanceof LivingEntity)
					entity.getCapability(TofuCraftReload.SOY_HEALTH_CAPABILITY, null).ifPresent((cap) -> {
						cap.setSoyHealth((LivingEntity) entity, message.level);
					});
			});
		return true;
	}
}
