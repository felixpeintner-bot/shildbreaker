package com.example.shieldbreaker;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Einfache Einstellungen, gespeichert in config/shieldbreaker.properties. */
public final class ShieldBreakerConfig {
	/** Schild automatisch mit der Axt brechen. */
	public static boolean enabled = true;
	/** Nach dem Axtschlag sofort mit der Mace nachschlagen (Stun Slam). */
	public static boolean stunSlam = false;
	/** Ticks zwischen Axtschlag und Mace-Schlag (0 = im selben Tick). */
	public static int maceDelay = 2;

	private ShieldBreakerConfig() {}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("shieldbreaker.properties");
	}

	public static void load() {
		Path f = file();
		if (!Files.exists(f)) return;
		try (Reader r = Files.newBufferedReader(f)) {
			Properties p = new Properties();
			p.load(r);
			enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
			stunSlam = Boolean.parseBoolean(p.getProperty("stunSlam", "false"));
			maceDelay = Math.max(0, Math.min(10, Integer.parseInt(p.getProperty("maceDelay", "2"))));
		} catch (IOException | NumberFormatException ignored) {
		}
	}

	public static void save() {
		Properties p = new Properties();
		p.setProperty("enabled", Boolean.toString(enabled));
		p.setProperty("stunSlam", Boolean.toString(stunSlam));
		p.setProperty("maceDelay", Integer.toString(maceDelay));
		try (Writer w = Files.newBufferedWriter(file())) {
			p.store(w, "Shield Breaker");
		} catch (IOException ignored) {
		}
	}
}
