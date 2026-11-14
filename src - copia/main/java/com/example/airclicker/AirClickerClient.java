package com.example.airclicker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class AirClickerClient implements ClientModInitializer {
    private static final AirClickerConfig CONFIG = AirClickerConfig.load();
    private static boolean enabled = false;
    private static long nextClickAt = 0L;

    private static KeyMapping.Category category;
    private static KeyMapping toggleKey;
    private static KeyMapping settingsKey;

    @Override
    public void onInitializeClient() {
        category = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("airclicker", "main"));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.airclicker.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));
        settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.airclicker.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                nextClickAt = System.nanoTime();
                notifyPlayer(client.player, enabled ? "AirClicker activado" : "AirClicker desactivado");
            }
            while (settingsKey.consumeClick()) {
                client.setScreen(new AirClickerScreen(client.screen));
            }

            if (!enabled || client.player == null || client.level == null || client.screen != null || client.isPaused()) {
                return;
            }

            long now = System.nanoTime();
            if (now < nextClickAt) return;

            if (isAllowedTarget(client)) {
                // Encola la acción de la tecla de ataque vanilla; no envía paquetes propios.
                client.options.keyAttack.click();
            }
            nextClickAt = now + (long) (CONFIG.intervalSeconds * 1_000_000_000L);
        });
    }

    private static boolean isAllowedTarget(Minecraft client) {
        HitResult hit = client.hitResult;
        if (hit == null) return false;
        if (hit.getType() == HitResult.Type.MISS) return true;
        if (hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity() instanceof LivingEntity && !(entityHit.getEntity() instanceof Player);
        }
        // No rompe bloques: los impactos sobre bloques se ignoran.
        return false;
    }

    private static void notifyPlayer(LocalPlayer player, String message) {
        if (player != null) player.displayClientMessage(Component.literal("[AirClicker] " + message), true);
    }

    static AirClickerConfig config() { return CONFIG; }
    static boolean isEnabled() { return enabled; }
    static void setEnabled(boolean value) { enabled = value; nextClickAt = System.nanoTime(); }
    static void saveConfig() { CONFIG.save(); }
}
