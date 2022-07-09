package com.joeskott.ridingutils.item.custom;

import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import com.joeskott.ridingutils.item.ModItems;
import com.joeskott.ridingutils.sound.ModSounds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RidingWhipItem extends Item {
    Random random = new Random();

    int cooldownTicks = RidingUtilsCommonConfigs.ridingWhipCooldownTicks.get();
    int damageOnUse = 1;
    boolean ejectPlayer = false;

    boolean offhandIsReins = false;

    public RidingWhipItem(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        if(!player.isPassenger()) {
            return super.use(level, player, usedHand);
        }

        ItemStack itemSelf = player.getItemInHand(usedHand);
        ItemStack itemOffhand = player.getOffhandItem();

        offhandIsReins = itemOffhand.is(ModItems.REINS.get());

        Entity playerMount = player.getVehicle();

        int maxDamage = itemSelf.getMaxDamage();
        int currentDamage = itemSelf.getDamageValue();

        int chanceRange = maxDamage - currentDamage + 1;

        boolean isOnGround = playerMount.isOnGround();
        boolean isInWater = playerMount.isInWater();

        if(isOnGround == true) {
            addMotion(playerMount);
        } else if(isInWater == true) {
            addWaterMotion(playerMount);
        } else if(offhandIsReins && RidingUtilsCommonConfigs.reinsNegateFallDamage.get()) {
            playerMount.resetFallDistance();
        }

        if(!level.isClientSide()) { //Are we on server?
            if(isOnGround == true) {
                activateWhipSound(playerMount);
                player.getCooldowns().addCooldown(this, cooldownTicks);
                damageItem(player, itemSelf, damageOnUse);
                rollForHPDamage(player, playerMount, chanceRange, currentDamage, maxDamage);
            }

            if(ejectPlayer == true && RidingUtilsCommonConfigs.ridingWhipBuck.get() == true) {
                // Called if bad stuff happened oops
                playerMount.ejectPassengers();
                buckPlayer(player, playerMount);
                ejectPlayer = false;
            } else if(ejectPlayer == true) {
                ejectPlayer = false;
            }
        }

        return super.use(level, player, usedHand);
    }

    private void activateWhipSound(Entity soundSourceEntity) {
        soundSourceEntity.playSound(ModSounds.RIDING_WHIP_ACTIVE.get(), 1.0f, getVariablePitch(0.4f));
    }


    private float getVariablePitch(float maxVariance) {
        float pitchAdjust = random.nextFloat(maxVariance) - random.nextFloat(maxVariance);
        float pitch = 1.2f + pitchAdjust;
        return pitch;
    }


    private void damageItem(Player player, ItemStack item, int damageAmount) {
        item.hurtAndBreak(
                damageAmount,
                player,
                (pPlayer) -> pPlayer.broadcastBreakEvent(pPlayer.getUsedItemHand()));
    }

    private void addMotion(Entity playerMount) {
        if(!playerMount.isOnGround() && !playerMount.isInWater()) {
            return;
        }
        Vec3 lookAngle = playerMount.getLookAngle();
        Vec3 lastMotion = playerMount.getDeltaMovement();
        Vec3 newMotion = new Vec3(lastMotion.x + lookAngle.x, lastMotion.y + lookAngle.y + 0.4, lastMotion.z + lookAngle.z);
        playerMount.setDeltaMovement(newMotion);
    }


    private void buckPlayer(Player player, Entity playerMount) {
        if(player.isPassenger()) {
            return;
        }
        player.stopFallFlying();
    }


    private void addWaterMotion(Entity playerMount) {
        if(!playerMount.isInWater()) {
            return;
        }
        Vec3 lookAngle = playerMount.getLookAngle();
        Vec3 newMotion = new Vec3(lookAngle.x / 3, 0.05f, lookAngle.z / 3);
        playerMount.setDeltaMovement(newMotion);
    }

    private void addSpeed(Entity playerMount, int amplifier, int duration) {
        //playerMount
        if(playerMount instanceof LivingEntity) {
            LivingEntity livingEntity = ((LivingEntity) playerMount);
            MobEffectInstance speedEffect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, false, false);
            livingEntity.addEffect(speedEffect);
        }
    }

    private void rollForHPDamage(Player player, Entity playerMount, int chanceRange, int currentDamage, int maxDamage) {
        int roll = random.nextInt(chanceRange);

        int damageCheck = RidingUtilsCommonConfigs.ridingWhipDangerStart.get();

        if(currentDamage < damageCheck || roll != 0) {
            doHurt(playerMount, 0.0f);
            addSpeed(playerMount, 2, 120);
        } else {
            doRealDamageAndSideEffects(player, playerMount);
        }
    }

    private void doRealDamageAndSideEffects(Player player, Entity playerMount) {
        ejectPlayer = random.nextBoolean();
        float hurtAmount = random.nextFloat(2.0f);
        doHurt(playerMount, hurtAmount);
    }

    private void doHurt(Entity playerMount, float hurtAmount) {
        if(!playerMount.isOnGround()) {
            return;
        }
        if(playerMount instanceof LivingEntity) {
            LivingEntity livingEntity = ((LivingEntity) playerMount);
            boolean isHorse = playerMount instanceof Horse;
            boolean showDamage = RidingUtilsCommonConfigs.ridingWhipAnimDamage.get();

            if (hurtAmount > 0 || !isHorse) {
                if(hurtAmount < 1.0f && !showDamage) {
                    return;
                }
                livingEntity.hurt(DamageSource.GENERIC, hurtAmount);
            } else if (isHorse) {
                int bound = 3;
                if(!RidingUtilsCommonConfigs.ridingWhipAnimDamage.get()) {
                    bound = 2;
                }
                int choose = random.nextInt(bound);
                float pitch = getVariablePitch(0.3f);


                switch (choose) {
                    case 0:
                        playerMount.playSound(SoundEvents.HORSE_ANGRY, 1.0f, pitch);
                        break;
                    case 1:
                        playerMount.playSound(SoundEvents.HORSE_BREATHE, 1.0f, pitch);
                        break;
                    case 2:
                        playerMount.playSound(SoundEvents.HORSE_HURT, 1.0f, pitch);
                        break;
                }
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(Screen.hasShiftDown()) {
            pTooltipComponents.add(new TranslatableComponent("tooltip.ridingutils.riding_whip.tooltip.shift"));
        } else {
            pTooltipComponents.add(new TranslatableComponent("tooltip.ridingutils.riding_whip.tooltip"));
        }
    }
}
