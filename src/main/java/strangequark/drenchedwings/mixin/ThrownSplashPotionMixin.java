package strangequark.drenchedwings.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static strangequark.drenchedwings.DrenchedWings.*;

@Mixin(AbstractThrownPotion.class)
public class ThrownSplashPotionMixin {
    @Inject(method = "onHitAsWater", at = @At("HEAD"))
    private void onPotionHit(ServerLevel serverLevel, CallbackInfo ci) {
        if (!serverLevel.getGameRules().getBoolean(DO_ELYTRA_NERF) || !serverLevel.getGameRules().getBoolean(APPLY_ELYTRA_COOLDOWN_FROM_SPLASH_WATER)) {
            return;
        }

        Entity self = (Entity) (Object) this;
        AABB aABB = self.getBoundingBox().inflate(4.0, 2.0, 4.0);
        ItemStack elytraStack = new ItemStack(Items.ELYTRA);

        for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, aABB)) {
            if (!(livingEntity instanceof ServerPlayer player)) {
                continue;
            }

            double d = self.distanceToSqr(livingEntity);
            if (d < 16.0) {
                double e = 1.0 - Math.sqrt(d) / 4.0;
                int scaledTicks = (int) ((COOLDOWN_SECONDS * e + 0.5) * 20);

                ItemCooldowns cd = player.getCooldowns();
                ItemCooldownsAccessor cdAcc = (ItemCooldownsAccessor) cd;

                ResourceLocation resourceLocation = cd.getCooldownGroup(elytraStack);
                Object instObj = cdAcc.getRawCooldowns().get(resourceLocation);

                int now = cdAcc.getTickCount();

                if (instObj == null) {
                    cd.addCooldown(elytraStack, scaledTicks);
                } else {
                    int endTime = ((CooldownInstanceAccessor) instObj).getEndTime();
                    int remaining = endTime - now;

                    if (remaining < scaledTicks) {
                        cd.addCooldown(elytraStack, scaledTicks);
                    }
                }
            }
        }
    }
}
