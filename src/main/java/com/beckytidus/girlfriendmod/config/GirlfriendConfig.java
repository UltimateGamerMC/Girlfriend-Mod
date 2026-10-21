package com.beckytidus.girlfriendmod.config;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-side settings, stored in config/girlfriend-mod.json.
 * Edit the file and run /girlfriend reload (or restart) to apply.
 */
public final class GirlfriendConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static GirlfriendConfig instance = new GirlfriendConfig();

    /** Master switch for gifts. Players can also turn them off per girlfriend in her menu or with /girlfriend gifts false. */
    public boolean giftsEnabled = true;
    /** 1.0 = default pace, 0.5 = half as often, 2.0 = twice as often. */
    public double giftFrequency = 1.0;
    /** Whether she wanders off to pick you flowers. */
    public boolean pickFlowers = true;
    /** Extra item ids she can gift on top of her usual ones, e.g. "plushables:bear_plush". */
    public List<String> extraGiftItems = new ArrayList<>();
    /** Item ids she will never gift, e.g. "minecraft:totem_of_undying". */
    public List<String> blockedGiftItems = new ArrayList<>();

    private GirlfriendConfig() {
    }

    public static GirlfriendConfig get() {
        return instance;
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("girlfriend-mod.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                GirlfriendConfig loaded = GSON.fromJson(reader, GirlfriendConfig.class);
                if (loaded != null) {
                    loaded.sanitize();
                    instance = loaded;
                }
            } catch (IOException | RuntimeException e) {
                GirlfriendMod.LOGGER.warn("Could not read {}; using defaults", file, e);
            }
        }
        // always write back so new options show up in existing files
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            GirlfriendMod.LOGGER.warn("Could not write {}", file, e);
        }
    }

    private void sanitize() {
        if (Double.isNaN(giftFrequency) || giftFrequency < 0.05) giftFrequency = 0.05;
        if (giftFrequency > 10) giftFrequency = 10;
        if (extraGiftItems == null) extraGiftItems = new ArrayList<>();
        if (blockedGiftItems == null) blockedGiftItems = new ArrayList<>();
    }
}
