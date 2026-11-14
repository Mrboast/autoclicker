package com.airclicker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ClickerConfig {
    public static final double MIN = 0.8;    // segundos
    public static final double MAX = 180.0;  // 3 minutos

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("airclicker.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static double intervalSeconds = MIN;

    public static void set(double seconds) {
        double rounded = Math.round(seconds * 10.0) / 10.0;
        intervalSeconds = Math.max(MIN, Math.min(MAX, rounded));
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                if (o.has("intervalSeconds")) set(o.get("intervalSeconds").getAsDouble());
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("intervalSeconds", intervalSeconds);
            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {
        }
    }

    public static String format(double s) {
        if (s >= 60) {
            int t = (int) Math.round(s);
            return (t / 60) + " min " + (t % 60) + " s";
        }
        return String.format(Locale.ROOT, "%.1f s", s);
    }
}
