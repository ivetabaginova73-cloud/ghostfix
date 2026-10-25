package com.ghostfix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Settings, saved to config/ghostfix.json. */
public class GhostFixConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ghostfix.json");

	private static GhostFixConfig instance = new GhostFixConfig();

	/** Master switch. */
	public boolean enabled = true;
	/** How many ticks (1 tick = 0.05 s) a missed click stays "remembered". */
	public int bufferTicks = 2;
	/** Only help against other players. */
	public boolean onlyPlayers = true;
	/** Only help when the target is in the air. */
	public boolean onlyMidAir = false;
	/** Wait for the attack cooldown so the buffered hit is not a weak hit. */
	public boolean waitForCooldown = true;
	/** Cooldown needed (percent) before a buffered hit fires. */
	public int cooldownPercent = 90;

	public static GhostFixConfig get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				GhostFixConfig loaded = GSON.fromJson(Files.readString(FILE), GhostFixConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
		} catch (Exception e) {
			System.err.println("[GhostFix] Could not read config, using defaults: " + e);
		}
		instance.clamp();
		save();
	}

	public static void save() {
		instance.clamp();
		try {
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (IOException e) {
			System.err.println("[GhostFix] Could not save config: " + e);
		}
	}

	public static void reset() {
		instance = new GhostFixConfig();
		save();
	}

	private void clamp() {
		bufferTicks = Math.max(1, Math.min(5, bufferTicks));
		cooldownPercent = Math.max(0, Math.min(100, cooldownPercent));
	}
}
