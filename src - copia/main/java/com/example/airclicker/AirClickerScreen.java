package com.example.airclicker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class AirClickerScreen extends Screen {
    private final Screen parent;
    private EditBox intervalField;
    private String error = "";

    AirClickerScreen(Screen parent) {
        super(Component.literal("AirClicker"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        intervalField = new EditBox(this.font, center - 90, this.height / 2 - 20, 180, 22,
                Component.literal("Intervalo en segundos"));
        intervalField.setMaxLength(12);
        intervalField.setValue(String.valueOf(AirClickerClient.config().intervalSeconds));
        addRenderableWidget(intervalField);

        addRenderableWidget(Button.builder(Component.literal("Guardar"), button -> saveAndClose())
                .bounds(center - 90, this.height / 2 + 18, 85, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), button -> onClose())
                .bounds(center + 5, this.height / 2 + 18, 85, 22).build());
        addRenderableWidget(Button.builder(Component.literal(AirClickerClient.isEnabled() ? "Desactivar" : "Activar"), button -> {
            AirClickerClient.setEnabled(!AirClickerClient.isEnabled());
            button.setMessage(Component.literal(AirClickerClient.isEnabled() ? "Desactivar" : "Activar"));
        }).bounds(center - 90, this.height / 2 + 48, 180, 22).build());
        setInitialFocus(intervalField);
    }

    private void saveAndClose() {
        try {
            double value = Double.parseDouble(intervalField.getValue().trim().replace(',', '.'));
            if (!Double.isFinite(value) || value < 0.8 || value > 180.0) {
                error = "Usa un valor entre 0.8 y 180 segundos.";
                return;
            }
            AirClickerClient.config().intervalSeconds = value;
            AirClickerClient.saveConfig();
            onClose();
        } catch (NumberFormatException exception) {
            error = "Escribe un número válido.";
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.width / 2 - 125, this.height / 2 - 78,
                this.width / 2 + 125, this.height / 2 + 86, 0xE51B1D27);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 64, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, "Intervalo (0.8 a 180 segundos)", this.width / 2,
                this.height / 2 - 38, 0xFFB9C2D0);
        if (!error.isEmpty()) graphics.drawCenteredString(this.font, error, this.width / 2,
                this.height / 2 + 76, 0xFFFF7777);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
