package com.airclicker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AirClickerMod implements ClientModInitializer {
    public static boolean running = false;
    public static KeyBinding toggleKey;
    public static KeyBinding menuKey;
    private static long nextClick = 0;
    private static boolean prevPauseOnLostFocus = true;

    @Override
    public void onInitializeClient() {
        ClickerConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.airclicker.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.airclicker"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.airclicker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.airclicker"));

        ClientTickEvents.END_CLIENT_TICK.register(AirClickerMod::tick);

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            if (!running) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden) return;
            long left = Math.max(0, nextClick - System.currentTimeMillis());
            String txt = "AutoClick ● " + ClickerConfig.format(ClickerConfig.intervalSeconds)
                    + "  (próximo en " + String.format(java.util.Locale.ROOT, "%.1f", left / 1000.0) + " s)";
            ctx.drawTextWithShadow(mc.textRenderer, txt, 6, 6, 0xFF22C55E);
        });
    }

    public static void start() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!running) {
            // Evita que el juego se pause al perder el foco (para que siga clicando en segundo plano).
            prevPauseOnLostFocus = mc.options.pauseOnLostFocus;
            mc.options.pauseOnLostFocus = false;
        }
        running = true;
        nextClick = System.currentTimeMillis() + (long) (ClickerConfig.intervalSeconds * 1000);
    }

    public static void stop() {
        if (running) {
            MinecraftClient.getInstance().options.pauseOnLostFocus = prevPauseOnLostFocus;
        }
        running = false;
    }

    private static void tick(MinecraftClient mc) {
        while (toggleKey.wasPressed()) {
            if (running) stop(); else start();
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(running ? "§aAutoclick iniciado" : "§cAutoclick detenido"), true);
            }
        }
        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new ClickerScreen());
        }

        if (!running) return;
        if (mc.player == null || mc.world == null) {
            stop();
            return;
        }
        long now = System.currentTimeMillis();
        // Con un menú abierto no se hace clic y se reinicia la cuenta.
        if (mc.currentScreen != null) {
            nextClick = now + (long) (ClickerConfig.intervalSeconds * 1000);
            return;
        }
        if (now >= nextClick) {
            KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(mc.options.attackKey.getBoundKeyTranslationKey()));
            nextClick = now + (long) (ClickerConfig.intervalSeconds * 1000);
        }
    }
}
