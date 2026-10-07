package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import com.beckytidus.girlfriendmod.dialogue.WaitAndFollowLines;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import com.beckytidus.girlfriendmod.util.RelationshipTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Prediction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class GirlFriendEntity extends PathfinderMob {
    private static final int PHRASE_MIN_INTERVAL_TICKS = 1200;
    private static final int GIFT_MIN_INTERVAL_TICKS = 2400;
    private static final int SELF_HEAL_INTERVAL_TICKS = 800;
    private static final int SENSE_INTERVAL_TICKS = 400;
    private static final int SENSE_COOLDOWN_TICKS = 2400;
    private static final int OWNER_HEAL_COOLDOWN_TICKS = 2400;
    private static final int POUT_COOLDOWN_TICKS = 60;
    private static final int SULK_TICKS = 600;
    private static final int PICKUP_MESSAGE_COOLDOWN_TICKS = 200;
    private static final double TELEPORT_DISTANCE = 32.0;
    private static final double SENSE_RANGE = 16.0;
    private static final double HUG_KISS_RANGE = 2.5;
    private static final int HUG_KISS_COOLDOWN_TICKS = 600;
    private static final int HUNGER_DECAY_INTERVAL = 1200;
    private static final Identifier BOND_DAMAGE_ID = Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "bond_damage");
    private static final Identifier BOND_HEALTH_ID = Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "bond_health");

    public static final int EMOTE_NONE = 0;
    public static final int EMOTE_CROUCH = 1;
    public static final int EMOTE_NOD = 2;
    public static final int EMOTE_HUG = 3;
    public static final int EMOTE_KISS = 4;
    public static final int EMOTE_DANCE = 5;
    public static final int EMOTE_WAVE = 6;
    public static final int EMOTE_HIGHFIVE = 7;
    public static final int EMOTE_POUT = 8;

    private static final EntityDataAccessor<String> SKIN_OWNER_NAME = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ResolvableProfile> SKIN_PROFILE = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.RESOLVABLE_PROFILE);
    private static final EntityDataAccessor<String> TEXTURE_VARIANT = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DISPLAY_NAME = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FRIENDLY_FIRE = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SULK_TICKS_DATA = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AFFECTION = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMOTE_TYPE = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMOTE_TICKS = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RELATIONSHIP_LEVEL = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOOD_LEVEL = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);

    @Nullable
    private EntityReference<LivingEntity> ownerRef;
    private long lastPhraseTick;
    private long lastGiftTick;
    private long lastSelfHealTick;
    private long lastSenseTick;
    private long lastSenseMessageTick = -SENSE_COOLDOWN_TICKS;
    private long lastOwnerHealTick = -OWNER_HEAL_COOLDOWN_TICKS;
    private long lastDeathSaveDay = -1;
    private long lastPoutTick = -POUT_COOLDOWN_TICKS;
    private long lastPickupMessageTick = -PICKUP_MESSAGE_COOLDOWN_TICKS;
    private long lastHungerDecayTick;
    private long lastInteractionTick;
    private long lastGoodnightDay = -1;
    private int hugKissCooldown;
    private boolean wasFarFromOwner = true;
    private int announcedTier = -1;

    public GirlFriendEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createGirlfriendAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(2, new FollowOwnerGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new PickFlowerGoal());
        this.goalSelector.addGoal(5, new WanderOffGoal());
        // Only potter about while she's with you; "wait here" means stay put.
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return isFollowingOwner() && !isSitting() && !isSulking() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new DefendOwnerGoal());
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_OWNER_NAME, "");
        builder.define(SKIN_PROFILE, ResolvableProfile.Static.EMPTY);
        builder.define(TEXTURE_VARIANT, "default");
        builder.define(DISPLAY_NAME, "");
        builder.define(OWNER, Optional.empty());
        builder.define(SITTING, false);
        builder.define(FOLLOWING, true);
        builder.define(FRIENDLY_FIRE, false);
        builder.define(SULK_TICKS_DATA, 0);
        builder.define(AFFECTION, 50);
        builder.define(HUNGER, 80);
        builder.define(EMOTE_TYPE, 0);
        builder.define(EMOTE_TICKS, 0);
        builder.define(RELATIONSHIP_LEVEL, 0);
        builder.define(MOOD_LEVEL, 50);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference.store(ownerRef, output, "Owner");
        output.putInt("RelationshipLevel", getRelationshipLevel());
        output.putString("CustomName", getPlayerCustomName());
        output.putBoolean("IsFollowing", isFollowingOwner());
        output.putLong("LastPhraseTick", lastPhraseTick);
        output.putLong("LastGiftTick", lastGiftTick);
        output.putLong("LastHealTick", lastSelfHealTick);
        output.putString("TextureVariant", getTextureVariant());
        output.putBoolean("Sitting", isSitting());
        output.putInt("MoodLevel", getMoodLevel());
        output.putString("SkinOwnerName", getSkinOwnerName());
        output.store("SkinProfile", ResolvableProfile.CODEC, getSkinProfile());
        output.putInt("Affection", getAffection());
        output.putInt("Hunger", getHunger());
        output.putLong("LastHungerDecayTick", lastHungerDecayTick);
        output.putBoolean("FriendlyFire", isFriendlyFire());
        output.putInt("SulkTicks", getSulkTicks());
        output.putLong("LastDeathSaveDay", lastDeathSaveDay);
        output.putLong("LastGoodnightDay", lastGoodnightDay);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setOwnerReference(EntityReference.readWithOldOwnerConversion(input, "Owner", this.level()));
        setRelationshipLevel(input.getIntOr("RelationshipLevel", 0));
        announcedTier = RelationshipTier.of(getRelationshipLevel()).ordinal();
        setPlayerCustomName(input.getStringOr("CustomName", ""));
        entityData.set(FOLLOWING, input.getBooleanOr("IsFollowing", true));
        lastPhraseTick = input.getLongOr("LastPhraseTick", 0);
        lastGiftTick = input.getLongOr("LastGiftTick", 0);
        lastSelfHealTick = input.getLongOr("LastHealTick", 0);
        setTextureVariant(input.getStringOr("TextureVariant", "default"));
        entityData.set(SITTING, input.getBooleanOr("Sitting", false));
        setMoodLevel(input.getIntOr("MoodLevel", 50));
        entityData.set(SKIN_OWNER_NAME, input.getStringOr("SkinOwnerName", ""));
        input.read("SkinProfile", ResolvableProfile.CODEC).ifPresent(p -> entityData.set(SKIN_PROFILE, p));
        setAffection(input.getIntOr("Affection", 50));
        setHunger(input.getIntOr("Hunger", 80));
        lastHungerDecayTick = input.getLongOr("LastHungerDecayTick", 0);
        entityData.set(FRIENDLY_FIRE, input.getBooleanOr("FriendlyFire", false));
        entityData.set(SULK_TICKS_DATA, input.getIntOr("SulkTicks", 0));
        lastDeathSaveDay = input.getLongOr("LastDeathSaveDay", -1);
        lastGoodnightDay = input.getLongOr("LastGoodnightDay", -1);
        applyBondAttributes();
    }

    // ---------------------------------------------------------------- equipment

    private static boolean isEquippable(ItemStack stack) {
        return stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)
            || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SPEARS)
            || stack.is(Items.SHIELD) || stack.is(Items.TRIDENT);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        return level.getGameRules().get(GameRules.MOB_GRIEFING) && canPickUpLoot() && isEquippable(stack);
    }

    @Override
    @Nullable
    public net.minecraft.tags.TagKey<Item> getPreferredWeaponType() {
        return ItemTags.SWORDS;
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        super.onEquipItem(slot, oldStack, newStack);
        if (level().isClientSide() || newStack.isEmpty() || oldStack.isEmpty() == false) return;
        long now = getWorldTime();
        if (now - lastPickupMessageTick < PICKUP_MESSAGE_COOLDOWN_TICKS) return;
        lastPickupMessageTick = now;
        say("Ooh, a " + newStack.getHoverName().getString() + "! I'm keeping this~");
        setMoodLevel(getMoodLevel() + 5);
        spawnHeartParticles();
    }

    // ---------------------------------------------------------------- owner

    @Nullable
    public Player getOwner() {
        return EntityReference.getLivingEntity(ownerRef, level()) instanceof Player p ? p : null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER).map(EntityReference::getUUID).orElse(null);
    }

    public boolean isOwnedBy(Player player) {
        UUID owner = getOwnerUUID();
        return owner != null && owner.equals(player.getUUID());
    }

    public boolean hasOwner() {
        return entityData.get(OWNER).isPresent();
    }

    public void setOwner(@Nullable Player player) {
        setOwnerReference(player != null ? EntityReference.of((LivingEntity) player) : null);
        if (player != null) setPersistenceRequired();
    }

    private void setOwnerReference(@Nullable EntityReference<LivingEntity> ref) {
        this.ownerRef = ref;
        entityData.set(OWNER, Optional.ofNullable(ref));
    }

    /** Never fight the person she loves, whatever the AI goals think. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player p && isOwnedBy(p)) return;
        if (target instanceof GirlFriendEntity other && getOwnerUUID() != null && getOwnerUUID().equals(other.getOwnerUUID())) return;
        super.setTarget(target);
    }

    // ---------------------------------------------------------------- talking

    public void say(String message) {
        if (getOwner() instanceof ServerPlayer owner) {
            owner.sendSystemMessage(GirlfriendText.speech(getDisplayNameForChat(), message));
        }
    }

    public void narrate(String action) {
        if (getOwner() instanceof ServerPlayer owner) {
            owner.sendSystemMessage(GirlfriendText.action(getDisplayNameForChat(), action));
        }
    }

    public void playCute(SoundEvent sound, float pitch) {
        if (level() instanceof ServerLevel sl) {
            sl.playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 0.6f, pitch);
        }
    }

    public void onChatResponse() {
        addRelationship(1);
        setMoodLevel(getMoodLevel() + 3);
        addAffection(2);
        spawnHeartParticles();
    }

    // ---------------------------------------------------------------- particles

    public void spawnHeartParticles() {
        spawnParticles(ParticleTypes.HEART, 4);
    }

    private void spawnParticles(ParticleOptions type, int count) {
        if (!(level() instanceof ServerLevel sw)) return;
        for (int i = 0; i < count; i++) {
            double x = getX() + (getRandom().nextDouble() - 0.5) * getBbWidth();
            double y = getY() + getBbHeight() * 0.8 + getRandom().nextDouble() * 0.3;
            double z = getZ() + (getRandom().nextDouble() - 0.5) * getBbWidth();
            sw.sendParticles(type, x, y, z, 1, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private long getWorldTime() {
        return this.level() instanceof ServerLevel sl ? sl.getGameTime() : 0;
    }

    // ---------------------------------------------------------------- sulking (replaces "attack the owner")

    public boolean isSulking() {
        return getSulkTicks() > 0;
    }

    /** Kept for older call sites: she is never hostile to her owner any more, only upset. */
    public boolean isAngeredAtOwner() {
        return isSulking();
    }

    public int getSulkTicks() {
        return entityData.get(SULK_TICKS_DATA);
    }

    private void startSulking() {
        entityData.set(SULK_TICKS_DATA, SULK_TICKS);
        triggerEmote(EMOTE_POUT, 60);
    }

    public void forgive() {
        if (!isSulking()) return;
        entityData.set(SULK_TICKS_DATA, 0);
        say(pick("...Okay. I forgive you. Just be gentle, okay?", "Hmph. Fine. You're lucky you're cute.", "Apology accepted~ Come here."));
        spawnHeartParticles();
    }

    // ---------------------------------------------------------------- goals

    private class FollowOwnerGoal extends Goal {
        private static final double START_DISTANCE = 6.0;
        private static final double STOP_DISTANCE = 2.5;
        private int recalcTicks;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowingOwner() || owner.isSpectator() || isSitting() || isSulking()) return false;
            return distanceToSqr(owner) > START_DISTANCE * START_DISTANCE;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowingOwner() || isSitting() || isSulking()) return false;
            return distanceToSqr(owner) > STOP_DISTANCE * STOP_DISTANCE;
        }

        @Override
        public void start() {
            recalcTicks = 0;
        }

        @Override
        public void stop() {
            getNavigation().stop();
            setSprinting(false);
        }

        @Override
        public void tick() {
            Player owner = getOwner();
            if (owner == null) return;
            getLookControl().setLookAt(owner, 10.0F, getMaxHeadXRot());
            if (--recalcTicks > 0) return;
            recalcTicks = 10;
            boolean far = distanceToSqr(owner) > 12 * 12;
            setSprinting(far);
            getNavigation().moveTo(owner, far ? 1.25 : 1.0);
        }
    }

    private class DefendOwnerGoal extends Goal {
        private static final double DEFEND_RANGE = 16.0;

        @Nullable
        private LivingEntity findThreat() {
            Player owner = getOwner();
            if (owner == null || owner.isSpectator()) return null;
            if (distanceToSqr(owner) > DEFEND_RANGE * DEFEND_RANGE) return null;
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && attacker.isAlive() && !(attacker instanceof GirlFriendEntity)
                    && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100) {
                return attacker;
            }
            for (Mob mob : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(DEFEND_RANGE), m -> m != GirlFriendEntity.this && m.getTarget() == owner)) {
                return mob;
            }
            return null;
        }

        @Override
        public boolean canUse() {
            if (getTarget() != null) return false;
            return findThreat() != null;
        }

        @Override
        public void start() {
            LivingEntity threat = findThreat();
            if (threat == null) return;
            setTarget(threat);
            if (getRandom().nextFloat() < 0.4f) {
                say(pick("Hey! Hands off my person!", "I've got you, don't worry!", "Nobody hurts you while I'm here!", "Leave them alone!"));
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Wanders to a nearby flower, picks a copy of it and brings it to you. */
    private class PickFlowerGoal extends Goal {
        private static final int RANGE = 8;
        @Nullable
        private BlockPos flower;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowingOwner() || isSitting() || isSulking() || getTarget() != null) return false;
            if (distanceToSqr(owner) > 16 * 16) return false;
            if (getWorldTime() - lastGiftTick < GIFT_MIN_INTERVAL_TICKS) return false;
            if (getRandom().nextInt(400) != 0) return false;
            flower = findFlower();
            return flower != null;
        }

        @Override
        public void start() {
            if (flower != null) getNavigation().moveTo(flower.getX() + 0.5, flower.getY(), flower.getZ() + 0.5, 0.8);
        }

        @Override
        public boolean canContinueToUse() {
            return flower != null && !getNavigation().isDone() && getTarget() == null;
        }

        @Override
        public void stop() {
            if (flower != null && distanceToSqr(flower.getX() + 0.5, flower.getY(), flower.getZ() + 0.5) < 3 * 3) {
                BlockState state = level().getBlockState(flower);
                Item item = state.getBlock().asItem();
                if (state.is(BlockTags.FLOWERS) && item != Items.AIR) {
                    triggerEmote(EMOTE_CROUCH, 20);
                    lastGiftTick = getWorldTime();
                    ItemStack gift = new ItemStack(item);
                    giveToOwner(gift);
                    narrate("picked you a " + gift.getHoverName().getString() + " ✿");
                    playCute(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f);
                    addAffection(1);
                }
            }
            flower = null;
        }

        @Nullable
        private BlockPos findFlower() {
            BlockPos center = blockPosition();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int attempt = 0; attempt < 40; attempt++) {
                mutable.set(center.getX() + getRandom().nextInt(RANGE * 2 + 1) - RANGE,
                    center.getY() + getRandom().nextInt(5) - 2,
                    center.getZ() + getRandom().nextInt(RANGE * 2 + 1) - RANGE);
                if (level().getBlockState(mutable).is(BlockTags.SMALL_FLOWERS)) return mutable.immutable();
            }
            return null;
        }
    }

    private class WanderOffGoal extends Goal {
        private int wanderTicks;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowingOwner() || isSitting() || getTarget() != null) return false;
            if (distanceToSqr(owner) > 8 * 8) return false;
            return getRandom().nextInt(600) == 0;
        }

        @Override
        public void start() {
            wanderTicks = 0;
            double angle = getRandom().nextDouble() * Math.PI * 2;
            double dist = 3 + getRandom().nextDouble() * 5;
            getNavigation().moveTo(getX() + Math.cos(angle) * dist, getY(), getZ() + Math.sin(angle) * dist, 0.7);
        }

        @Override
        public boolean canContinueToUse() {
            return wanderTicks++ < 120 && getTarget() == null && !getNavigation().isDone();
        }
    }

    // ---------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        if (getPlayerCustomName().isEmpty()) {
            setPlayerCustomName(FemaleNames.pickRandom(getRandom()));
        }
        if (getSkinOwnerName().isEmpty() && ("default".equals(getTextureVariant()) || getTextureVariant().isEmpty())) {
            setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(getRandom()));
        }

        tickEmote();
        Player owner = getOwner();
        if (owner == null) return;
        setPersistenceRequired();
        long now = getWorldTime();

        if (isFollowingOwner() && !isSitting() && owner.level() == level() && owner.distanceToSqr(this) > TELEPORT_DISTANCE * TELEPORT_DISTANCE && owner.onGround()) {
            teleportTo(owner.getX(), owner.getY(), owner.getZ());
            getNavigation().stop();
        }

        if (isSulking()) {
            int left = getSulkTicks() - 1;
            entityData.set(SULK_TICKS_DATA, left);
            if (left % 40 == 0) spawnParticles(ParticleTypes.ANGRY_VILLAGER, 1);
            if (left == 0) {
                say(pick("...I'm not mad anymore. Just don't do that again.", "Okay, I'm done pouting. Hug?", "Hmph. Fine, I missed you."));
                spawnHeartParticles();
            }
            getNavigation().stop();
            return;
        }

        if (hugKissCooldown > 0) hugKissCooldown--;
        if (now - lastHungerDecayTick >= HUNGER_DECAY_INTERVAL) {
            setHunger(getHunger() - 2);
            lastHungerDecayTick = now;
            if (getHunger() == 20) say(pick("My tummy's rumbling... got a snack?", "I'm getting hungry~ Cake? Pie? Anything?"));
            if (getHunger() < 20) setMoodLevel(getMoodLevel() - 1);
        }

        double distToOwner = owner.distanceToSqr(this);
        if (wasFarFromOwner && distToOwner < 64.0 && getEmoteTicks() <= 0 && getTarget() == null) {
            wasFarFromOwner = false;
            triggerEmote(EMOTE_WAVE, 30);
            say(pick("There you are! I missed you~", "Hiii! Over here!", "You're back!"));
        }
        if (distToOwner > 400.0) wasFarFromOwner = true;

        if (distToOwner < HUG_KISS_RANGE * HUG_KISS_RANGE && hugKissCooldown <= 0 && getTarget() == null && getRandom().nextFloat() < 0.003f) {
            hugKissCooldown = HUG_KISS_COOLDOWN_TICKS;
            boolean kiss = getRelationshipTier().atLeast(RelationshipTier.DATING) && getRandom().nextBoolean();
            triggerEmote(kiss ? EMOTE_KISS : EMOTE_HUG, 40);
            narrate(kiss ? "gives you a quick kiss on the cheek" : "wraps you in a surprise hug");
            spawnHeartParticles();
            addAffection(1);
        }
        if (getEmoteTicks() <= 0 && getRandom().nextFloat() < 0.002f) {
            if (getMoodLevel() >= 70 && getAffection() >= 60 && getRandom().nextFloat() < 0.4f) triggerEmote(EMOTE_DANCE, 60);
            else triggerEmote(getRandom().nextBoolean() ? EMOTE_NOD : EMOTE_CROUCH, 20);
        }

        int phraseInterval = Math.max(PHRASE_MIN_INTERVAL_TICKS, 2400 - getRelationshipLevel() * 12);
        if (now - lastPhraseTick >= phraseInterval && getRandom().nextFloat() < 0.02f) {
            say(randomIdleLine(owner));
            addRelationship(1);
            lastPhraseTick = now;
        }

        int giftInterval = Math.max(GIFT_MIN_INTERVAL_TICKS, 6000 - getRelationshipLevel() * 36);
        if (now - lastGiftTick >= giftInterval && getRandom().nextFloat() < 0.02f) {
            giveRandomGift();
            lastGiftTick = now;
        }

        if (getHealth() < getMaxHealth() * 0.7 && now - lastSelfHealTick >= SELF_HEAL_INTERVAL_TICKS) {
            heal(5.0f);
            lastSelfHealTick = now;
        }

        if (getRelationshipTier().atLeast(RelationshipTier.PARTNER) && distToOwner < 10 * 10
                && owner.getHealth() < owner.getMaxHealth() * 0.35f && now - lastOwnerHealTick >= OWNER_HEAL_COOLDOWN_TICKS) {
            lastOwnerHealTick = now;
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            triggerEmote(EMOTE_HUG, 40);
            say(pick("You're hurt! Hold still, let me help.", "Hey, hey, I've got you. Breathe.", "Don't scare me like that! Here~"));
            playCute(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f);
            spawnHeartParticles();
        }

        if (now - lastSenseTick >= SENSE_INTERVAL_TICKS) {
            lastSenseTick = now;
            trySenseDanger(now);
        }

        if (getMoodLevel() >= 70 && getRandom().nextFloat() < 0.005f) spawnHeartParticles();
        if (getTarget() != null && getRandom().nextFloat() < 0.05f) spawnParticles(ParticleTypes.CRIT, 2);

        tickTierProgress(owner);

        if (isSitting()) {
            getNavigation().stop();
            setSprinting(false);
        }
    }

    private void tickEmote() {
        int ticks = getEmoteTicks();
        if (ticks > 0) {
            entityData.set(EMOTE_TICKS, ticks - 1);
            if (ticks - 1 <= 0) entityData.set(EMOTE_TYPE, EMOTE_NONE);
        }
    }

    private void trySenseDanger(long now) {
        if (now - lastSenseMessageTick < SENSE_COOLDOWN_TICKS) return;
        var hostiles = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(SENSE_RANGE), m -> m.isAlive() && m.getTarget() == null);
        if (hostiles.isEmpty() || getRandom().nextFloat() > 0.25f) return;
        lastSenseMessageTick = now;
        say(pick("Psst, I think something's lurking nearby...", "Stay close, I hear something.", "Careful, there's a monster around here."));
    }

    private String randomIdleLine(Player owner) {
        long time = level().getOverworldClockTime() % 24000;
        if (level().isRaining() && getRandom().nextFloat() < 0.4f) {
            return pick("I love the sound of rain... especially with you.", "Wanna stay under a roof and listen to the rain?", "Rainy days are cuddle days. Just saying.");
        }
        if (time > 13000 && time < 23000 && getRandom().nextFloat() < 0.4f) {
            return pick("The stars are so pretty tonight.", "Getting late... wanna head to bed soon?", "Night time makes me want to hold your hand.");
        }
        RelationshipTier tier = getRelationshipTier();
        if (tier == RelationshipTier.JUST_MET) {
            return pick("So... what do you like to do for fun?", "I like it here. With you, I mean. Um.", "You seem nice. I'm glad I met you.",
                "Is it weird that I'm a little nervous?", "Tell me about yourself sometime?");
        }
        if (!tier.atLeast(RelationshipTier.PARTNER)) {
            return pick("You make adventuring feel less scary.", "I like watching you build stuff.", "Heehee, you're kinda cute when you're focused.",
                "Can we go see the ocean together sometime?", "I saved you a spot next to me~", "I think about you a lot, you know.");
        }
        return pick("I love you. Just wanted you to hear it.", "Every day with you is my favorite day.", "Wherever you go, I'm going too.",
            "You're my favorite person in every world.", "We should build a little house together. With a garden!",
            "Do you remember when we first met? I was so shy.", "I'm really proud of you, " + owner.getName().getString() + ".");
    }

    // ---------------------------------------------------------------- relationship

    public RelationshipTier getRelationshipTier() {
        return RelationshipTier.of(getRelationshipLevel());
    }

    private void tickTierProgress(Player owner) {
        int tier = getRelationshipTier().ordinal();
        if (announcedTier < 0) {
            announcedTier = tier;
            return;
        }
        if (tier > announcedTier && owner instanceof ServerPlayer sp) {
            RelationshipTier t = getRelationshipTier();
            sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            sp.connection.send(new ClientboundSetTitleTextPacket(Component.literal("♥ " + t.title + " ♥").withStyle(t.color)));
            sp.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(getDisplayNameForChat() + " feels closer to you")));
            sp.sendSystemMessage(GirlfriendText.info("New perk: " + t.perk));
            say(pick("I... I think I'm really falling for you.", "Is it obvious how happy you make me?", "I never want this to end.", "You mean everything to me."));
            playCute(SoundEvents.PLAYER_LEVELUP, 1.3f);
            spawnParticles(ParticleTypes.HEART, 16);
            triggerEmote(EMOTE_DANCE, 60);
        }
        if (tier != announcedTier) {
            announcedTier = tier;
            applyBondAttributes();
        }
    }

    /** Stronger bond = tougher, harder-hitting bodyguard. */
    private void applyBondAttributes() {
        int level = getRelationshipLevel();
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.addOrReplacePermanentModifier(new AttributeModifier(BOND_DAMAGE_ID, level / 15.0, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.addOrReplacePermanentModifier(new AttributeModifier(BOND_HEALTH_ID, level / 5.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** Called when her owner is about to die. Soulmates get one rescue per Minecraft day. */
    public boolean tryRescueOwner(ServerPlayer owner) {
        if (!getRelationshipTier().atLeast(RelationshipTier.SOULMATE)) return false;
        if (!isAlive() || distanceToSqr(owner) > 24 * 24) return false;
        long day = level().getOverworldClockTime() / 24000L;
        if (day == lastDeathSaveDay) return false;
        lastDeathSaveDay = day;
        owner.setHealth(6.0f);
        owner.removeAllEffects();
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
        owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, owner.getX(), owner.getY() + 1, owner.getZ(), 40, 0.5, 0.8, 0.5, 0.3);
            sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        }
        say(pick("NOT TODAY! I'm not losing you!", "Stay with me! I've got you!", "Don't you dare leave me!"));
        owner.sendSystemMessage(GirlfriendText.info(getDisplayNameForChat() + "'s love saved you. (Once per day)"));
        return true;
    }

    public void onOwnerSleep() {
        long day = level().getOverworldClockTime() / 24000L;
        if (day == lastGoodnightDay) return;
        lastGoodnightDay = day;
        say(pick("Goodnight~ Sweet dreams. I'll be right here.", "Sleep well. Dream of me, okay?", "*yawns* Night night~"));
        addRelationship(2);
        setMoodLevel(getMoodLevel() + 10);
        if (isFollowingOwner() && !isSitting()) toggleSit();
    }

    // ---------------------------------------------------------------- gifts & food

    private void giveToOwner(ItemStack stack) {
        Player owner = getOwner();
        if (owner == null) return;
        if (!owner.getInventory().add(stack)) {
            owner.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    private void giveRandomGift() {
        RelationshipTier tier = getRelationshipTier();
        Item[] pool;
        if (tier.atLeast(RelationshipTier.FOREVER)) {
            pool = new Item[]{Items.DIAMOND, Items.EMERALD, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.NETHERITE_SCRAP};
        } else if (tier.atLeast(RelationshipTier.PARTNER)) {
            pool = new Item[]{Items.DIAMOND, Items.EMERALD, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.EXPERIENCE_BOTTLE, Items.CAKE};
        } else if (tier.atLeast(RelationshipTier.CRUSH)) {
            pool = new Item[]{Items.EMERALD, Items.AMETHYST_SHARD, Items.COOKIE, Items.PUMPKIN_PIE, Items.IRON_INGOT, Items.GOLD_INGOT};
        } else {
            pool = new Item[]{Items.COOKIE, Items.APPLE, Items.POPPY, Items.DANDELION, Items.SWEET_BERRIES};
        }
        ItemStack gift = new ItemStack(pool[getRandom().nextInt(pool.length)]);
        if (gift.is(Items.COOKIE)) gift.setCount(3);
        giveToOwner(gift);
        narrate(pick("slips a " + gift.getHoverName().getString() + " into your pocket", "made you a little present: " + gift.getHoverName().getString(),
            "found a " + gift.getHoverName().getString() + " and thought of you"));
        playCute(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.0f);
        addRelationship(2);
    }

    public void feedEntity(ItemStack stack) {
        int food;
        if (stack.is(Items.CAKE) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.COOKIE)) food = 25;
        else if (stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.COOKED_CHICKEN) || stack.is(Items.BREAD) || stack.is(Items.BAKED_POTATO)) food = 15;
        else food = 8;
        boolean favorite = food >= 25;
        heal(food / 3.0f);
        setHunger(getHunger() + food);
        addRelationship(favorite ? 4 : 2);
        addAffection(favorite ? 5 : 2);
        setMoodLevel(getMoodLevel() + (favorite ? 8 : 3));
        triggerEmote(favorite ? EMOTE_DANCE : EMOTE_NOD, favorite ? 40 : 20);
        playCute(SoundEvents.GENERIC_EAT.value(), 1.3f);
        if (favorite) {
            say(pick("Sweets?! You spoil me~", "Mmm, my favorite! Thank you!", "You remembered I like these!"));
            spawnHeartParticles();
        } else {
            say(pick("Thanks, that hit the spot!", "Yum~ thank you!", "You always take care of me."));
        }
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Player owner = getOwner();
        if (owner != null && source.getEntity() instanceof Player attacker && attacker == owner) {
            long now = getWorldTime();
            if (!isFriendlyFire()) {
                if (now - lastPoutTick >= POUT_COOLDOWN_TICKS) {
                    lastPoutTick = now;
                    triggerEmote(EMOTE_POUT, 30);
                    say(HugAndHitResponses.pickBonk(this));
                }
                return false;
            }
            addRelationship(-3);
            setMoodLevel(getMoodLevel() - 10);
            if (!isSulking()) {
                startSulking();
                say(HugAndHitResponses.pickHitByOwner(this));
            }
            return super.hurtServer(level, source, amount);
        }

        boolean result = super.hurtServer(level, source, amount);
        if (result && owner != null && source.getEntity() instanceof LivingEntity && getRandom().nextFloat() < 0.3f) {
            say(HugAndHitResponses.pickHitByOther(this));
        }
        if (result) setMoodLevel(getMoodLevel() - 2);
        if (getHealth() <= 0 && owner instanceof ServerPlayer sp) {
            sp.sendSystemMessage(GirlfriendText.info(getDisplayNameForChat() + " has fallen... ✝"));
        }
        return result;
    }

    /** Runs on both client and server, so the client can open her menu. */
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND) return net.minecraft.world.InteractionResult.PASS;
        return com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler.handleGirlFriendInteraction(player, this);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return !hasOwner() && !isPersistenceRequired();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    // ---------------------------------------------------------------- state accessors

    public boolean canInteract(Player player) {
        if (level().isClientSide()) return true;
        return getWorldTime() - lastInteractionTick >= 10;
    }

    public void setLastInteractionTick(long tick) {
        this.lastInteractionTick = tick;
    }

    public Component getStatsDisplayText() {
        RelationshipTier tier = getRelationshipTier();
        return Component.literal(getDisplayNameForChat() + " ").withColor(0xF5A9F2)
            .append(Component.literal("[" + tier.title + "] ").withStyle(tier.color))
            .append(GirlfriendText.hearts(getRelationshipLevel(), 100, 10))
            .append(Component.literal("  Mood " + getMoodLevel() + "  Hunger " + getHunger() + "  HP " + (int) getHealth() + "/" + (int) getMaxHealth()).withColor(0xBBBBBB));
    }

    public int getRelationshipLevel() {
        return entityData.get(RELATIONSHIP_LEVEL);
    }

    public void setRelationshipLevel(int value) {
        entityData.set(RELATIONSHIP_LEVEL, Math.clamp(value, 0, 100));
    }

    public void addRelationship(int amount) {
        setRelationshipLevel(getRelationshipLevel() + amount);
    }

    public int getMoodLevel() {
        return entityData.get(MOOD_LEVEL);
    }

    public void setMoodLevel(int value) {
        entityData.set(MOOD_LEVEL, Math.clamp(value, 0, 100));
    }

    public int getAffection() {
        return entityData.get(AFFECTION);
    }

    public void setAffection(int value) {
        entityData.set(AFFECTION, Math.clamp(value, 0, 100));
    }

    public void addAffection(int amount) {
        setAffection(getAffection() + amount);
    }

    public int getHunger() {
        return entityData.get(HUNGER);
    }

    public void setHunger(int value) {
        entityData.set(HUNGER, Math.clamp(value, 0, 100));
    }

    public void setPlayerCustomName(String name) {
        String clean = name == null ? "" : name.strip();
        if (clean.length() > 24) clean = clean.substring(0, 24);
        entityData.set(DISPLAY_NAME, clean);
        setCustomName(clean.isEmpty() ? null : Component.literal(clean));
    }

    public String getPlayerCustomName() {
        return entityData.get(DISPLAY_NAME);
    }

    public String getDisplayNameForChat() {
        String name = getPlayerCustomName();
        return name.isEmpty() ? "Girlfriend" : name;
    }

    public void setTextureVariant(String variant) {
        entityData.set(TEXTURE_VARIANT, variant != null ? variant : "default");
    }

    public String getTextureVariant() {
        return entityData.get(TEXTURE_VARIANT);
    }

    public String getSkinOwnerName() {
        return entityData.get(SKIN_OWNER_NAME);
    }

    public ResolvableProfile getSkinProfile() {
        return entityData.get(SKIN_PROFILE);
    }

    /** Sets a player skin from an already-resolved profile (see the /girlfriend skin command). */
    public void setSkinProfile(String name, ResolvableProfile profile) {
        entityData.set(SKIN_OWNER_NAME, name);
        entityData.set(SKIN_PROFILE, profile);
    }

    public void clearSkinProfile() {
        entityData.set(SKIN_OWNER_NAME, "");
        entityData.set(SKIN_PROFILE, ResolvableProfile.Static.EMPTY);
    }

    public int getEmoteType() {
        return entityData.get(EMOTE_TYPE);
    }

    public int getEmoteTicks() {
        return entityData.get(EMOTE_TICKS);
    }

    public void triggerEmote(int type, int durationTicks) {
        if (level().isClientSide()) return;
        entityData.set(EMOTE_TYPE, type);
        entityData.set(EMOTE_TICKS, durationTicks);
    }

    public boolean isFriendlyFire() {
        return entityData.get(FRIENDLY_FIRE);
    }

    public void setFriendlyFire(boolean enabled) {
        entityData.set(FRIENDLY_FIRE, enabled);
    }

    public boolean isFollowingOwner() {
        return entityData.get(FOLLOWING);
    }

    public void toggle() {
        boolean following = !isFollowingOwner();
        entityData.set(FOLLOWING, following);
        if (following && isSitting()) entityData.set(SITTING, false);
        say(following ? WaitAndFollowLines.pickFollowYou(this) : WaitAndFollowLines.pickWaitHere(this));
    }

    public boolean isSitting() {
        return entityData.get(SITTING);
    }

    public void toggleSit() {
        boolean sitting = !isSitting();
        entityData.set(SITTING, sitting);
        if (sitting) getNavigation().stop();
        say(sitting ? pick("I'll sit right here~", "Taking a little break!", "*sits and swings her legs*") : pick("I'm up! Let's go!", "Okay, adventure time!", "Ready when you are~"));
    }

    public void requestTeleport(double x, double y, double z) {
        if (level() instanceof ServerLevel) teleportTo(x, y, z);
    }

    private String pick(String... options) {
        return options[getRandom().nextInt(options.length)];
    }
}
