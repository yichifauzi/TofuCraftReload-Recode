package baguchan.tofucraft.entity;

import baguchan.tofucraft.entity.goal.CropHarvestGoal;
import baguchan.tofucraft.entity.goal.DoSleepingGoal;
import baguchan.tofucraft.entity.goal.EatItemGoal;
import baguchan.tofucraft.entity.goal.FindJobBlockGoal;
import baguchan.tofucraft.entity.goal.MakeFoodGoal;
import baguchan.tofucraft.entity.goal.MoveToJobGoal;
import baguchan.tofucraft.entity.goal.OpenTofuDoorGoal;
import baguchan.tofucraft.entity.goal.RestockGoal;
import baguchan.tofucraft.entity.goal.ShareItemAndGossipGoal;
import baguchan.tofucraft.entity.goal.TofunianLoveGoal;
import baguchan.tofucraft.entity.goal.TofunianSleepOnBedGoal;
import baguchan.tofucraft.entity.goal.WakeUpGoal;
import baguchan.tofucraft.registry.TofuAdvancements;
import baguchan.tofucraft.registry.TofuBiomes;
import baguchan.tofucraft.registry.TofuEntityTypes;
import baguchan.tofucraft.registry.TofuItems;
import baguchan.tofucraft.registry.TofuSounds;
import baguchan.tofucraft.registry.TofuTrades;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Tofunian extends AbstractTofunian implements ReputationEventHandler {

	private static final EntityDataAccessor<String> ACTION = SynchedEntityData.defineId(Tofunian.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> ROLE = SynchedEntityData.defineId(Tofunian.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> TOFUNIAN_TYPE = SynchedEntityData.defineId(Tofunian.class, EntityDataSerializers.STRING);

	public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(TofuItems.SOYMILK.get(), 3, TofuItems.TOFUCOOKIE.get(), 3, TofuItems.TOFUGRILLED.get(), 1);

	private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(TofuItems.SOYMILK.get(), TofuItems.TOFUCOOKIE.get(), TofuItems.TOFUGRILLED.get(), TofuItems.SEEDS_SOYBEANS.get());
	private static final Predicate<? super ItemEntity> ALLOWED_ITEMS = (p_213616_0_) -> {
		return WANTED_ITEMS.contains(p_213616_0_.getItem().getItem());
	};


	private byte foodLevel;
	private final GossipContainer gossips = new GossipContainer();

	@Nullable
	private BlockPos tofunainHome;

	@Nullable
	private BlockPos tofunainJobBlock;

	private long lastGossipTime;
	private long lastGossipDecay;
	private long lastRestock;
	private int restocksToday;
	private long lastRestockDayTime;

	private int timeUntilReset;
	private boolean leveledUp;

	@Nullable
	private Player previousCustomer;

	private int xp;

	private int tofunianLevel = 1;

	public final AnimationState agreeAnimationState = new AnimationState();

	public Tofunian(EntityType<? extends Tofunian> type, Level worldIn) {
		super(type, worldIn);
		((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
		setCanPickUpLoot(true);
	}

	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new WakeUpGoal(this));
		this.goalSelector.addGoal(0, new DoSleepingGoal(this));
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
		this.goalSelector.addGoal(1, new OpenTofuDoorGoal(this, true));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 1.2D, 1.25D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ShuDofuSpider.class, 10.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, TofuGandlem.class, 10.0F, 1.2D, 1.3D));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.3D));
		this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
		this.goalSelector.addGoal(2, new TofunianSleepOnBedGoal(this, 0.85F, 6));
		this.goalSelector.addGoal(3, new EatItemGoal<>(this, null, (p_35882_) -> {
			return getHealth() < getMaxHealth();
		}));
		this.goalSelector.addGoal(4, new TofunianLoveGoal(this, 0.8F));
		this.goalSelector.addGoal(5, new GetItemGoal<>(this));
		this.goalSelector.addGoal(6, new CropHarvestGoal(this, 0.9F));
		this.goalSelector.addGoal(7, new MakeFoodGoal(this, 0.9F, 1));
		this.goalSelector.addGoal(8, new RestockGoal(this, 0.9F, 1));
		this.goalSelector.addGoal(9, new MoveToJobGoal(this, 0.9F, 1));
		this.goalSelector.addGoal(10, new MoveToGoal(this, 42.0D, 1.0D));
		this.goalSelector.addGoal(11, new FindJobBlockGoal(this, 0.85F, 6));

		this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.9D));
		this.goalSelector.addGoal(13, new InteractGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(14, new ShareItemAndGossipGoal(this, 0.9F));
		this.goalSelector.addGoal(15, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.goalSelector.addGoal(16, new RandomLookAroundGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.24D).add(Attributes.MAX_HEALTH, 20.0D);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
		return TofuEntityTypes.TOFUNIAN.get().create(p_241840_1_);
	}

	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ROLE, Roles.TOFUNIAN.name());
		this.entityData.define(ACTION, Actions.NORMAL.name());
		this.entityData.define(TOFUNIAN_TYPE, TofunianType.NORMAL.name());
	}

	public Actions getAction() {
		return Actions.get(this.entityData.get(ROLE));
	}

	public void setAction(Actions action) {
		this.entityData.set(ACTION, action.name());
	}

	public void setRole(Roles role) {
		this.entityData.set(ROLE, role.name());
	}

	public Roles getRole() {
		return Roles.get(this.entityData.get(ROLE));
	}

	public void setTofunianType(TofunianType type) {
		this.entityData.set(TOFUNIAN_TYPE, type.name());
	}

	public TofunianType getTofunianType() {
		return TofunianType.get(this.entityData.get(TOFUNIAN_TYPE));
	}

	public void setTofunainHome(@Nullable BlockPos pos) {
		this.tofunainHome = pos;
	}

	@Nullable
	public BlockPos getTofunainHome() {
		return this.tofunainHome;
	}

	public void setTofunainJobBlock(@Nullable BlockPos tofunainJobBlock) {
		this.tofunainJobBlock = tofunainJobBlock;
	}

	@Nullable
	public BlockPos getTofunainJobBlock() {
		return this.tofunainJobBlock;
	}

	@Nullable
	public Entity changeDimension(ServerLevel server, ITeleporter teleporter) {
		setTofunainHome(null);
		if (xp != 0) {
			setTofunainJobBlock(null);
		}
		return super.changeDimension(server, teleporter);
	}

	protected void customServerAiStep() {
		if (!isTrading() && this.timeUntilReset > 0) {
			this.timeUntilReset--;
			if (this.timeUntilReset <= 0) {
				if (this.leveledUp) {
					increaseMerchantCareer();
					this.leveledUp = false;
				}
				addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
			}
		}
		if (this.previousCustomer != null && level() instanceof ServerLevel) {
			((ServerLevel) level()).onReputationEvent(ReputationEventType.TRADE, this.previousCustomer, this);
			level().broadcastEntityEvent(this, (byte) 14);
			this.previousCustomer = null;
		}
		if (getRole() == Roles.TOFUNIAN && isTrading()) {
			stopTrading();
		}

		this.tofunianJobCheck();
		this.tofunianHomeCheck();

		super.customServerAiStep();
	}

	public void aiStep() {
		this.updateSwingTime();
		super.aiStep();
	}

	public void tofunianJobCheck() {
		if ((level().getGameTime() + this.getId()) % (50) != 0) return;

		//validate job position
		if (this.getTofunainJobBlock() != null && this.getRole() != Roles.TOFUNIAN) {
			if (!Roles.getJobBlock(this.getRole().getPoiType()).contains(this.level().getBlockState(this.getTofunainJobBlock()))) {
				if (this.level() instanceof ServerLevel) {
					//don't forget release poi
					PoiManager poimanager = ((ServerLevel) this.level()).getPoiManager();
					if (poimanager.exists(this.getTofunainJobBlock(), (p_217230_) -> {
						return true;
					})) {
						poimanager.release(this.getTofunainJobBlock());
					}
				}
				this.setTofunainJobBlock(null);

				if (this.getTofunainLevel() == 1 && this.getVillagerXp() == 0) {
					this.setOffers(null);
					this.setRole(Roles.TOFUNIAN);
				}
			}
		}
	}

	public void tofunianHomeCheck() {
		if ((level().getGameTime() + this.getId()) % (50) != 0) return;

		//validate home position
		boolean tryFind = false;
		if (getTofunainHome() == null) {
			tryFind = true;
		} else {
			if (!this.level().getBlockState(this.getTofunainHome()).is(BlockTags.BEDS)) {
				//home position isnt a chest, keep current position but find better one
				tryFind = true;
				this.setTofunainHome(null);
			}
		}

		if (tryFind) {
			int range = 10;
			for (int x = -range; x <= range; x++) {
				for (int y = -range / 2; y <= range / 2; y++) {
					for (int z = -range; z <= range; z++) {
						BlockPos pos = this.blockPosition().offset(x, y, z);
						if (this.level().getBlockState(pos).is(BlockTags.BEDS)) {
							setTofunainHome(pos);
							return;
						}
					}
				}
			}
		}
	}

	protected void rewardTradeXp(MerchantOffer offer) {
		int i = 3 + this.random.nextInt(4);
		this.xp += offer.getXp();
		this.previousCustomer = this.getTradingPlayer();
		if (canLevelUp()) {
			this.timeUntilReset = 40;
			this.leveledUp = true;
			i += 5;
		}
		if (offer.shouldRewardExp())
			level().addFreshEntity(new ExperienceOrb(level(), getX(), getY() + 0.5D, getZ(), i));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_35282_, DifficultyInstance p_35283_, MobSpawnType p_35284_, @org.jetbrains.annotations.Nullable SpawnGroupData p_35285_, @org.jetbrains.annotations.Nullable CompoundTag p_35286_) {
		if (p_35282_.getBiome(this.blockPosition()).is(TofuBiomes.ZUNDA_FOREST)) {
			this.setTofunianType(TofunianType.ZUNDA);
		}

		return super.finalizeSpawn(p_35282_, p_35283_, p_35284_, p_35285_, p_35286_);
	}

	public InteractionResult mobInteract(Player p_35472_, InteractionHand p_35473_) {
		ItemStack itemstack = p_35472_.getItemInHand(p_35473_);
		if (itemstack.getItem() != TofuItems.TOFUNIAN_SPAWNEGG.get() && this.isAlive() && !this.isTrading() && !this.isSleeping() && !p_35472_.isSecondaryUseActive()) {
			if (p_35472_ instanceof ServerPlayer) {
				TofuAdvancements.MY_TOFU_CHILD.trigger((ServerPlayer) p_35472_);
			}
			if (this.isBaby()) {
				this.setUnhappy();
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			} else {
				boolean flag = this.getOffers().isEmpty();
				if (p_35473_ == InteractionHand.MAIN_HAND) {
					if (flag && !this.level().isClientSide) {
						this.setUnhappy();
					}

					p_35472_.awardStat(Stats.TALKED_TO_VILLAGER);
				}

				if (flag) {
					return InteractionResult.sidedSuccess(this.level().isClientSide);
				} else {
					if (!this.level().isClientSide && !this.offers.isEmpty()) {
						this.startTrading(p_35472_);
					}

					return InteractionResult.sidedSuccess(this.level().isClientSide);
				}
			}
		} else {
			return super.mobInteract(p_35472_, p_35473_);
		}
	}

	private void setUnhappy() {
		this.setUnhappyCounter(40);
		if (!this.level().isClientSide()) {
			this.playSound(TofuSounds.TOFUNIAN_NO.get(), this.getSoundVolume(), this.getVoicePitch());
		}

	}

	private void updateSpecialPrices(Player p_35541_) {
		int i = this.getPlayerReputation(p_35541_);
		if (i != 0) {
			for (MerchantOffer merchantoffer : this.getOffers()) {
				merchantoffer.addToSpecialPriceDiff(-Mth.floor((float) i * merchantoffer.getPriceMultiplier()));
			}
		}

		if (p_35541_.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
			MobEffectInstance mobeffectinstance = p_35541_.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
			int k = mobeffectinstance.getAmplifier();

			for (MerchantOffer merchantoffer1 : this.getOffers()) {
				double d0 = 0.3D + 0.0625D * (double) k;
				int j = (int) Math.floor(d0 * (double) merchantoffer1.getBaseCostA().getCount());
				merchantoffer1.addToSpecialPriceDiff(-Math.max(j, 1));
			}
		}

	}

	private void startTrading(Player p_35537_) {
		this.updateSpecialPrices(p_35537_);
		this.setTradingPlayer(p_35537_);
		this.openTradingScreen(p_35537_, this.getDisplayName(), this.getTofunainLevel());
	}

	public void setTradingPlayer(@Nullable Player player) {
		boolean flag = (getTradingPlayer() != null && player == null);
		super.setTradingPlayer(player);
		if (flag)
			stopTrading();
	}

	protected void stopTrading() {
		super.stopTrading();
		resetSpecialPrices();
	}

	private void resetSpecialPrices() {
		for (MerchantOffer merchantoffer : this.getOffers())
			merchantoffer.resetSpecialPriceDiff();
	}

	public boolean canRestock() {
		return true;
	}

	public void restock() {
		calculateDemandOfOffers();
		for (MerchantOffer merchantoffer : this.getOffers()) {
			merchantoffer.resetUses();
		}
		this.lastRestock = level().getGameTime();
		this.restocksToday++;
	}

	private boolean allowedToRestock() {
		return (this.restocksToday == 0 || (this.restocksToday < 2 && level().getGameTime() > this.lastRestock + 2400L));
	}

	public boolean canResetStock() {
		long i = this.lastRestock + 12000L;
		long j = this.level().getGameTime();
		boolean flag = j > i;
		long k = this.level().getDayTime();
		if (this.lastRestockDayTime > 0L) {
			long l = this.lastRestockDayTime / 24000L;
			long i1 = k / 24000L;
			flag |= i1 > l;
		}

		this.lastRestockDayTime = k;
		if (flag) {
			this.lastRestock = j;
			this.resetNumberOfRestocks();
		}

		return this.allowedToRestock() && this.hasUsedOffer();
	}

	private void resetNumberOfRestocks() {
		resetOffersAndAdjustForDemand();
		this.restocksToday = 0;
	}

	private void resetOffersAndAdjustForDemand() {
		int i = 2 - this.restocksToday;
		if (i > 0) {
			for (MerchantOffer merchantoffer : this.getOffers()) {
				merchantoffer.resetUses();
			}
		}
		for (int j = 0; j < i; j++) {
			calculateDemandOfOffers();
		}
	}

	private boolean hasUsedOffer() {
		for (MerchantOffer merchantoffer : this.getOffers()) {
			if (merchantoffer.needsRestock()) {
				return true;
			}

		}
		return false;
	}

	private void calculateDemandOfOffers() {
		for (MerchantOffer merchantoffer : this.getOffers()) {
			merchantoffer.updateDemand();
		}
	}

	public void setOffers(MerchantOffers offersIn) {
		this.offers = offersIn;
	}

	private boolean canLevelUp() {
		int i = this.tofunianLevel;
		return VillagerData.canLevelUp(i) && this.xp >= VillagerData.getMaxXpPerLevel(i);
	}

	private void increaseMerchantCareer() {
		setTofunainLevel(this.tofunianLevel + 1);
		updateTrades();
		this.level().broadcastEntityEvent(this, (byte) 5);
	}

	public void setTofunainLevel(int level) {
		this.tofunianLevel = level;
	}

	public int getTofunainLevel() {
		return this.tofunianLevel;
	}

	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte("FoodLevel", this.foodLevel);
		compound.put("Gossips", this.gossips.store(NbtOps.INSTANCE).copy());
		compound.putInt("Xp", this.xp);
		compound.putInt("Level", this.tofunianLevel);
		compound.putLong("LastRestock", this.lastRestock);
		compound.putLong("LastGossipDecay", this.lastGossipDecay);
		compound.putInt("RestocksToday", this.restocksToday);
		if (this.tofunainHome != null) {
			compound.put("TofunianHome", NbtUtils.writeBlockPos(this.tofunainHome));
		}
		if (this.tofunainJobBlock != null) {
			compound.put("TofunianJobBlock", NbtUtils.writeBlockPos(this.tofunainJobBlock));
		}
		compound.putString("Roles", getRole().name());
		compound.putString("TofunianType", getTofunianType().name());
	}

	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Offers", 10)) {
			this.offers = new MerchantOffers(compound.getCompound("Offers"));
		}
		if (compound.contains("FoodLevel", 1)) {
			this.foodLevel = compound.getByte("FoodLevel");
		}
		ListTag listtag = compound.getList("Gossips", 10);
		this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, listtag));
		ListTag listnbt = compound.getList("Gossips", 10);
		if (compound.contains("Xp", 3)) {
			this.xp = compound.getInt("Xp");
		}
		if (compound.contains("Level")) {
			this.tofunianLevel = compound.getInt("Level");
		}
		this.lastGossipDecay = compound.getLong("LastGossipDecay");
		this.lastRestock = compound.getLong("LastRestock");
		this.restocksToday = compound.getInt("RestocksToday");
		if (compound.contains("TofunianHome")) {
			this.tofunainHome = NbtUtils.readBlockPos(compound.getCompound("TofunianHome"));
		}
		if (compound.contains("TofunianJobBlock")) {
			this.tofunainJobBlock = NbtUtils.readBlockPos(compound.getCompound("TofunianJobBlock"));
		}
		if (compound.contains("Roles")) {
			setRole(Roles.get(compound.getString("Roles")));
		}
		if (compound.contains("TofunianType")) {
			setTofunianType(TofunianType.get(compound.getString("TofunianType")));
		}
		setCanPickUpLoot(true);
	}

	public int getVillagerXp() {
		return this.xp;
	}

	protected void pickUpItem(ItemEntity p_175445_1_) {
		ItemStack itemstack = p_175445_1_.getItem();
		if (wantsToPickUp(itemstack)) {
			SimpleContainer inventory = getInventory();
			boolean flag = inventory.canAddItem(itemstack);
			if (!flag)
				return;
			this.onItemPickup(p_175445_1_);
			this.take(p_175445_1_, itemstack.getCount());
			ItemStack itemstack1 = inventory.addItem(itemstack);
			if (itemstack1.isEmpty()) {
				p_175445_1_.discard();
			} else {
				itemstack.setCount(itemstack1.getCount());
			}
		}
	}

	public boolean canMate() {
		return (this.foodLevel + countFoodPointsInInventory() >= 42 && getAge() == 0);
	}

	private boolean hungry() {
		return (this.foodLevel < 12);
	}

	public boolean wantsToPickUp(ItemStack p_230293_1_) {
		Item item = p_230293_1_.getItem();
		return (WANTED_ITEMS.contains(item) && this.getInventory().canAddItem(p_230293_1_));
	}

	public boolean hasExcessFood() {
		return (countFoodPointsInInventory() >= 42);
	}

	public boolean wantsMoreFood() {
		return (countFoodPointsInInventory() < 42);
	}

	public boolean hasFarmSeeds() {
		return this.getInventory().hasAnyOf(ImmutableSet.of(TofuItems.SEEDS_SOYBEANS.get()));
	}

	private int countFoodPointsInInventory() {
		SimpleContainer inventory = this.getInventory();
		return FOOD_POINTS.entrySet().stream().mapToInt(p_226553_1_ -> inventory.countItem(p_226553_1_.getKey()) * p_226553_1_.getValue().intValue())

				.sum();
	}

	public void cookingFood() {
		for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
			ItemStack itemstack = this.getInventory().getItem(i);
			if (!itemstack.isEmpty() &&
					itemstack.getItem() == TofuItems.SEEDS_SOYBEANS.get()) {
				this.getInventory().removeItem(i, 1);
				cookResult();
			}
		}
	}

	public boolean eatFood() {
		if (this.getInventory().isEmpty()) {
			return false;
		}
		for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
			ItemStack itemstack = this.getInventory().getItem(i);
			if (!itemstack.isEmpty() && FOOD_POINTS.containsKey(itemstack.getItem())) {
				this.setItemInHand(InteractionHand.MAIN_HAND, itemstack.split(1));
				return true;
			}
		}
		return false;
	}

	private void cookResult() {
		this.getInventory().addItem(new ItemStack(TofuItems.TOFUGRILLED.get()));
	}

	public ItemStack eat(Level p_36185_, ItemStack p_36186_) {
		this.heal(p_36186_.getFoodProperties(this).getNutrition());
		this.playSound(SoundEvents.PLAYER_BURP, 0.5F, p_36185_.random.nextFloat() * 0.1F + 0.9F);

		return super.eat(p_36185_, p_36186_);
	}

	public void updateTrades() {
		Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap = TofuTrades.TOFUNIAN_TRADE.get(getRole());
		if (int2objectmap != null && !int2objectmap.isEmpty()) {
			VillagerTrades.ItemListing[] avillagertrades$ItemListing = int2objectmap.get(this.tofunianLevel);
			if (avillagertrades$ItemListing != null) {
				MerchantOffers merchantoffers = this.getOffers();
				addOffersFromItemListings(merchantoffers, avillagertrades$ItemListing, 2);
			}
		}
	}


	public void tick() {
		super.tick();
		this.maybeDecayGossip();
	}

	public void setLastHurtByMob(@Nullable LivingEntity p_70604_1_) {
		if (p_70604_1_ != null && this.level() instanceof ServerLevel) {
			((ServerLevel) this.level()).onReputationEvent(ReputationEventType.VILLAGER_HURT, p_70604_1_, this);
			if (this.isAlive() && p_70604_1_ instanceof Player) {
				this.level().broadcastEntityEvent(this, (byte) 13);
			}
		}

		super.setLastHurtByMob(p_70604_1_);
	}

	@OnlyIn(Dist.CLIENT)
	public void handleEntityEvent(byte p_70103_1_) {
		if (p_70103_1_ == 5) {
			this.agreeAnimationState.start(this.tickCount);
		} else if (p_70103_1_ == 12) {
			this.addParticlesAroundSelf(ParticleTypes.HEART);
		} else if (p_70103_1_ == 13) {
			this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
		} else if (p_70103_1_ == 14) {
			this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
		} else if (p_70103_1_ == 42) {
			this.addParticlesAroundSelf(ParticleTypes.SPLASH);
		} else {
			super.handleEntityEvent(p_70103_1_);
		}

	}

	public void die(DamageSource p_35419_) {
		Entity entity = p_35419_.getEntity();

		if (this.getTofunainJobBlock() != null) {
			if (this.level() instanceof ServerLevel) {
				//don't forget release poi
				PoiManager poimanager = ((ServerLevel) this.level()).getPoiManager();
				if (poimanager.exists(this.getTofunainJobBlock(), (p_217230_) -> {
					return true;
				})) {
					poimanager.release(this.getTofunainJobBlock());
				}
			}
		}
		super.die(p_35419_);
	}

	@Override
	public void onReputationEventFrom(ReputationEventType reputationEventType, Entity entity) {
		if (reputationEventType == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
			this.gossips.add(entity.getUUID(), GossipType.MAJOR_POSITIVE, 20);
			this.gossips.add(entity.getUUID(), GossipType.MINOR_POSITIVE, 25);
		} else if (reputationEventType == ReputationEventType.TRADE) {
			this.gossips.add(entity.getUUID(), GossipType.TRADING, 2);
		} else if (reputationEventType == ReputationEventType.VILLAGER_HURT) {
			this.gossips.add(entity.getUUID(), GossipType.MINOR_NEGATIVE, 25);
		} else if (reputationEventType == ReputationEventType.VILLAGER_KILLED) {
			this.gossips.add(entity.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
		}

	}

	public int getPlayerReputation(Player p_35533_) {
		return this.gossips.getReputation(p_35533_.getUUID(), (p_35427_) -> {
			return true;
		});
	}

	public void gossip(ServerLevel p_35412_, Tofunian p_35413_, long p_35414_) {
		if ((p_35414_ < this.lastGossipTime || p_35414_ >= this.lastGossipTime + 1200L) && (p_35414_ < p_35413_.lastGossipTime || p_35414_ >= p_35413_.lastGossipTime + 1200L)) {
			this.gossips.transferFrom(p_35413_.gossips, this.random, 10);
			this.lastGossipTime = p_35414_;
			p_35413_.lastGossipTime = p_35414_;
			this.spawnGolemIfNeeded(p_35412_, p_35414_, 4);
		}
	}

	public void spawnGolemIfNeeded(ServerLevel p_35398_, long p_35399_, int p_35400_) {
		if (this.wantsToSpawnGolem(p_35398_, p_35399_)) {
			AABB aabb = this.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);
			List<Tofunian> list = p_35398_.getEntitiesOfClass(Tofunian.class, aabb);
			List<Tofunian> list1 = list.stream().filter((p_186293_) -> {
				return p_186293_.wantsToSpawnGolem(p_35398_, p_35399_);
			}).limit(5L).collect(Collectors.toList());
			if (list1.size() >= p_35400_) {
				if (SpawnUtil.trySpawnMob(TofuEntityTypes.TOFU_GOLEM.get(), MobSpawnType.MOB_SUMMONED, p_35398_, this.blockPosition(), 10, 8, 6, SpawnUtil.Strategy.LEGACY_IRON_GOLEM).isPresent()) {
				}
			}
		}
	}

	private boolean wantsToSpawnGolem(ServerLevel p_35398_, long p_35399_) {
		AABB aabb = this.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);
		List<TofuGolem> list = p_35398_.getEntitiesOfClass(TofuGolem.class, aabb);

		if (list.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private void maybeDecayGossip() {
		long i = this.level().getGameTime();
		if (this.lastGossipDecay == 0L) {
			this.lastGossipDecay = i;
		} else if (i >= this.lastGossipDecay + 24000L) {
			this.gossips.decay();
			this.lastGossipDecay = i;
		}
	}

	public GossipContainer getGossips() {
		return this.gossips;
	}

	public void setGossips(Tag p_35456_) {
		this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, p_35456_));
	}

	public float getWalkTargetValue(BlockPos p_27573_, LevelReader p_27574_) {
		return p_27574_.getBlockState(p_27573_.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : p_27574_.getPathfindingCostFromLightLevels(p_27573_);
	}

	@Override
	protected Component getTypeName() {
		return Component.translatable("entity.tofucraft.tofunian." + this.getRole().name().toLowerCase(Locale.ROOT));
	}

	public enum TofunianType implements IExtensibleEnum {
		NORMAL,
		ZUNDA;

		private TofunianType() {

		}

		public static TofunianType get(String nameIn) {
			for (TofunianType role : values()) {
				if (role.name().equals(nameIn))
					return role;
			}
			return NORMAL;
		}

		public static TofunianType create(String name) {
			throw new IllegalStateException("Enum not extended");
		}
	}

	public enum Actions implements IExtensibleEnum {
		NORMAL,
		CRY,
		EAT;

		private Actions() {

		}

		public static Actions get(String nameIn) {
			for (Actions role : values()) {
				if (role.name().equals(nameIn))
					return role;
			}
			return NORMAL;
		}

		public static Actions create(String name) {
			throw new IllegalStateException("Enum not extended");
		}
	}

	public enum Roles implements IExtensibleEnum {
		TOFUCOOK(PoiTypes.FARMER), TOFUSMITH(PoiTypes.ARMORER), SOYWORKER(PoiTypes.LEATHERWORKER), TOFUNIAN(null);

		private static final Map<String, Roles> lookup;
		private final ResourceKey<PoiType> poitype;

		static {
			lookup = Arrays.stream(values()).collect(Collectors.toMap(Enum::name, p_220362_0_ -> p_220362_0_));
		}

		private Roles(ResourceKey<PoiType> poitype) {
			this.poitype = poitype;
		}

		public static Roles create(String name, ResourceKey<PoiType> poitype) {
			throw new IllegalStateException("Enum not extended");
		}


		public static Roles get(String nameIn) {
			for (Roles role : values()) {
				if (role.name().equals(nameIn))
					return role;
			}
			return TOFUNIAN;
		}

		@Nullable
		public static Roles getJob(ResourceKey<PoiType> poitypeIn) {
			for (Roles role : values()) {
				if (role != TOFUNIAN && role.getPoiType() == poitypeIn) {
					return role;
				}
			}
			return null;
		}

		@Nullable
		public static Roles get(BlockState blockState) {
			for (Roles role : values()) {
				if (role != TOFUNIAN) {
					Optional<Holder<PoiType>> poiTypeHolder = ForgeRegistries.POI_TYPES.getHolder(role.getPoiType());

					if (poiTypeHolder.isPresent() && poiTypeHolder.get().value().is(blockState)) {
						return role;
					}
				}
			}
			return null;
		}

		public static Set<BlockState> getJobBlock(ResourceKey<PoiType> poitypeIn) {
			for (Roles role : values()) {
				if (role != TOFUNIAN && role.getPoiType() == poitypeIn) {
					Optional<Holder<PoiType>> poiTypeHolder = ForgeRegistries.POI_TYPES.getHolder(role.getPoiType());

					if (poiTypeHolder.isPresent()) {
						return poiTypeHolder.get().value().matchingStates();
					}
				}
			}
			return null;
		}

		public static Set<BlockState> getJobMatch(Roles roles, ResourceKey<PoiType> poitypeIn) {
			if (roles != TOFUNIAN && roles.getPoiType() == poitypeIn) {
				Optional<Holder<PoiType>> poiTypeHolder = ForgeRegistries.POI_TYPES.getHolder(roles.getPoiType());

				if (poiTypeHolder.isPresent()) {
					return poiTypeHolder.get().value().matchingStates();
				}
			}
			return null;
		}

		@Nullable
		public ResourceKey<PoiType> getPoiType() {
			return poitype;
		}
	}

	class MoveToGoal extends Goal {
		final Tofunian hunter;
		final double stopDistance;
		final double speedModifier;

		MoveToGoal(Tofunian p_i50459_2_, double p_i50459_3_, double p_i50459_5_) {
			this.hunter = p_i50459_2_;
			this.stopDistance = p_i50459_3_;
			this.speedModifier = p_i50459_5_;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		public void stop() {
			Tofunian.this.navigation.stop();
		}

		public boolean canUse() {
			BlockPos blockpos = this.hunter.getTofunainHome();

			double distance = this.hunter.level().isDay() ? this.stopDistance : this.stopDistance / 4.0F;

			return blockpos != null && this.isTooFarAway(blockpos, distance);
		}

		public void tick() {
			BlockPos blockpos = this.hunter.getTofunainHome();
			if (blockpos != null && Tofunian.this.navigation.isDone()) {
				if (this.isTooFarAway(blockpos, 10.0D)) {
					Vec3 vector3d = (new Vec3((double) blockpos.getX() - this.hunter.getX(), (double) blockpos.getY() - this.hunter.getY(), (double) blockpos.getZ() - this.hunter.getZ())).normalize();
					Vec3 vector3d1 = vector3d.scale(10.0D).add(this.hunter.getX(), this.hunter.getY(), this.hunter.getZ());
					Tofunian.this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
				} else {
					Tofunian.this.navigation.moveTo((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), this.speedModifier);
				}
			}

		}

		private boolean isTooFarAway(BlockPos p_220846_1_, double p_220846_2_) {
			return !p_220846_1_.closerThan(this.hunter.blockPosition(), p_220846_2_);
		}
	}

	public class GetItemGoal<T extends Tofunian> extends Goal {
		private final T mob;

		public GetItemGoal(T p_i50572_2_) {
			this.mob = p_i50572_2_;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		public boolean canUse() {

			List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0D, 4.0D, 4.0D), Tofunian.ALLOWED_ITEMS);
			if (!list.isEmpty() && this.mob.hasLineOfSight(list.get(0))) {
				return this.mob.getNavigation().moveTo(list.get(0), (double) 1.0F);
			}

			return false;
		}
	}

	private static boolean isHalloween() {
		LocalDate localdate = LocalDate.now();
		int i = localdate.get(ChronoField.DAY_OF_MONTH);
		int j = localdate.get(ChronoField.MONTH_OF_YEAR);
		return j == 10 && i >= 20 || j == 11 && i <= 3;
	}
}
