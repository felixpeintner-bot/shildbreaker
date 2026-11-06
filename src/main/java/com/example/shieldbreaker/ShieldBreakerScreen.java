package com.example.shieldbreaker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menü, das mit Rechte STRG geöffnet wird. */
public class ShieldBreakerScreen extends Screen {
	public ShieldBreakerScreen() {
		super(Component.literal("Shield Breaker"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = this.height / 2 - 40;

		this.addRenderableWidget(Button.builder(shieldLabel(), b -> {
			ShieldBreakerConfig.enabled = !ShieldBreakerConfig.enabled;
			b.setMessage(shieldLabel());
			ShieldBreakerConfig.save();
		}).bounds(cx - 100, y, 200, 20).build());

		this.addRenderableWidget(Button.builder(slamLabel(), b -> {
			ShieldBreakerConfig.stunSlam = !ShieldBreakerConfig.stunSlam;
			b.setMessage(slamLabel());
			ShieldBreakerConfig.save();
		}).bounds(cx - 100, y + 28, 200, 20).build());

		this.addRenderableWidget(Button.builder(delayLabel(), b -> {
			ShieldBreakerConfig.maceDelay = (ShieldBreakerConfig.maceDelay + 1) % 11;
			b.setMessage(delayLabel());
			ShieldBreakerConfig.save();
		}).bounds(cx - 100, y + 56, 200, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
				.bounds(cx - 100, y + 90, 200, 20).build());
	}

	private static Component shieldLabel() {
		return Component.literal("Auto-Schild-Break: " + (ShieldBreakerConfig.enabled ? "AN" : "AUS"));
	}

	private static Component slamLabel() {
		return Component.literal("Stun Slam (Axt -> Mace): " + (ShieldBreakerConfig.stunSlam ? "AN" : "AUS"));
	}

	private static Component delayLabel() {
		return Component.literal("Mace-Verzögerung: " + ShieldBreakerConfig.maceDelay + " Ticks");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		String title = "Shield Breaker";
		graphics.text(this.font, title, (this.width - this.font.width(title)) / 2, this.height / 2 - 70, 0xFFFFFFFF, true);
		String hint = "Stun Slam: Mace muss in der Hotbar liegen";
		graphics.text(this.font, hint, (this.width - this.font.width(hint)) / 2, this.height / 2 + 66, 0xFFAAAAAA, true);
	}
}
