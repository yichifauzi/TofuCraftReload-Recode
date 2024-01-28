package baguchan.tofucraft.registry;

import baguchan.tofucraft.TofuCraftReload;
import baguchan.tofucraft.inventory.SaltFurnaceMenu;
import baguchan.tofucraft.inventory.TFCrafterMenu;
import baguchan.tofucraft.inventory.TFOvenMenu;
import baguchan.tofucraft.inventory.TFStorageMenu;
import baguchan.tofucraft.inventory.TofuWorkStationMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TofuMenus {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, TofuCraftReload.MODID);


	public static final Supplier<MenuType<SaltFurnaceMenu>> SALT_FURNACE = MENU_TYPES.register("salt_furnace", () -> new MenuType<>(SaltFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));

	public static final Supplier<MenuType<TFStorageMenu>> TF_STORAGE = MENU_TYPES.register("tf_storage", () -> new MenuType<>(TFStorageMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final Supplier<MenuType<TofuWorkStationMenu>> TOFU_WORK_STATION = MENU_TYPES.register("tofu_work_station", () -> new MenuType<>(TofuWorkStationMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final Supplier<MenuType<TFCrafterMenu>> TF_CRAFTER = MENU_TYPES.register("tf_crafter", () -> new MenuType<>(TFCrafterMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final Supplier<MenuType<TFOvenMenu>> TF_OVEN = MENU_TYPES.register("tf_oven", () -> new MenuType<>(TFOvenMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
