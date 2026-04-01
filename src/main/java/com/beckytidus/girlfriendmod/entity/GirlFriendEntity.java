package com.beckytidus.girlfriendmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import net.minecraft.world.scores.Team;
import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import com.beckytidus.girlfriendmod.dialogue.WaitAndFollowLines;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class GirlFriendEntity extends PathfinderMob {
    private static final int AGGRO_DECAY_TICKS = 6000;
    private static final int PHRASE_INTERVAL_TICKS = 600;
    private static final int GIFT_INTERVAL_TICKS = 1200;
    private static final int HEAL_INTERVAL_TICKS = 800;
    private static final int SENSE_INTERVAL_TICKS = 200;
    private static final int PARTICLE_INTERVAL_TICKS = 40;
    private static final double TELEPORT_DISTANCE = 32.0;
    private static final double SENSE_RANGE = 24.0;
    private static final double HUG_KISS_RANGE = 2.5;
    private static final int HUG_KISS_COOLDOWN_TICKS = 400;
    private static final int HUNGER_DECAY_INTERVAL = 1200;
    public static final int EMOTE_NONE = 0;
    public static final int EMOTE_CROUCH = 1;
    public static final int EMOTE_NOD = 2;
    public static final int EMOTE_HUG = 3;
    public static final int EMOTE_KISS = 4;
    public static final int EMOTE_DANCE = 5;
    public static final int EMOTE_WAVE = 6;
    public static final int EMOTE_HIGHFIVE = 7;

    private int relationshipLevel = 0;
    private int maxRelationshipLevel = 100;
    private int moodLevel = 50;
    private int maxMoodLevel = 100;
    private long lastPhraseTick = 0;
    private long lastGiftTick = 0;
    private long lastHealTick = 0;
    private long lastSenseTick = 0;
    private long lastParticleTick = 0;
    private long angeredAtTick = -1;
    private String playerCustomName = "";
    @Nullable
    private EntityReference<LivingEntity> ownerRef;
    private boolean isFollowing = true;
    private String textureVariant = "default";
    private String skinOwnerName = "";
    private static final EntityDataAccessor<String> SKIN_OWNER_NAME = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TEXTURE_VARIANT = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.STRING);
    private int affection = 50;
    private int maxAffection = 100;
    private int hunger = 80;
    private int maxHunger = 100;
    private long lastHungerDecayTick = 0;
    private int emoteTicks = 0;
    private int emoteType = 0;
    private int hugKissCooldown = 0;
    private boolean isSitting = false;
    private static final int INTERACTION_COOLDOWN_TICKS = 15;
    private long lastInteractionTick = 0;
    private boolean wasFarFromOwner = true;
    private int lastRelationshipLevel = 0;
    private int catchUpCooldownTicks = 0;
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AFFECTION = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMOTE_TYPE = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMOTE_TICKS = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RELATIONSHIP_LEVEL = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOOD_LEVEL = SynchedEntityData.defineId(GirlFriendEntity.class, EntityDataSerializers.INT);

    public GirlFriendEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Girlfriend"));
        this.setCanPickUpLoot(true);
    }

    private static void sendOwnerSystem(Player owner, Component message) {
        if (owner instanceof ServerPlayer sp) {
            sp.sendSystemMessage(message);
        }
    }

    public static AttributeSupplier.Builder createGirlfriendAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WanderToInterestGoal());
        this.goalSelector.addGoal(5, new WanderOffGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new FollowOwnerGoal());
        this.goalSelector.addGoal(8, new DefendOwnerGoal());

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_OWNER_NAME, "");
        builder.define(TEXTURE_VARIANT, "default");
        builder.define(SITTING, false);
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
        output.putInt("RelationshipLevel", relationshipLevel);
        output.putString("CustomName", playerCustomName);
        output.putBoolean("IsFollowing", isFollowing);
        output.putLong("LastPhraseTick", lastPhraseTick);
        output.putLong("LastGiftTick", lastGiftTick);
        output.putLong("LastHealTick", lastHealTick);
        output.putLong("AngeredAtTick", angeredAtTick);
        output.putString("TextureVariant", textureVariant);
        output.putBoolean("Sitting", isSitting);
        output.putInt("MoodLevel", moodLevel);
        output.putLong("LastSenseTick", lastSenseTick);
        output.putString("SkinOwnerName", skinOwnerName);
        output.putInt("Affection", affection);
        output.putInt("Hunger", hunger);
        output.putLong("LastHungerDecayTick", lastHungerDecayTick);
        output.putLong("LastInteractionTick", lastInteractionTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ownerRef = EntityReference.readWithOldOwnerConversion(input, "Owner", this.level());
        relationshipLevel = input.getIntOr("RelationshipLevel", 0);
        playerCustomName = input.getStringOr("CustomName", "");
        isFollowing = input.getBooleanOr("IsFollowing", true);
        lastPhraseTick = input.getLongOr("LastPhraseTick", 0);
        lastGiftTick = input.getLongOr("LastGiftTick", 0);
        lastHealTick = input.getLongOr("LastHealTick", 0);
        angeredAtTick = input.getLongOr("AngeredAtTick", -1);
        textureVariant = input.getStringOr("TextureVariant", "default");
        isSitting = input.getBooleanOr("Sitting", false);
        moodLevel = input.getIntOr("MoodLevel", 50);
        lastSenseTick = input.getLongOr("LastSenseTick", 0);
        skinOwnerName = input.getStringOr("SkinOwnerName", "");
        affection = input.getIntOr("Affection", 50);
        hunger = input.getIntOr("Hunger", 80);
        lastHungerDecayTick = input.getLongOr("LastHungerDecayTick", 0);
        lastInteractionTick = input.getLongOr("LastInteractionTick", 0);
        if (!level().isClientSide()) {
            getEntityData().set(SKIN_OWNER_NAME, skinOwnerName);
            getEntityData().set(TEXTURE_VARIANT, textureVariant);
            getEntityData().set(SITTING, isSitting);
            getEntityData().set(AFFECTION, affection);
            getEntityData().set(HUNGER, hunger);
            getEntityData().set(RELATIONSHIP_LEVEL, relationshipLevel);
            getEntityData().set(MOOD_LEVEL, moodLevel);
        }
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        if (level.getGameRules().get(GameRules.MOB_GRIEFING) && canPickUpLoot()) {
            if (stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)) return true;
            if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SPEARS)) return true;
            if (stack.is(Items.SHIELD) || stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT)) return true;
        }
        return stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)
            || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SPEARS)
            || stack.is(Items.SHIELD) || stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT);
    }

    @Override
    @Nullable
    public net.minecraft.tags.TagKey<net.minecraft.world.item.Item> getPreferredWeaponType() {
        return ItemTags.SWORDS;
    }


    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        super.onEquipItem(slot, oldStack, newStack);
        if (level().isClientSide()) return;
        Player owner = getOwner();
        if (owner != null && oldStack.isEmpty() && !newStack.isEmpty()) {
            String itemName = newStack.getHoverName().getString();
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": Look what I found! I'm using this " + itemName + " now."));
            setMoodLevel(Math.min(maxMoodLevel, moodLevel + 5));
            spawnHeartParticles();
        }
    }

    public void onChatResponse() {
        addRelationship(1);
        setMoodLevel(Math.min(maxMoodLevel, moodLevel + 3));
        addAffection(2);
        spawnHeartParticles();
    }

    public void spawnHeartParticles() {
        if (!(level() instanceof ServerLevel sw)) return;
        for (int i = 0; i < 4; i++) {
            double x = getX() + (level().getRandom().nextDouble() - 0.5) * getBbWidth();
            double y = getY() + getBbHeight() * 0.8 + level().getRandom().nextDouble() * 0.2;
            double z = getZ() + (level().getRandom().nextDouble() - 0.5) * getBbWidth();
            sw.sendParticles(ParticleTypes.HEART, x, y, z, 1, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private long getWorldTime() {
        return this.level() instanceof ServerLevel ? ((ServerLevel) this.level()).getGameTime() : 0;
    }

    public boolean isAngeredAtOwner() {
        LivingEntity target = getTarget();
        return target != null && getOwner() != null && target == getOwner();
    }

    private void tickAggroDecay() {
        LivingEntity target = getTarget();
        if (target == null) {
            angeredAtTick = -1;
            return;
        }
        if (angeredAtTick < 0) {
            angeredAtTick = getWorldTime();
        }
        long elapsed = getWorldTime() - angeredAtTick;
        if (elapsed >= AGGRO_DECAY_TICKS) {
            setTarget(null);
            angeredAtTick = -1;
        }
    }

    private class FollowOwnerGoal extends Goal {
        private static final double MIN_DISTANCE = 3.0;
        private static final double MAX_DISTANCE = 12.0;
        private static final int CATCH_UP_MIN_TICKS = 10;
        private static final int CATCH_UP_MAX_TICKS = 40;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowing || owner.isSpectator() || isSitting) return false;
            if (isAngeredAtOwner()) return false;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            Player owner = getOwner();
            if (owner == null) return;
            if (catchUpCooldownTicks > 0) {
                catchUpCooldownTicks--;
                return;
            }
            double distance = GirlFriendEntity.this.distanceToSqr(owner);
            if (distance > MAX_DISTANCE * MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().moveTo(owner, 1.2);
                GirlFriendEntity.this.setSprinting(true);
                catchUpCooldownTicks = GirlFriendEntity.this.level().getRandom().nextInt(CATCH_UP_MAX_TICKS - CATCH_UP_MIN_TICKS + 1) + CATCH_UP_MIN_TICKS;
            } else if (distance > MIN_DISTANCE * MIN_DISTANCE) {
                GirlFriendEntity.this.getNavigation().moveTo(owner, 1.0);
                GirlFriendEntity.this.setSprinting(false);
                catchUpCooldownTicks = GirlFriendEntity.this.level().getRandom().nextInt(CATCH_UP_MAX_TICKS - CATCH_UP_MIN_TICKS + 1) + CATCH_UP_MIN_TICKS;
            } else {
                GirlFriendEntity.this.getNavigation().stop();
                GirlFriendEntity.this.setSprinting(false);
            }
            GirlFriendEntity.this.getLookControl().setLookAt(owner, 10.0F, GirlFriendEntity.this.getMaxHeadXRot());
        }
    }

    private class DefendOwnerGoal extends Goal {
        private static final double DEFEND_RANGE = 15.0;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || owner.isSpectator() || getTarget() != null) return false;
            if (GirlFriendEntity.this.distanceToSqr(owner) > DEFEND_RANGE * DEFEND_RANGE) return false;
            for (LivingEntity entity : GirlFriendEntity.this.level().getEntitiesOfClass(LivingEntity.class, GirlFriendEntity.this.getBoundingBox().inflate(DEFEND_RANGE), e -> {
                if (e == GirlFriendEntity.this || e == owner) return false;
                if (e instanceof Mob mob && mob.getTarget() == owner) return true;
                return e instanceof Monster hostile && hostile.getTarget() == owner;
            })) {
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            Player owner = getOwner();
            if (owner == null) return;
            for (LivingEntity entity : GirlFriendEntity.this.level().getEntitiesOfClass(LivingEntity.class, GirlFriendEntity.this.getBoundingBox().inflate(DEFEND_RANGE), e -> {
                if (e == GirlFriendEntity.this || e == owner) return false;
                if (e instanceof Mob mob && mob.getTarget() == owner) return true;
                return e instanceof Monster hostile && hostile.getTarget() == owner;
            })) {
                GirlFriendEntity.this.setTarget(entity);
                sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": I'll protect you!"));
                break;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return getTarget() != null && getOwner() != null;
        }
    }

    private class WanderToInterestGoal extends Goal {
        private static final double RANGE = 10.0;
        private static final int INTEREST_CHANCE = 200;
        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowing || isSitting || isAngeredAtOwner() || getTarget() != null) return false;
            if (GirlFriendEntity.this.distanceToSqr(owner) > 20 * 20) return false;
            if (!GirlFriendEntity.this.getNavigation().isDone()) return false;
            return GirlFriendEntity.this.level().getRandom().nextInt(INTEREST_CHANCE) == 0 && findInterestTarget() != null;
        }

        @Override
        public void start() {
            BlockPos target = findInterestTarget();
            if (target != null) {
                GirlFriendEntity.this.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.6);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !GirlFriendEntity.this.getNavigation().isDone();
        }

        private BlockPos findInterestTarget() {
            Level level = GirlFriendEntity.this.level();
            BlockPos center = GirlFriendEntity.this.blockPosition();
            int r = (int) RANGE;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            BlockPos nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (int x = -r; x <= r; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -r; z <= r; z++) {
                        mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        BlockState state = level.getBlockState(mutable);
                        if (state.is(BlockTags.FLOWERS) || state.is(Blocks.BEEHIVE) || state.is(Blocks.BEE_NEST)) {
                            double d = GirlFriendEntity.this.distanceToSqr(mutable.getX() + 0.5, mutable.getY(), mutable.getZ() + 0.5);
                            if (d < nearestDist && d > 4) {
                                nearestDist = d;
                                nearest = new BlockPos(mutable.getX(), mutable.getY(), mutable.getZ());
                            }
                        }
                    }
                }
            }
            if (nearest != null) return nearest;
            var bees = level.getEntitiesOfClass(Bee.class, GirlFriendEntity.this.getBoundingBox().inflate(RANGE), e -> e.isAlive());
            if (!bees.isEmpty()) {
                Bee bee = bees.get(level.getRandom().nextInt(bees.size()));
                return bee.blockPosition();
            }
            return null;
        }
    }

    private class WanderOffGoal extends Goal {
        private static final int WANDER_CHANCE = 300;
        private static final double MIN_DIST = 4.0;
        private static final double MAX_DIST = 14.0;
        private static final int MAX_WANDER_TICKS = 200;
        private int wanderTicks = 0;

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !isFollowing || isSitting || isAngeredAtOwner() || getTarget() != null) return false;
            if (GirlFriendEntity.this.distanceToSqr(owner) < MIN_DIST * MIN_DIST) return false;
            return GirlFriendEntity.this.level().getRandom().nextInt(WANDER_CHANCE) == 0;
        }

        @Override
        public void start() {
            wanderTicks = 0;
            double x = GirlFriendEntity.this.getX();
            double y = GirlFriendEntity.this.getY();
            double z = GirlFriendEntity.this.getZ();
            double angle = GirlFriendEntity.this.level().getRandom().nextDouble() * Math.PI * 2;
            double dist = MIN_DIST + GirlFriendEntity.this.level().getRandom().nextDouble() * (MAX_DIST - MIN_DIST);
            double tx = x + Math.cos(angle) * dist;
            double tz = z + Math.sin(angle) * dist;
            BlockPos blockPos = BlockPos.containing(tx, y, tz);
            if (GirlFriendEntity.this.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.7)) {
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (wanderTicks++ > MAX_WANDER_TICKS) return false;
            if (getTarget() != null || isAngeredAtOwner()) return false;
            return !GirlFriendEntity.this.getNavigation().isDone();
        }
    }


    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            Player owner = getOwner();
            if (owner != null && isFollowing && !isSitting && !isAngeredAtOwner()) {
                double distSq = owner.distanceToSqr(this);
                if (distSq > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                    setPos(owner.getX(), owner.getY(), owner.getZ());
                }
            }
        }
        super.tick();
        if (this.level().isClientSide()) return;

        if (playerCustomName.isEmpty()) {
            setPlayerCustomName(FemaleNames.pickRandom(level().getRandom()));
        }
        if (skinOwnerName.isEmpty() && ("default".equals(textureVariant) || textureVariant.isEmpty())) {
            setTextureVariant(com.beckytidus.girlfriendmod.registry.GirlfriendSkins.pickRandomTextureVariant(level().getRandom()));
        }

        tickAggroDecay();

        Player owner = getOwner();
        if (owner != null) {
            setPersistenceRequired();
            long now = getWorldTime();

            if (emoteTicks > 0) {
                emoteTicks--;
                if (emoteTicks <= 0) emoteType = EMOTE_NONE;
                if (!level().isClientSide()) {
                    getEntityData().set(EMOTE_TICKS, emoteTicks);
                    if (emoteTicks <= 0) getEntityData().set(EMOTE_TYPE, EMOTE_NONE);
                }
            }
            if (hugKissCooldown > 0) hugKissCooldown--;
            if (now - lastHungerDecayTick >= HUNGER_DECAY_INTERVAL) {
                setHunger(Math.max(0, hunger - 2));
                lastHungerDecayTick = now;
            }
            if (!isAngeredAtOwner()) {
                double distToOwner = owner.distanceToSqr(GirlFriendEntity.this);
                if (wasFarFromOwner && distToOwner < 64.0 && emoteTicks <= 0 && getTarget() == null) {
                    wasFarFromOwner = false;
                    triggerEmote(EMOTE_WAVE, 30);
                    sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": Hey! Over here!"));
                }
                if (distToOwner > 100.0) wasFarFromOwner = true;
                if (distToOwner < HUG_KISS_RANGE * HUG_KISS_RANGE && hugKissCooldown <= 0 && getTarget() == null) {
                    if (this.level().getRandom().nextFloat() < 0.004f) {
                        hugKissCooldown = HUG_KISS_COOLDOWN_TICKS;
                        boolean doKiss = this.level().getRandom().nextBoolean();
                        triggerEmote(doKiss ? EMOTE_KISS : EMOTE_HUG, 40);
                        sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + (doKiss ? "*kisses you*" : "*hugs you tightly*")));
                        spawnHeartParticles();
                        addAffection(1);
                    }
                }
                if (emoteTicks <= 0 && this.level().getRandom().nextFloat() < 0.003f) {
                    float r = this.level().getRandom().nextFloat();
                    if (moodLevel >= 70 && affection >= 60 && r < 0.25f) triggerEmote(EMOTE_DANCE, 60);
                    else if (r < 0.5f) triggerEmote(this.level().getRandom().nextBoolean() ? EMOTE_NOD : EMOTE_CROUCH, 20);
                }
                int phraseInterval = Math.max(600, 900 - relationshipLevel * 2);
                if (now - lastPhraseTick >= phraseInterval && this.level().getRandom().nextFloat() < 0.05f) {
                    sayRandomPhrase();
                    lastPhraseTick = now;
                }
                int giftInterval = Math.max(1800, 2400 - relationshipLevel * 4);
                if (now - lastGiftTick >= giftInterval && this.level().getRandom().nextFloat() < 0.05f) {
                    if (this.level().getRandom().nextBoolean()) {
                        giveRandomGift();
                    } else {
                        sendOwnerSystem(owner, Component.literal("♥ Girlfriend thought of you."));
                    }
                    lastGiftTick = now;
                }
                if (this.getHealth() < this.getMaxHealth() * 0.7 && now - lastHealTick >= HEAL_INTERVAL_TICKS) {
                    this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
                    lastHealTick = now;
                }
                if (now - lastSenseTick >= SENSE_INTERVAL_TICKS) {
                    trySenseNearby(owner, now);
                    lastSenseTick = now;
                }
                if (now - lastParticleTick >= PARTICLE_INTERVAL_TICKS && moodLevel >= 60 && this.level().getRandom().nextFloat() < 0.2f) {
                    spawnHeartParticles();
                    lastParticleTick = now;
                }
            }
            if (getTarget() != null && level() instanceof ServerLevel sw && level().getRandom().nextFloat() < 0.08f) {
                sw.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + getBbHeight() * 0.5, getZ(), 1, 0.2, 0.2, 0.2, 0.0);
                lastParticleTick = now;
            }
            if (relationshipLevel > lastRelationshipLevel) {
                int prev = lastRelationshipLevel;
                lastRelationshipLevel = relationshipLevel;
                if (relationshipLevel >= 25 && prev < 25 || relationshipLevel >= 50 && prev < 50 || relationshipLevel >= 75 && prev < 75 || relationshipLevel >= 100 && prev < 100) {
                    String[] celebrations = {"We're getting closer!", "Our bond is growing stronger!", "I feel so connected to you!", "We make an amazing team!"};
                    if (relationshipLevel >= 100) celebrations = new String[]{"We're perfect together!", "I'll love you forever!", "You're my everything!", "Soulmates!"};
                    else if (relationshipLevel >= 75) celebrations = new String[]{"We're so close now!", "I trust you completely!", "You mean the world to me!", "Best partners ever!"};
                    else if (relationshipLevel >= 50) celebrations = new String[]{"We're really bonding!", "I'm so happy with you!", "You're amazing!", "Halfway to forever!"};
                    if (owner != null) {
                        String msg = celebrations[level().getRandom().nextInt(celebrations.length)];
                        sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + msg));
                        for (int i = 0; i < 8; i++) spawnHeartParticles();
                    }
                }
            }
            if (isSitting) {
                getNavigation().stop();
                setSprinting(false);
            }
            updateNameTag();
        }
    }

    private void trySenseNearby(Player owner, long now) {
        if (!(level() instanceof ServerLevel sw)) return;
        var hostiles = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(SENSE_RANGE), LivingEntity::isAlive);
        if (!hostiles.isEmpty() && level().getRandom().nextFloat() < 0.08f) {
            String[] sense = {
                "I sense something hostile nearby!",
                "There's danger in the area...",
                "Enemies are close! Stay alert!",
                "I feel a threat nearby!",
                "Something dangerous is approaching!"
            };
            String msg = sense[level().getRandom().nextInt(sense.length)];
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + msg));
            spawnHeartParticles();
        }
        var players = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(SENSE_RANGE), p -> p != owner && p.isAlive());
        if (!players.isEmpty() && level().getRandom().nextFloat() < 0.04f) {
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": I sense another player nearby."));
        }
    }

    private void updateNameTag() {
        String displayName = "Girlfriend";
        if (!playerCustomName.isEmpty()) {
            displayName = playerCustomName;
        }
        this.setCustomName(Component.literal(displayName));
    }

    public boolean canInteract(Player player) {
        if (level().isClientSide()) return true;
        long now = level() instanceof ServerLevel sw ? sw.getGameTime() : 0;
        return now - lastInteractionTick >= INTERACTION_COOLDOWN_TICKS;
    }

    public void setLastInteractionTick(long tick) {
        this.lastInteractionTick = tick;
    }

    public Component getStatsDisplayText() {
        return Component.literal("Lv." + relationshipLevel + "  ♥" + getMoodLevel() + "  Aff:" + getAffection() + "  Hunger:" + getHunger() + "  HP:" + (int) getHealth() + "/" + (int) getMaxHealth());
    }


    private void sayRandomPhrase() {
        String[] phrases = {
            "I love you so much!", "You're amazing!", "I'm always here for you",
            "You make me so happy", "Thank you for everything", "Let's fight together!",
            "I've got your back", "You're my everything", "This time with you is wonderful",
            "I'll never leave you", "Your smile brightens my day", "I'm so proud of you",
            "Let me help you", "We make a great team!", "I missed you!",
            "You're my hero!", "I trust you completely", "Let's adventure together!",
            "You make my heart skip a beat", "I think about you all the time",
            "I'd do anything for you", "You're the best!", "I'm lucky to have you",
            "You're so strong and brave", "I admire your determination",
            "Every moment with you is precious", "You brighten my world",
            "I love your laugh", "You inspire me daily", "Together we're unstoppable!"
        };
        Player owner = getOwner();
        if (owner != null) {
            String phrase = phrases[this.level().getRandom().nextInt(phrases.length)];
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + phrase));
            addRelationship(2 + this.level().getRandom().nextInt(2));
        }
    }

    private void giveRandomGift() {
        Player owner = getOwner();
        if (owner != null) {
            String[] gifts = {
                "gives you a gift!", "found something for you!",
                "made something special for you!", "wants you to have this!",
                "made this just for you!", "thought of you!",
                "picked this out for you!", "brought you something!"
            };
            String giftPhrase = gifts[this.level().getRandom().nextInt(gifts.length)];
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + " " + giftPhrase));
            addRelationship(3);
            ItemStack gift = getRandomGiftItem();
            if (!owner.getInventory().add(gift)) {
                owner.drop(gift, false);
            }
        }
    }

    private ItemStack getRandomGiftItem() {
        ItemStack[] possibleGifts = {
            new ItemStack(Items.AMETHYST_SHARD),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.POPPY),
            new ItemStack(Items.APPLE),
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.COOKED_BEEF)
        };
        return possibleGifts[this.level().getRandom().nextInt(possibleGifts.length)].copy();
    }

    public Player getOwner() {
        return EntityReference.getLivingEntity(ownerRef, level()) instanceof Player p ? p : null;
    }

    public void setOwner(Player player) {
        this.ownerRef = player != null ? EntityReference.of((LivingEntity) player) : null;
        if (player != null) {
            setPersistenceRequired();
        }
    }

    public void feedEntity(ItemStack stack) {
        if (stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE)) {
            this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
            addRelationship(3 + this.level().getRandom().nextInt(2));
            setHunger(Math.min(maxHunger, hunger + 15));
            addAffection(4);
            Player o = getOwner();
            if (o != null) {
                sendOwnerSystem(o, Component.literal("♥ " + getDisplayNameForChat() + ": Thank you for the food!"));
            }
        } else if (stack.is(Items.WHEAT) || stack.is(Items.BREAD)) {
            this.setHealth(Math.min(this.getHealth() + 2.0f, this.getMaxHealth()));
            addRelationship(3);
            setHunger(Math.min(maxHunger, hunger + 8));
            addAffection(2);
        } else if (stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BAKED_POTATO)) {
            this.setHealth(Math.min(this.getHealth() + 3.0f, this.getMaxHealth()));
            addRelationship(3 + this.level().getRandom().nextInt(2));
            setHunger(Math.min(maxHunger, hunger + 10));
            addAffection(3);
        } else if (stack.is(Items.PUMPKIN_PIE) || stack.is(Items.CAKE)) {
            this.setHealth(Math.min(this.getHealth() + 6.0f, this.getMaxHealth()));
            addRelationship(4);
            setHunger(maxHunger);
            setMoodLevel(Math.min(maxMoodLevel, moodLevel + 8));
            addAffection(5);
            Player o = getOwner();
            if (o != null) {
                sendOwnerSystem(o, Component.literal("♥ " + getDisplayNameForChat() + ": Mmm, delicious!"));
                spawnHeartParticles();
            }
        }
    }

    public int getRelationshipLevel() {
        return level() != null && level().isClientSide() ? getEntityData().get(RELATIONSHIP_LEVEL) : relationshipLevel;
    }

    public void setRelationshipLevel(int value) {
        this.relationshipLevel = Math.min(Math.max(0, value), maxRelationshipLevel);
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(RELATIONSHIP_LEVEL, relationshipLevel);
        }
    }

    public int getMoodLevel() {
        return level() != null && level().isClientSide() ? getEntityData().get(MOOD_LEVEL) : moodLevel;
    }

    public void setMoodLevel(int value) {
        this.moodLevel = Math.min(Math.max(0, value), maxMoodLevel);
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(MOOD_LEVEL, moodLevel);
        }
    }

    public void addRelationship(int amount) {
        setRelationshipLevel(relationshipLevel + amount);
    }

    public void setPlayerCustomName(String name) {
        this.playerCustomName = name != null ? name : "";
    }

    public String getPlayerCustomName() {
        return playerCustomName;
    }

    public String getDisplayNameForChat() {
        return playerCustomName != null && !playerCustomName.isEmpty() ? playerCustomName : "Girlfriend";
    }

    public void setTextureVariant(String variant) {
        this.textureVariant = variant != null ? variant : "default";
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(TEXTURE_VARIANT, this.textureVariant);
        }
    }

    public String getTextureVariant() {
        return level() != null && level().isClientSide() ? getEntityData().get(TEXTURE_VARIANT) : textureVariant;
    }

    public String getSkinOwnerName() {
        return level() != null && level().isClientSide() ? getEntityData().get(SKIN_OWNER_NAME) : skinOwnerName;
    }

    public void setSkinOwnerName(String name) {
        this.skinOwnerName = name != null ? name : "";
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(SKIN_OWNER_NAME, this.skinOwnerName);
        }
    }

    public int getAffection() {
        return level() != null && level().isClientSide() ? getEntityData().get(AFFECTION) : affection;
    }

    public void setAffection(int value) {
        this.affection = Math.min(Math.max(0, value), maxAffection);
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(AFFECTION, this.affection);
        }
    }

    public void addAffection(int amount) {
        setAffection(affection + amount);
    }

    public int getHunger() {
        return level() != null && level().isClientSide() ? getEntityData().get(HUNGER) : hunger;
    }

    public void setHunger(int value) {
        this.hunger = Math.min(Math.max(0, value), maxHunger);
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(HUNGER, this.hunger);
        }
    }

    public int getEmoteType() {
        return level() != null && level().isClientSide() ? getEntityData().get(EMOTE_TYPE) : emoteType;
    }

    public int getEmoteTicks() {
        return level() != null && level().isClientSide() ? getEntityData().get(EMOTE_TICKS) : emoteTicks;
    }

    public void triggerEmote(int type, int durationTicks) {
        this.emoteType = type;
        this.emoteTicks = durationTicks;
        if (level() != null && !level().isClientSide()) {
            getEntityData().set(EMOTE_TYPE, emoteType);
            getEntityData().set(EMOTE_TICKS, emoteTicks);
        }
    }

    public void toggle() {
        this.isFollowing = !this.isFollowing;
        Player owner = getOwner();
        if (owner != null) {
            String msg = this.isFollowing ? WaitAndFollowLines.pickFollowYou(this) : WaitAndFollowLines.pickWaitHere(this);
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + msg));
        }
    }

    public boolean isFollowingOwner() {
        return this.isFollowing;
    }

    public void setSitting(boolean sitting) {
        this.isSitting = sitting;
        if (level() != null && !level().isClientSide()) getEntityData().set(SITTING, isSitting);
    }

    public boolean isSitting() {
        return level() != null && level().isClientSide() ? getEntityData().get(SITTING) : isSitting;
    }

    public void toggleSit() {
        setSitting(!isSitting);
        Player owner = getOwner();
        if (owner != null) {
            sendOwnerSystem(owner, Component.literal("♥ " + getDisplayNameForChat() + ": " + (isSitting ? "I'll sit here for a bit." : "I'm up! Let's go!")));
        }
    }

    public int getLastRelationshipLevel() {
        return lastRelationshipLevel;
    }

    public void requestTeleport(double x, double y, double z) {
        if (level() instanceof ServerLevel) {
            teleportTo(x, y, z);
        }
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Player owner = getOwner();
        if (owner != null && source.getEntity() instanceof Player attacker) {
            if (attacker == owner) {
                addRelationship(-10);
                setTarget(owner);
                angeredAtTick = getWorldTime();
                if (owner instanceof ServerPlayer spe) {
                    DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOwner(this), false);
                }
            } else {
                addRelationship(-5);
                if (owner instanceof ServerPlayer spe) {
                    DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOther(this), false);
                }
            }
        } else if (source.getEntity() instanceof LivingEntity attacker && owner != null) {
            setTarget(attacker);
            angeredAtTick = getWorldTime();
            if (owner instanceof ServerPlayer spe) {
                DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOther(this), false);
            }
        }

        boolean result = super.hurtServer(level, source, amount);

        if (result) {
            setMoodLevel(Math.max(0, moodLevel - 5));
        }
        if (this.getHealth() <= 0 && owner != null) {
            sendOwnerSystem(owner, Component.literal("No! " + getDisplayNameForChat() + " has fallen..."));
        }

        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return getOwner() == null && !isPersistenceRequired();
    }
}
