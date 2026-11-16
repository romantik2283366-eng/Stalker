package com.stalker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class StalkerClient implements ClientModInitializer {
    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("stalker", "main"));

    private static KeyBinding summonKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        summonKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stalker.summon",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY
        ));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stalker.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (summonKey.wasPressed()) {
                StalkerModule.summonNow(client);
            }
            while (toggleKey.wasPressed()) {
                StalkerModule.toggle(client);
            }
            StalkerModule.tick(client);
        });
    }
}
