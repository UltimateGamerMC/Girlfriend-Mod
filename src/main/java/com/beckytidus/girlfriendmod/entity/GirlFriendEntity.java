package com.beckytidus.girlfriendmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jetbrains.annotations.Nullable;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import net.minecraft.scoreboard.Team;
import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import com.beckytidus.girlfriendmod.dialogue.WaitAndFollowLines;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;
import java.util.UUID;

public class GirlFriendEntity extends PathAwareEntity {
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
    private LazyEntityReference<LivingEntity> ownerRef;
    private boolean isFollowing = true;
    private String textureVariant = "default";
    private String skinOwnerName = "";
    private static final TrackedData<String> SKIN_OWNER_NAME = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> TEXTURE_VARIANT = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.STRING);
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
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> AFFECTION = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> HUNGER = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> EMOTE_TYPE = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> EMOTE_TICKS = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> RELATIONSHIP_LEVEL = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MOOD_LEVEL = DataTracker.registerData(GirlFriendEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public GirlFriendEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("Girlfriend"));
        this.setCanPickUpLoot(true);
    }

    public static DefaultAttributeContainer.Builder createGirlfriendAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(3, new LookAroundGoal(this));
        this.goalSelector.add(4, new WanderToInterestGoal());
        this.goalSelector.add(5, new WanderOffGoal());
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(7, new FollowOwnerGoal());
        this.goalSelector.add(8, new DefendOwnerGoal());

        this.targetSelector.add(1, new GirlfriendRevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SKIN_OWNER_NAME, "");
        builder.add(TEXTURE_VARIANT, "default");
        builder.add(SITTING, false);
        builder.add(AFFECTION, 50);
        builder.add(HUNGER, 80);
        builder.add(EMOTE_TYPE, 0);
        builder.add(EMOTE_TICKS, 0);
        builder.add(RELATIONSHIP_LEVEL, 0);
        builder.add(MOOD_LEVEL, 50);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (ownerRef != null) {
            view.put("Owner", Uuids.INT_STREAM_CODEC, ownerRef.getUuid());
        }
        view.putInt("RelationshipLevel", relationshipLevel);
        view.putString("CustomName", playerCustomName);
        view.putBoolean("IsFollowing", isFollowing);
        view.putLong("LastPhraseTick", lastPhraseTick);
        view.putLong("LastGiftTick", lastGiftTick);
        view.putLong("LastHealTick", lastHealTick);
        view.putLong("AngeredAtTick", angeredAtTick);
        view.putString("TextureVariant", textureVariant);
        view.putBoolean("Sitting", isSitting);
        view.putInt("MoodLevel", moodLevel);
        view.putLong("LastSenseTick", lastSenseTick);
        view.putString("SkinOwnerName", skinOwnerName);
        view.putInt("Affection", affection);
        view.putInt("Hunger", hunger);
        view.putLong("LastHungerDecayTick", lastHungerDecayTick);
        view.putLong("LastInteractionTick", lastInteractionTick);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Optional<UUID> ownerUuid = view.read("Owner", Uuids.INT_STREAM_CODEC);
        if (ownerUuid.isPresent()) {
            ownerRef = LazyEntityReference.ofUUID(ownerUuid.get());
        }
        relationshipLevel = view.getInt("RelationshipLevel", 0);
        playerCustomName = view.getString("CustomName", "");
        isFollowing = view.getBoolean("IsFollowing", true);
        lastPhraseTick = view.getLong("LastPhraseTick", 0);
        lastGiftTick = view.getLong("LastGiftTick", 0);
        lastHealTick = view.getLong("LastHealTick", 0);
        angeredAtTick = view.getLong("AngeredAtTick", -1);
        textureVariant = view.getString("TextureVariant", "default");
        isSitting = view.getBoolean("Sitting", false);
        moodLevel = view.getInt("MoodLevel", 50);
        lastSenseTick = view.getLong("LastSenseTick", 0);
        skinOwnerName = view.getString("SkinOwnerName", "");
        affection = view.getInt("Affection", 50);
        hunger = view.getInt("Hunger", 80);
        lastHungerDecayTick = view.getLong("LastHungerDecayTick", 0);
        lastInteractionTick = view.getLong("LastInteractionTick", 0);
        if (!getEntityWorld().isClient()) {
            getDataTracker().set(SKIN_OWNER_NAME, skinOwnerName);
            getDataTracker().set(TEXTURE_VARIANT, textureVariant);
            getDataTracker().set(SITTING, isSitting);
            getDataTracker().set(AFFECTION, affection);
            getDataTracker().set(HUNGER, hunger);
            getDataTracker().set(RELATIONSHIP_LEVEL, relationshipLevel);
            getDataTracker().set(MOOD_LEVEL, moodLevel);
        }
    }

    @Override
    public boolean canGather(net.minecraft.server.world.ServerWorld world, ItemStack stack) {
        if (world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING) != Boolean.FALSE && canPickUpLoot()) {
            if (stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR) || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR)) return true;
            if (stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES) || stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SPEARS)) return true;
            if (stack.isOf(Items.SHIELD) || stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW) || stack.isOf(Items.TRIDENT)) return true;
        }
        return false;
    }

    @Override
    @Nullable
    public net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> getPreferredWeapons() {
        return ItemTags.SWORDS;
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        if (stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR) || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR)) return true;
        if (stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES) || stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SPEARS)) return true;
        return stack.isOf(Items.SHIELD) || stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW) || stack.isOf(Items.TRIDENT);
    }

    @Override
    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        super.onEquipStack(slot, oldStack, newStack);
        if (getEntityWorld().isClient()) return;
        PlayerEntity owner = getOwner();
        if (owner != null && oldStack.isEmpty() && !newStack.isEmpty()) {
            String itemName = newStack.getName().getString();
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": Look what I found! I'm using this " + itemName + " now."), false);
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
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        for (int i = 0; i < 4; i++) {
            double x = getX() + (getEntityWorld().getRandom().nextDouble() - 0.5) * getWidth();
            double y = getY() + getHeight() * 0.8 + getEntityWorld().getRandom().nextDouble() * 0.2;
            double z = getZ() + (getEntityWorld().getRandom().nextDouble() - 0.5) * getWidth();
            sw.spawnParticles(ParticleTypes.HEART, x, y, z, 1, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private long getWorldTime() {
        return this.getEntityWorld() instanceof ServerWorld ? ((ServerWorld) this.getEntityWorld()).getLevelProperties().getTime() : 0;
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
        public boolean canStart() {
            PlayerEntity owner = getOwner();
            if (owner == null || !isFollowing || owner.isSpectator() || isSitting) return false;
            if (isAngeredAtOwner()) return false;
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            PlayerEntity owner = getOwner();
            if (owner == null) return;
            if (catchUpCooldownTicks > 0) {
                catchUpCooldownTicks--;
                return;
            }
            double distance = GirlFriendEntity.this.squaredDistanceTo(owner);
            if (distance > MAX_DISTANCE * MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.2);
                GirlFriendEntity.this.setSprinting(true);
                catchUpCooldownTicks = GirlFriendEntity.this.getEntityWorld().getRandom().nextBetween(CATCH_UP_MIN_TICKS, CATCH_UP_MAX_TICKS);
            } else if (distance > MIN_DISTANCE * MIN_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.0);
                GirlFriendEntity.this.setSprinting(false);
                catchUpCooldownTicks = GirlFriendEntity.this.getEntityWorld().getRandom().nextBetween(CATCH_UP_MIN_TICKS, CATCH_UP_MAX_TICKS);
            } else {
                GirlFriendEntity.this.getNavigation().stop();
                GirlFriendEntity.this.setSprinting(false);
            }
            GirlFriendEntity.this.getLookControl().lookAt(owner, 10.0F, GirlFriendEntity.this.getMaxHeadRotation());
        }
    }

    private class DefendOwnerGoal extends Goal {
        private static final double DEFEND_RANGE = 15.0;

        @Override
        public boolean canStart() {
            PlayerEntity owner = getOwner();
            if (owner == null || owner.isSpectator() || getTarget() != null) return false;
            if (GirlFriendEntity.this.squaredDistanceTo(owner) > DEFEND_RANGE * DEFEND_RANGE) return false;
            for (LivingEntity entity : GirlFriendEntity.this.getEntityWorld().getEntitiesByClass(LivingEntity.class, GirlFriendEntity.this.getBoundingBox().expand(DEFEND_RANGE), e -> {
                if (e == GirlFriendEntity.this || e == owner) return false;
                if (e instanceof MobEntity mob && mob.getTarget() == owner) return true;
                return e instanceof HostileEntity hostile && hostile.getTarget() == owner;
            })) {
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            PlayerEntity owner = getOwner();
            if (owner == null) return;
            for (LivingEntity entity : GirlFriendEntity.this.getEntityWorld().getEntitiesByClass(LivingEntity.class, GirlFriendEntity.this.getBoundingBox().expand(DEFEND_RANGE), e -> {
                if (e == GirlFriendEntity.this || e == owner) return false;
                if (e instanceof MobEntity mob && mob.getTarget() == owner) return true;
                return e instanceof HostileEntity hostile && hostile.getTarget() == owner;
            })) {
                GirlFriendEntity.this.setTarget(entity);
                owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": I'll protect you!"), false);
                break;
            }
        }

        @Override
        public boolean shouldContinue() {
            return getTarget() != null && getOwner() != null;
        }
    }

    private class WanderToInterestGoal extends Goal {
        private static final double RANGE = 10.0;
        private static final int INTEREST_CHANCE = 200;
        @Override
        public boolean canStart() {
            PlayerEntity owner = getOwner();
            if (owner == null || !isFollowing || isSitting || isAngeredAtOwner() || getTarget() != null) return false;
            if (GirlFriendEntity.this.squaredDistanceTo(owner) > 20 * 20) return false;
            if (GirlFriendEntity.this.getNavigation().isFollowingPath()) return false;
            return GirlFriendEntity.this.getEntityWorld().getRandom().nextInt(INTEREST_CHANCE) == 0 && findInterestTarget() != null;
        }

        @Override
        public void start() {
            BlockPos target = findInterestTarget();
            if (target != null) {
                GirlFriendEntity.this.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.6);
            }
        }

        @Override
        public boolean shouldContinue() {
            return GirlFriendEntity.this.getNavigation().isFollowingPath() && !GirlFriendEntity.this.getNavigation().isIdle();
        }

        private BlockPos findInterestTarget() {
            World world = GirlFriendEntity.this.getEntityWorld();
            BlockPos center = GirlFriendEntity.this.getBlockPos();
            int r = (int) RANGE;
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            BlockPos nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (int x = -r; x <= r; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -r; z <= r; z++) {
                        mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        BlockState state = world.getBlockState(mutable);
                        if (state.isIn(BlockTags.FLOWERS) || state.isOf(Blocks.BEEHIVE) || state.isOf(Blocks.BEE_NEST)) {
                            double d = GirlFriendEntity.this.squaredDistanceTo(mutable.getX() + 0.5, mutable.getY(), mutable.getZ() + 0.5);
                            if (d < nearestDist && d > 4) {
                                nearestDist = d;
                                nearest = new BlockPos(mutable.getX(), mutable.getY(), mutable.getZ());
                            }
                        }
                    }
                }
            }
            if (nearest != null) return nearest;
            var bees = world.getEntitiesByClass(BeeEntity.class, GirlFriendEntity.this.getBoundingBox().expand(RANGE), e -> e.isAlive());
            if (!bees.isEmpty()) {
                BeeEntity bee = bees.get(world.getRandom().nextInt(bees.size()));
                return bee.getBlockPos();
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
        public boolean canStart() {
            PlayerEntity owner = getOwner();
            if (owner == null || !isFollowing || isSitting || isAngeredAtOwner() || getTarget() != null) return false;
            if (GirlFriendEntity.this.squaredDistanceTo(owner) < MIN_DIST * MIN_DIST) return false;
            return GirlFriendEntity.this.getEntityWorld().getRandom().nextInt(WANDER_CHANCE) == 0;
        }

        @Override
        public void start() {
            wanderTicks = 0;
            double x = GirlFriendEntity.this.getX();
            double y = GirlFriendEntity.this.getY();
            double z = GirlFriendEntity.this.getZ();
            double angle = GirlFriendEntity.this.getEntityWorld().getRandom().nextDouble() * Math.PI * 2;
            double dist = MIN_DIST + GirlFriendEntity.this.getEntityWorld().getRandom().nextDouble() * (MAX_DIST - MIN_DIST);
            double tx = x + Math.cos(angle) * dist;
            double tz = z + Math.sin(angle) * dist;
            BlockPos blockPos = BlockPos.ofFloored(tx, y, tz);
            if (GirlFriendEntity.this.getNavigation().startMovingTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.7)) {
            }
        }

        @Override
        public boolean shouldContinue() {
            if (wanderTicks++ > MAX_WANDER_TICKS) return false;
            if (getTarget() != null || isAngeredAtOwner()) return false;
            return GirlFriendEntity.this.getNavigation().isFollowingPath();
        }
    }

    private static class GirlfriendRevengeGoal extends RevengeGoal {
        public GirlfriendRevengeGoal(PathAwareEntity mob) {
            super(mob);
        }

        @Override
        public boolean shouldContinue() {
            GirlFriendEntity gf = (GirlFriendEntity) this.mob;
            if (gf.getTarget() == null) return false;
            return super.shouldContinue();
        }
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient()) {
            PlayerEntity owner = getOwner();
            if (owner != null && isFollowing && !isSitting && !isAngeredAtOwner()) {
                double distSq = owner.squaredDistanceTo(this);
                if (distSq > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                    setPosition(owner.getX(), owner.getY(), owner.getZ());
                }
            }
        }
        super.tick();
        if (this.getEntityWorld().isClient()) return;

        if (playerCustomName.isEmpty()) {
            setPlayerCustomName(FemaleNames.pickRandom(getEntityWorld().getRandom()));
        }
        if (skinOwnerName.isEmpty() && ("default".equals(textureVariant) || textureVariant.isEmpty())) {
            setTextureVariant(com.beckytidus.girlfriendmod.registry.GirlfriendSkins.pickRandomTextureVariant(getEntityWorld().getRandom()));
        }

        tickAggroDecay();

        PlayerEntity owner = getOwner();
        if (owner != null) {
            setPersistent();
            long now = getWorldTime();

            if (emoteTicks > 0) {
                emoteTicks--;
                if (emoteTicks <= 0) emoteType = EMOTE_NONE;
                if (!getEntityWorld().isClient()) {
                    getDataTracker().set(EMOTE_TICKS, emoteTicks);
                    if (emoteTicks <= 0) getDataTracker().set(EMOTE_TYPE, EMOTE_NONE);
                }
            }
            if (hugKissCooldown > 0) hugKissCooldown--;
            if (now - lastHungerDecayTick >= HUNGER_DECAY_INTERVAL) {
                setHunger(Math.max(0, hunger - 2));
                lastHungerDecayTick = now;
            }
            if (!isAngeredAtOwner()) {
                double distToOwner = owner.squaredDistanceTo(GirlFriendEntity.this);
                if (wasFarFromOwner && distToOwner < 64.0 && emoteTicks <= 0 && getTarget() == null) {
                    wasFarFromOwner = false;
                    triggerEmote(EMOTE_WAVE, 30);
                    owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": Hey! Over here!"), false);
                }
                if (distToOwner > 100.0) wasFarFromOwner = true;
                if (distToOwner < HUG_KISS_RANGE * HUG_KISS_RANGE && hugKissCooldown <= 0 && getTarget() == null) {
                    if (this.getEntityWorld().getRandom().nextFloat() < 0.004f) {
                        hugKissCooldown = HUG_KISS_COOLDOWN_TICKS;
                        boolean doKiss = this.getEntityWorld().getRandom().nextBoolean();
                        triggerEmote(doKiss ? EMOTE_KISS : EMOTE_HUG, 40);
                        owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + (doKiss ? "*kisses you*" : "*hugs you tightly*")), false);
                        spawnHeartParticles();
                        addAffection(1);
                    }
                }
                if (emoteTicks <= 0 && this.getEntityWorld().getRandom().nextFloat() < 0.003f) {
                    float r = this.getEntityWorld().getRandom().nextFloat();
                    if (moodLevel >= 70 && affection >= 60 && r < 0.25f) triggerEmote(EMOTE_DANCE, 60);
                    else if (r < 0.5f) triggerEmote(this.getEntityWorld().getRandom().nextBoolean() ? EMOTE_NOD : EMOTE_CROUCH, 20);
                }
                int phraseInterval = Math.max(600, 900 - relationshipLevel * 2);
                if (now - lastPhraseTick >= phraseInterval && this.getEntityWorld().getRandom().nextFloat() < 0.05f) {
                    sayRandomPhrase();
                    lastPhraseTick = now;
                }
                int giftInterval = Math.max(1800, 2400 - relationshipLevel * 4);
                if (now - lastGiftTick >= giftInterval && this.getEntityWorld().getRandom().nextFloat() < 0.05f) {
                    if (this.getEntityWorld().getRandom().nextBoolean()) {
                        giveRandomGift();
                    } else {
                        owner.sendMessage(Text.literal("♥ Girlfriend thought of you."), false);
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
                if (now - lastParticleTick >= PARTICLE_INTERVAL_TICKS && moodLevel >= 60 && this.getEntityWorld().getRandom().nextFloat() < 0.2f) {
                    spawnHeartParticles();
                    lastParticleTick = now;
                }
            }
            if (getTarget() != null && getEntityWorld() instanceof ServerWorld sw && getEntityWorld().getRandom().nextFloat() < 0.08f) {
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + getHeight() * 0.5, getZ(), 1, 0.2, 0.2, 0.2, 0.0);
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
                        String msg = celebrations[getEntityWorld().getRandom().nextInt(celebrations.length)];
                        owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + msg), false);
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

    private void trySenseNearby(PlayerEntity owner, long now) {
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        var hostiles = getEntityWorld().getEntitiesByClass(HostileEntity.class, getBoundingBox().expand(SENSE_RANGE), LivingEntity::isAlive);
        if (!hostiles.isEmpty() && getEntityWorld().getRandom().nextFloat() < 0.08f) {
            String[] sense = {
                "I sense something hostile nearby!",
                "There's danger in the area...",
                "Enemies are close! Stay alert!",
                "I feel a threat nearby!",
                "Something dangerous is approaching!"
            };
            String msg = sense[getEntityWorld().getRandom().nextInt(sense.length)];
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + msg), false);
            spawnHeartParticles();
        }
        var players = getEntityWorld().getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(SENSE_RANGE), p -> p != owner && p.isAlive());
        if (!players.isEmpty() && getEntityWorld().getRandom().nextFloat() < 0.04f) {
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": I sense another player nearby."), false);
        }
    }

    private void updateNameTag() {
        String displayName = "Girlfriend";
        if (!playerCustomName.isEmpty()) {
            displayName = playerCustomName;
        }
        this.setCustomName(Text.literal(displayName));
    }

    public boolean canInteract(PlayerEntity player) {
        if (getEntityWorld().isClient()) return true;
        long now = getEntityWorld() instanceof ServerWorld sw ? sw.getLevelProperties().getTime() : 0;
        return now - lastInteractionTick >= INTERACTION_COOLDOWN_TICKS;
    }

    public void setLastInteractionTick(long tick) {
        this.lastInteractionTick = tick;
    }

    public Text getStatsDisplayText() {
        return Text.literal("Lv." + relationshipLevel + "  ♥" + getMoodLevel() + "  Aff:" + getAffection() + "  Hunger:" + getHunger() + "  HP:" + (int) getHealth() + "/" + (int) getMaxHealth());
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
        PlayerEntity owner = getOwner();
        if (owner != null) {
            String phrase = phrases[this.getEntityWorld().getRandom().nextInt(phrases.length)];
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + phrase), false);
            addRelationship(2 + this.getEntityWorld().getRandom().nextInt(2));
        }
    }

    private void giveRandomGift() {
        PlayerEntity owner = getOwner();
        if (owner != null) {
            String[] gifts = {
                "gives you a gift!", "found something for you!",
                "made something special for you!", "wants you to have this!",
                "made this just for you!", "thought of you!",
                "picked this out for you!", "brought you something!"
            };
            String giftPhrase = gifts[this.getEntityWorld().getRandom().nextInt(gifts.length)];
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + " " + giftPhrase), false);
            addRelationship(3);
            ItemStack gift = getRandomGiftItem();
            if (!owner.getInventory().insertStack(gift)) {
                owner.dropItem(gift, false);
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
        return possibleGifts[this.getEntityWorld().getRandom().nextInt(possibleGifts.length)].copy();
    }

    public PlayerEntity getOwner() {
        if (ownerRef == null) return null;
        LivingEntity e = ownerRef.getEntityByClass(this.getEntityWorld(), LivingEntity.class);
        return e instanceof PlayerEntity pe ? pe : null;
    }

    public void setOwner(PlayerEntity player) {
        this.ownerRef = player != null ? LazyEntityReference.of((LivingEntity) player) : null;
        if (player != null) {
            setPersistent();
        }
    }

    public void feedEntity(ItemStack stack) {
        if (stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE)) {
            this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
            addRelationship(3 + this.getEntityWorld().getRandom().nextInt(2));
            setHunger(Math.min(maxHunger, hunger + 15));
            addAffection(4);
            if (getOwner() != null) {
                getOwner().sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": Thank you for the food!"), false);
            }
        } else if (stack.isOf(Items.WHEAT) || stack.isOf(Items.BREAD)) {
            this.setHealth(Math.min(this.getHealth() + 2.0f, this.getMaxHealth()));
            addRelationship(3);
            setHunger(Math.min(maxHunger, hunger + 8));
            addAffection(2);
        } else if (stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BAKED_POTATO)) {
            this.setHealth(Math.min(this.getHealth() + 3.0f, this.getMaxHealth()));
            addRelationship(3 + this.getEntityWorld().getRandom().nextInt(2));
            setHunger(Math.min(maxHunger, hunger + 10));
            addAffection(3);
        } else if (stack.isOf(Items.PUMPKIN_PIE) || stack.isOf(Items.CAKE)) {
            this.setHealth(Math.min(this.getHealth() + 6.0f, this.getMaxHealth()));
            addRelationship(4);
            setHunger(maxHunger);
            setMoodLevel(Math.min(maxMoodLevel, moodLevel + 8));
            addAffection(5);
            if (getOwner() != null) {
                getOwner().sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": Mmm, delicious!"), false);
                spawnHeartParticles();
            }
        }
    }

    public int getRelationshipLevel() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(RELATIONSHIP_LEVEL) : relationshipLevel;
    }

    public void setRelationshipLevel(int level) {
        this.relationshipLevel = Math.min(Math.max(0, level), maxRelationshipLevel);
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(RELATIONSHIP_LEVEL, relationshipLevel);
        }
    }

    public int getMoodLevel() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(MOOD_LEVEL) : moodLevel;
    }

    public void setMoodLevel(int level) {
        this.moodLevel = Math.min(Math.max(0, level), maxMoodLevel);
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(MOOD_LEVEL, moodLevel);
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
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(TEXTURE_VARIANT, this.textureVariant);
        }
    }

    public String getTextureVariant() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(TEXTURE_VARIANT) : textureVariant;
    }

    public String getSkinOwnerName() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(SKIN_OWNER_NAME) : skinOwnerName;
    }

    public void setSkinOwnerName(String name) {
        this.skinOwnerName = name != null ? name : "";
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(SKIN_OWNER_NAME, this.skinOwnerName);
        }
    }

    public int getAffection() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(AFFECTION) : affection;
    }

    public void setAffection(int value) {
        this.affection = Math.min(Math.max(0, value), maxAffection);
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(AFFECTION, this.affection);
        }
    }

    public void addAffection(int amount) {
        setAffection(affection + amount);
    }

    public int getHunger() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(HUNGER) : hunger;
    }

    public void setHunger(int value) {
        this.hunger = Math.min(Math.max(0, value), maxHunger);
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(HUNGER, this.hunger);
        }
    }

    public int getEmoteType() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(EMOTE_TYPE) : emoteType;
    }

    public int getEmoteTicks() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(EMOTE_TICKS) : emoteTicks;
    }

    public void triggerEmote(int type, int durationTicks) {
        this.emoteType = type;
        this.emoteTicks = durationTicks;
        if (getEntityWorld() != null && !getEntityWorld().isClient()) {
            getDataTracker().set(EMOTE_TYPE, emoteType);
            getDataTracker().set(EMOTE_TICKS, emoteTicks);
        }
    }

    public void toggle() {
        this.isFollowing = !this.isFollowing;
        PlayerEntity owner = getOwner();
        if (owner != null) {
            String msg = this.isFollowing ? WaitAndFollowLines.pickFollowYou(this) : WaitAndFollowLines.pickWaitHere(this);
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + msg), false);
        }
    }

    public boolean isFollowingOwner() {
        return this.isFollowing;
    }

    public void setSitting(boolean sitting) {
        this.isSitting = sitting;
        if (getEntityWorld() != null && !getEntityWorld().isClient()) getDataTracker().set(SITTING, isSitting);
    }

    public boolean isSitting() {
        return getEntityWorld() != null && getEntityWorld().isClient() ? getDataTracker().get(SITTING) : isSitting;
    }

    public void toggleSit() {
        setSitting(!isSitting);
        PlayerEntity owner = getOwner();
        if (owner != null) {
            owner.sendMessage(Text.literal("♥ " + getDisplayNameForChat() + ": " + (isSitting ? "I'll sit here for a bit." : "I'm up! Let's go!")), false);
        }
    }

    public int getLastRelationshipLevel() {
        return lastRelationshipLevel;
    }

    public void requestTeleport(double x, double y, double z) {
        if (getEntityWorld() instanceof ServerWorld) {
            teleport(x, y, z, true);
        }
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        PlayerEntity owner = getOwner();
        if (owner != null && source.getAttacker() instanceof PlayerEntity attacker) {
            if (attacker == owner) {
                addRelationship(-10);
                setTarget(owner);
                angeredAtTick = getWorldTime();
                if (owner instanceof ServerPlayerEntity spe) {
                    DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOwner(this), false);
                }
            } else {
                addRelationship(-5);
                if (owner instanceof ServerPlayerEntity spe) {
                    DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOther(this), false);
                }
            }
        } else if (source.getAttacker() instanceof LivingEntity attacker && owner != null) {
            setTarget(attacker);
            angeredAtTick = getWorldTime();
            if (owner instanceof ServerPlayerEntity spe) {
                DelayedChatReply.sendImmediate(spe, this, HugAndHitResponses.pickHitByOther(this), false);
            }
        }

        boolean result = super.damage(world, source, amount);

        if (result) {
            setMoodLevel(Math.max(0, moodLevel - 5));
        }
        if (this.getHealth() <= 0 && owner != null) {
            owner.sendMessage(Text.literal("No! " + getDisplayNameForChat() + " has fallen..."), false);
        }

        return result;
    }

    @Override
    protected void updatePostDeath() {
        super.updatePostDeath();
        if (this.deathTime >= 20 && !this.getEntityWorld().isClient() && !this.isRemoved()) {
            this.discard();
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return getOwner() == null && !isPersistent();
    }
}
