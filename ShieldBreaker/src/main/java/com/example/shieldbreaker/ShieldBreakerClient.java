package com.example.shieldbreaker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Predicate;

public class ShieldBreakerClient implements ClientModInitializer {
	private enum Phase { IDLE, AXE_SENT, MACE_SENT }

	/** Ticks, die nach dem letzten Schlag gewartet wird, bevor zur alten Waffe gewechselt wird. */
	private static final int RESTORE_DELAY = 2;
	private static final double MAX_REACH = 6.0;

	private static Phase phase = Phase.IDLE;
	private static int originalSlot = -1;
	private static int maceSlot = -1;
	private static int timer = 0;
	private static LivingEntity target;
	/** Verhindert, dass unser eigener Mace-Schlag den Callback erneut auslöst. */
	private static boolean selfAttack = false;

	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		ShieldBreakerConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shieldbreaker", "main"));
		openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.shieldbreaker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_RCONTROL, category));

		AttackEntityCallback.EVENT.register(ShieldBreakerClient::onAttack);
		ClientTickEvents.END_CLIENT_TICK.register(ShieldBreakerClient::onTick);
	}

	private static InteractionResult onAttack(Player player, Level level, InteractionHand hand,
											  Entity entity, EntityHitResult hit) {
		if (!level.isClientSide() || selfAttack || phase != Phase.IDLE || !ShieldBreakerConfig.enabled) {
			return InteractionResult.PASS;
		}
		if (!(entity instanceof LivingEntity living) || !living.isBlocking()) {
			return InteractionResult.PASS;
		}

		Inventory inv = player.getInventory();
		int axeSlot = findHotbar(inv, s -> s.is(ItemTags.AXES));
		if (axeSlot < 0) return InteractionResult.PASS;

		originalSlot = inv.getSelectedSlot();
		maceSlot = ShieldBreakerConfig.stunSlam ? findHotbar(inv, s -> s.is(Items.MACE)) : -1;
		target = living;
		phase = Phase.AXE_SENT;
		timer = (maceSlot >= 0) ? ShieldBreakerConfig.maceDelay : RESTORE_DELAY;

		// Slot wechseln, bevor der Angriff rausgeht – der Schlag erfolgt dann mit der Axt.
		inv.setSelectedSlot(axeSlot);
		return InteractionResult.PASS;
	}

	private static void onTick(Minecraft client) {
		while (openGuiKey.consumeClick()) {
			if (client.gui.screen() == null) {
				client.gui.setScreen(new ShieldBreakerScreen());
			}
		}

		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			reset();
			return;
		}
		if (phase == Phase.IDLE) return;
		if (timer-- > 0) return;

		Inventory inv = player.getInventory();
		if (phase == Phase.AXE_SENT) {
			boolean canSlam = maceSlot >= 0 && target != null && target.isAlive()
					&& player.distanceTo(target) <= MAX_REACH;
			if (canSlam) {
				inv.setSelectedSlot(maceSlot);
				selfAttack = true;
				try {
					client.gameMode.attack(player, target);
					player.swing(InteractionHand.MAIN_HAND);
				} finally {
					selfAttack = false;
				}
				phase = Phase.MACE_SENT;
				timer = RESTORE_DELAY;
			} else {
				restore(inv);
			}
		} else {
			restore(inv);
		}
	}

	private static void restore(Inventory inv) {
		if (originalSlot >= 0 && originalSlot <= 8) {
			inv.setSelectedSlot(originalSlot);
		}
		reset();
	}

	private static void reset() {
		phase = Phase.IDLE;
		originalSlot = -1;
		maceSlot = -1;
		target = null;
		timer = 0;
	}

	private static int findHotbar(Inventory inv, Predicate<ItemStack> test) {
		for (int i = 0; i < 9; i++) {
			if (test.test(inv.getItem(i))) return i;
		}
		return -1;
	}
}
