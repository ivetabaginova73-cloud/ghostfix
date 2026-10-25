package com.ghostfix;

import com.ghostfix.gui.GhostFixScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class GhostFixClient implements ClientModInitializer {
	/** Number of ticks a missed click is still remembered (counts down). */
	public static int bufferLeft = 0;

	private static KeyBinding openSettingsKey;

	@Override
	public void onInitializeClient() {
		GhostFixConfig.load();

		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ghostfix.open",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyBinding.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettingsKey.wasPressed()) {
				client.setScreen(new GhostFixScreen(client.currentScreen));
			}
		});
	}
}
