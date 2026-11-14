package com.airclicker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ClickerScreen extends Screen {
    private static final int W = 300;
    private static final int H = 222;

    private static final int GREEN = 0xFF22C55E;
    private static final int GREEN_BG = 0xFF0F2A1A;
    private static final int AMBER = 0xFFF59E0B;
    private static final int AMBER_BG = 0xFF33260A;
    private static final int GRAY = 0xFFA1A1AA;
    private static final int WHITE = 0xFFFFFFFF;

    private ButtonWidget startBtn;
    private ButtonWidget stopBtn;

    public ClickerScreen() {
        super(Text.literal("Air AutoClicker"));
    }

    private int px() { return (width - W) / 2; }
    private int py() { return (height - H) / 2; }

    @Override
    protected void init() {
        int x = px(), y = py();
        addDrawableChild(new IntervalSlider(x + 16, y + 90, W - 32, 20));

        int bw = (W - 44) / 2;
        startBtn = addDrawableChild(ButtonWidget.builder(Text.literal("▶  Iniciar"),
                b -> AirClickerMod.start()).dimensions(x + 16, y + H - 40, bw, 24).build());
        stopBtn = addDrawableChild(ButtonWidget.builder(Text.literal("■  Detener"),
                b -> AirClickerMod.stop()).dimensions(x + 28 + bw, y + H - 40, bw, 24).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        int x = px(), y = py();
        ctx.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xFF2A2A2A); // borde
        ctx.fill(x, y, x + W, y + H, 0xF2050505);                 // panel
    }

    private void badge(DrawContext ctx, String text, int rightX, int y, int fg, int bg) {
        int tw = textRenderer.getWidth(text);
        ctx.fill(rightX - tw - 12, y - 3, rightX, y + 11, bg);
        ctx.drawText(textRenderer, text, rightX - tw - 6, y, fg, false);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        startBtn.active = !AirClickerMod.running;
        stopBtn.active = AirClickerMod.running;

        super.render(ctx, mouseX, mouseY, delta);

        int x = px(), y = py();
        int left = x + 16, right = x + W - 16;

        // Encabezado
        ctx.drawText(textRenderer, "Air AutoClicker", left, y + 14, WHITE, true);
        badge(ctx, "Clic al aire", right, y + 12, GREEN, GREEN_BG);

        // Estado
        ctx.drawText(textRenderer, "Estado", left, y + 38, WHITE, true);
        if (AirClickerMod.running) badge(ctx, "En marcha", right, y + 36, AMBER, AMBER_BG);
        else badge(ctx, "Preparado", right, y + 36, GREEN, GREEN_BG);

        // Valor grande
        var m = ctx.getMatrices();
        m.push();
        m.scale(2f, 2f, 1f);
        ctx.drawText(textRenderer, ClickerConfig.format(ClickerConfig.intervalSeconds),
                left / 2, (y + 54) / 2, GREEN, true);
        m.pop();
        ctx.drawText(textRenderer, "Intervalo de clics", left, y + 76, GRAY, false);

        // Separador y datos
        ctx.fill(left, y + 120, right, y + 121, 0xFF1F1F1F);
        row(ctx, "Tiempo mínimo", "0.8 segundos", y + 130, left, right);
        row(ctx, "Tiempo máximo", "180 segundos (3 min)", y + 146, left, right);
        row(ctx, "Tecla de activación",
                AirClickerMod.toggleKey.getBoundKeyLocalizedText().getString(), y + 162, left, right);
    }

    private void row(DrawContext ctx, String label, String value, int y, int left, int right) {
        ctx.drawText(textRenderer, label, left, y, GRAY, false);
        ctx.drawText(textRenderer, value, right - textRenderer.getWidth(value), y, WHITE, true);
    }

    @Override
    public void removed() {
        ClickerConfig.save();
    }

    /** Slider logarítmico: más precisión en tiempos cortos, llega hasta 3 minutos. */
    private static class IntervalSlider extends SliderWidget {
        IntervalSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), toSlider(ClickerConfig.intervalSeconds));
            updateMessage();
        }

        static double toSlider(double s) {
            return Math.log(s / ClickerConfig.MIN) / Math.log(ClickerConfig.MAX / ClickerConfig.MIN);
        }

        static double fromSlider(double v) {
            return ClickerConfig.MIN * Math.pow(ClickerConfig.MAX / ClickerConfig.MIN, v);
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Intervalo: " + ClickerConfig.format(ClickerConfig.intervalSeconds)));
        }

        @Override
        protected void applyValue() {
            ClickerConfig.set(fromSlider(value));
        }
    }
}
