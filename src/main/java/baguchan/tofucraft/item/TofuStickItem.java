package baguchan.tofucraft.item;

import baguchan.tofucraft.registry.TofuBlocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;

public class TofuStickItem extends Item {
	public TofuStickItem(Properties group) {
		super(group);
	}

	public ActionResultType useOn(ItemUseContext context) {
		if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() == TofuBlocks.GRILLEDTOFU &&
				TofuBlocks.TOFU_PORTAL.trySpawnPortal(context.getLevel(), context.getClickedPos().above())) {
			if (!context.getPlayer().isCreative())
				context.getItemInHand().hurtAndBreak(1, (LivingEntity) context.getPlayer(), p_213625_1_ -> p_213625_1_.broadcastBreakEvent(context.getHand()));
			return ActionResultType.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public Rarity getRarity(ItemStack p_77613_1_) {
		return Rarity.RARE;
	}

	@Override
	public boolean isFoil(ItemStack p_77636_1_) {
		return true;
	}
}
