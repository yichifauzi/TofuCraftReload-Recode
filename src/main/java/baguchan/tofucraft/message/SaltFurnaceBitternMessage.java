package baguchan.tofucraft.message;

import baguchan.tofucraft.tileentity.SaltFurnaceTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SaltFurnaceBitternMessage {
	public BlockPos blockPos;

	public FluidStack fluid;

	public SaltFurnaceBitternMessage(BlockPos blockPos, FluidStack fluid) {
		this.blockPos = blockPos;
		this.fluid = fluid;
	}

	public void writePacketData(PacketBuffer buffer) {
		buffer.writeBlockPos(this.blockPos);
		CompoundNBT fluidTag = new CompoundNBT();
		if (this.fluid != null)
			this.fluid.writeToNBT(fluidTag);
		buffer.writeNbt(fluidTag);
	}

	public static SaltFurnaceBitternMessage readPacketData(PacketBuffer buffer) {
		BlockPos blockPos = buffer.readBlockPos();
		FluidStack fluid = FluidStack.loadFluidStackFromNBT(buffer.readNbt());
		return new SaltFurnaceBitternMessage(blockPos, fluid);
	}

	public static boolean handle(SaltFurnaceBitternMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
			context.enqueueWork(() -> {
				TileEntity tileentity = (Minecraft.getInstance()).player.level.getBlockEntity(message.blockPos);
				if (tileentity instanceof SaltFurnaceTileEntity) {
					SaltFurnaceTileEntity tileentity1 = (SaltFurnaceTileEntity) tileentity;
					tileentity1.bitternTank.setFluid(message.fluid);
				}
			});
		return true;
	}
}
