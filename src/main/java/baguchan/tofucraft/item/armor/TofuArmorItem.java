package baguchan.tofucraft.item.armor;

import baguchan.tofucraft.api.tfenergy.IEnergyInsertable;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class TofuArmorItem extends ArmorItem implements IEnergyInsertable {
	public static final String TAG_TF = "tf_energy";
	public static final String TAG_TFMAX = "tf_energymax";

	public TofuArmorItem(Holder<ArmorMaterial> tofuArmorMaterial, Type type, Properties properties) {
		super(tofuArmorMaterial, type, properties);
	}

	@Override
	public int fill(ItemStack inst, int energy, boolean simulate) {
		int calculated = Math.min(energy, inst.getDamageValue());
		if (!simulate) {
			if (inst.getDamageValue() > 0) {
				inst.setDamageValue(Mth.clamp(inst.getDamageValue() - calculated, 0, inst.getMaxDamage()));
				return calculated * 5;
			}
		}
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack p_41456_) {
		return super.isEnchantable(p_41456_);
	}
}
