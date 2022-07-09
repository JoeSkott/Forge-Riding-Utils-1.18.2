package com.joeskott.ridingutils.item.custom;

import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import com.joeskott.ridingutils.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class ReinsItem extends Item {
    public ReinsItem(Properties pProperties) {
        super(pProperties);
    }
    Random random = new Random();
    int damageOnUse = 1;

    boolean jumping = false;

    boolean isHorse = false;

    boolean cancelMotion = false;


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!player.isPassenger()) {
            return super.use(level, player, usedHand);
        }

        Entity playerMount = player.getVehicle();
        ItemStack itemSelf = player.getItemInHand(usedHand);
        ItemStack itemOffhand = player.getOffhandItem();

        boolean offhandIsWhip = itemOffhand.is(ModItems.RIDING_WHIP.get());
        boolean offhandIsSelf = itemOffhand.is(this);

        if(!itemSelf.is(ModItems.REINS.get()) || offhandIsWhip || offhandIsSelf) {
            cancelMotion = true;
        } else {
            cancelMotion = false;
        }



        if(playerMount instanceof Horse) {
            isHorse = true;
        } else {
            isHorse = false;
        }

        if(isHorse) { // Don't run code if this is a horse, but still negate fall damage
            if(RidingUtilsCommonConfigs.reinsNegateFallDamage.get()) {
                playerMount.resetFallDistance();
            }
            return super.use(level, player, usedHand);
        }


        if(!level.isClientSide()) {
            if(random.nextInt(10) == 0) {
                damageItem(player, itemSelf, damageOnUse);
                playerMount.playSound(SoundEvents.LEASH_KNOT_BREAK, 0.2f, getVariablePitch(0.5f));
            }
            if(getBlockCollision(playerMount)) {
                jumping = true;
            }
        }

        if(playerMount.isInWater()) {
            addWaterMotion(player, playerMount);
        } else if (!jumping) {
            addMotion(player, playerMount);
        } else {
            jumping = false;
            addJumpMotion(player, playerMount);
        }

        //LivingEntity mount = (LivingEntity) playerMount;
        //if(mount instanceof Enemy) {
        //    mount.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0D);
        //}


        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {

        if(!player.level.isClientSide()) {
            if(!player.isPassenger()) {

                boolean isAdult = true;
                boolean ageable = interactionTarget instanceof AgeableMob;

                if(ageable) {
                    if(((AgeableMob) interactionTarget).getAge() < 0) {
                        isAdult = false;
                    }
                }
                if(isAdult) {
                    player.startRiding(interactionTarget);
                    //tame(player, interactionTarget); //testing
                    interactionTarget.playSound(SoundEvents.PIG_SADDLE, 1.0f, 1.0f);

                }
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    private float getVariablePitch(float maxVariance) {
        float pitchAdjust = random.nextFloat(maxVariance) - random.nextFloat(maxVariance);
        float pitch = 1.0f + pitchAdjust;
        return pitch;
    }

    private void damageItem(Player player, ItemStack item, int damageAmount) {
        if(cancelMotion) {
            return;
        }
        item.hurtAndBreak(
                damageAmount,
                player,
                (pPlayer) -> pPlayer.broadcastBreakEvent(pPlayer.getUsedItemHand()));
    }

    private void addMotion(Player player, Entity playerMount) {
        if(cancelMotion) {
            return;
        }

        boolean offGroundCheck = (!playerMount.isOnGround() || playerMount.isInWater());
        boolean flightCheck = (playerMount instanceof FlyingMob);
        if(offGroundCheck && !flightCheck) {
            return;
        }
        Vec3 lookAngle = player.getLookAngle();
        Vec3 lastMotion = playerMount.getDeltaMovement();
        Vec3 newMotion = new Vec3(lastMotion.x + (lookAngle.x/2), 0.0f, lastMotion.z + (lookAngle.z/2));

        Vec3 newFlightMotion = new Vec3(lastMotion.x + (lookAngle.x * 2f), lastMotion.y + (lookAngle.y * 2f), lastMotion.z + (lookAngle.z * 2f));



        if(flightCheck) {
            setLookAngleXAndY(playerMount, player);
            playerMount.setDeltaMovement(newFlightMotion);
        } else {
            setLookAngle(playerMount, player);
            playerMount.setDeltaMovement(newMotion);
        }


    }

    private void addJumpMotion(Player player, Entity playerMount) {
        if(cancelMotion) {
            return;
        }

        Vec3 lastMotion = playerMount.getDeltaMovement();
        Vec3 lookAngle = playerMount.getLookAngle();
        setLookAngle(playerMount, player);

        if(!playerMount.isOnGround()) {
            Vec3 adjustedMotion = new Vec3(lookAngle.x, lastMotion.y + 0.1f, lookAngle.z);
            playerMount.setDeltaMovement(adjustedMotion);

            if(RidingUtilsCommonConfigs.reinsNegateFallDamage.get() == true) {
                playerMount.resetFallDistance();
            }

            return;
        }


        Vec3 newMotion = new Vec3(lastMotion.x + lookAngle.x, 0.5f, lastMotion.z + lookAngle.z);
        playerMount.setDeltaMovement(newMotion);


    }

    private void setLookAngle(Entity entity, Player player) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        //entity.setXRot(xRot);
        entity.setYRot(yRot);
    }

    private void setLookAngleXAndY(Entity entity, Player player) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        entity.setXRot(xRot);
        entity.setYRot(yRot);
    }

    private void addWaterMotion(Player player, Entity playerMount) {
        if(cancelMotion) {
            return;
        }

        boolean offGroundCheck = (!playerMount.isOnGround() && !playerMount.isInWater());
        boolean flightCheck = (playerMount instanceof FlyingMob);
        if(offGroundCheck && !flightCheck) {
            return;
        }
        Vec3 lookAngle = player.getLookAngle();
        Vec3 lastMotion = playerMount.getDeltaMovement();

        Vec3 newMotion = new Vec3(lastMotion.x + (lookAngle.x/4), 0.01f, lastMotion.z + (lookAngle.z/4));
        playerMount.setDeltaMovement(newMotion);

        setLookAngle(playerMount, player);
    }

    private boolean getBlockCollision(Entity playerMount) {
        Vec3 lookAngle = playerMount.getLookAngle();
        Vec3 position = new Vec3(playerMount.getX(), playerMount.getY(), playerMount.getZ());
        double angleX = lookAngle.x * 2.5;
        double angleZ = lookAngle.z * 2.5;

        BlockPos collidePos = new BlockPos(angleX + position.x, position.y, angleZ + position.z);
        BlockState blockState = playerMount.level.getBlockState(collidePos);

        boolean collision = blockState.getMaterial().isSolid();
        return collision;
    }

    public void tame(Player player, Entity entity) {
        //entity.setTame(true);
        //entity.setOwnerUUID(pPlayer.getUUID());
        if(player instanceof  ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, (Animal) entity);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(Screen.hasShiftDown()) {
            pTooltipComponents.add(new TranslatableComponent("tooltip.ridingutils.reins.tooltip.shift"));
        } else {
            pTooltipComponents.add(new TranslatableComponent("tooltip.ridingutils.reins.tooltip"));
        }
    }
}
