package com.example.airclicker;

import net.fabricmc.loader.api.FabricLoader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class AirClickerConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("airclicker.properties");
    double intervalSeconds = 0.8;

    static AirClickerConfig load() {
        AirClickerConfig config = new AirClickerConfig();
        if (Files.exists(FILE)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(FILE)) {
                properties.load(in);
                config.intervalSeconds = clamp(Double.parseDouble(properties.getProperty("intervalSeconds", "0.8")));
            } catch (Exception ignored) {
                config.intervalSeconds = 0.8;
            }
        }
        return config;
    }

    void save() {
        Properties properties = new Properties();
        properties.setProperty("intervalSeconds", Double.toString(clamp(intervalSeconds)));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                properties.store(out, "AirClicker settings");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    static double clamp(double value) {
        if (!Double.isFinite(value)) return 0.8;
        return Math.max(0.8, Math.min(180.0, value));
    }
}
