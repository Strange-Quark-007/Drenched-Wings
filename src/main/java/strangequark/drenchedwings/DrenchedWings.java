package strangequark.drenchedwings;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrenchedWings implements ModInitializer {
    public static final String MOD_ID = "drenchedwings";
    public static final String MOD_NAME = "Drenched Wings";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int COOLDOWN_SECONDS = 20;

    public static final GameRules.Key<GameRules.BooleanValue> DO_ELYTRA_NERF = GameRuleRegistry.register("doElytraNerf", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DISABLE_ELYTRA_IN_RAIN = GameRuleRegistry.register("disableElytraInRain", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DISABLE_ELYTRA_IN_WATER = GameRuleRegistry.register("disableElytraInWater", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DISABLE_ELYTRA_IN_LAVA = GameRuleRegistry.register("disableElytraInLava", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DISABLE_ROCKETS_IN_RAIN = GameRuleRegistry.register("disableRocketsInRain", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> APPLY_ELYTRA_COOLDOWN_FROM_SPLASH_WATER = GameRuleRegistry.register("applyElytraCooldownFromSplashWater", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getGameRules().getBoolean(DO_ELYTRA_NERF)) {
                for (ServerPlayer player : world.getPlayers(serverPlayer -> true)) {
                    if (world.getGameRules().getBoolean(DISABLE_ELYTRA_IN_RAIN) && world.isRaining() && world.canSeeSky(player.blockPosition())) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                    if (world.getGameRules().getBoolean(DISABLE_ELYTRA_IN_WATER) && player.isInWater()) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                    if (world.getGameRules().getBoolean(DISABLE_ELYTRA_IN_LAVA) && player.isInLava()) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                }
            }
        });

        UseItemCallback.EVENT.register((player, level, interactionHand) -> {
            if (!(level instanceof ServerLevel world) || !world.getGameRules().getBoolean(DO_ELYTRA_NERF)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(interactionHand);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                if (world.getGameRules().getBoolean(DISABLE_ROCKETS_IN_RAIN) && world.isRaining() && world.canSeeSky(player.blockPosition())) {
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });

        EntityElytraEvents.ALLOW.register(entity -> {
            if (entity instanceof Player player) {
                var elytraStack = player.getItemBySlot(EquipmentSlot.CHEST);

                return !player.getCooldowns().isOnCooldown(elytraStack);
            }
            return true;
        });


        UseEntityCallback.EVENT.register((player, level, interactionHand, entity, entityHitResult) -> {
            if (entityHitResult == null) {
                return InteractionResult.PASS;
            }

            var itemStack = player.getItemInHand(interactionHand);
            var potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (potionContents.is(Potions.WATER)) {
                var hitEntity = entityHitResult.getEntity();
                if (hitEntity instanceof ServerPlayer playerHit) {
                    playerHit.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                }
            }
            return InteractionResult.PASS;
        });
    }
}