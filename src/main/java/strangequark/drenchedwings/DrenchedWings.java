package strangequark.drenchedwings;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrenchedWings implements ModInitializer {
    public static final String MOD_ID = "drenchedwings";
    public static final String MOD_NAME = "Drenched Wings";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int COOLDOWN_SECONDS = 20;

    public static final GameRule<Boolean> DO_ELYTRA_NERF = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "do_elytra_nerf"));
    public static final GameRule<Boolean> DISABLE_ELYTRA_IN_RAIN = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "disable_elytra_in_rain"));
    public static final GameRule<Boolean> DISABLE_ELYTRA_IN_WATER = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "disable_elytra_in_water"));
    public static final GameRule<Boolean> DISABLE_ELYTRA_IN_LAVA = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "disable_elytra_in_lava"));
    public static final GameRule<Boolean> DISABLE_ROCKETS_IN_RAIN = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "disable_rockets_in_rain"));
    public static final GameRule<Boolean> APPLY_ELYTRA_COOLDOWN_FROM_SPLASH_WATER = GameRuleBuilder.forBoolean(true).buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "apply_elytra_cooldown_from_splash_water"));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getGameRules().get(DO_ELYTRA_NERF)) {
                for (ServerPlayer player : world.getPlayers(serverPlayer -> true)) {
                    if (world.getGameRules().get(DISABLE_ELYTRA_IN_RAIN) && world.isRaining() && world.canSeeSky(player.blockPosition())) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                    if (world.getGameRules().get(DISABLE_ELYTRA_IN_WATER) && player.isInWater()) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                    if (world.getGameRules().get(DISABLE_ELYTRA_IN_LAVA) && player.isInLava()) {
                        player.getCooldowns().addCooldown(new ItemStack(Items.ELYTRA), COOLDOWN_SECONDS * 20);
                    }
                }
            }
        });

        UseItemCallback.EVENT.register((player, level, interactionHand) -> {
            if (!(level instanceof ServerLevel world) || !world.getGameRules().get(DO_ELYTRA_NERF)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(interactionHand);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                if (world.getGameRules().get(DISABLE_ROCKETS_IN_RAIN) && world.isRaining() && world.canSeeSky(player.blockPosition())) {
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
    }
}