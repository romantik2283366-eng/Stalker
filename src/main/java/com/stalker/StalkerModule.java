package com.stalker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

import java.util.Random;

public final class StalkerModule {
    private static final Random RNG = new Random();

    private static boolean enabled = true;
    private static Stalker current;
    private static ClientWorld lastWorld;
    private static int timer = 1200 + RNG.nextInt(2400);
    private static double distance = 40.0;

    private StalkerModule() {}

    public static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        if (world != lastWorld) {
            current = null;
            lastWorld = world;
        }
        if (world == null || player == null) {
            current = null;
            return;
        }
        if (!enabled) {
            clear();
            return;
        }

        if (current != null) {
            if (!current.tick(player)) {
                current = null;
                afterAppearance();
            }
            return;
        }

        if (--timer <= 0) {
            current = Stalker.spawn(world, player, distance);
            if (current == null) {
                timer = 200;
            }
        }
    }

    /** Клавиша K: вызвать существо сразу (для проверки). */
    public static void summonNow(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if (world == null || player == null) return;

        if (!enabled) {
            player.sendMessage(Text.literal("Stalker выключен (клавиша J)"), true);
            return;
        }

        clear();
        current = Stalker.spawn(world, player, 25.0);
        if (current == null) {
            player.sendMessage(Text.literal("Нет подходящего места рядом"), true);
        }
    }

    /** Клавиша J: включить или выключить мод. */
    public static void toggle(MinecraftClient client) {
        enabled = !enabled;
        if (!enabled) {
            clear();
        }
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("Stalker: " + (enabled ? "ON" : "OFF")), true);
        }
    }

    private static void clear() {
        if (current != null) {
            current.remove();
            current = null;
        }
    }

    private static void afterAppearance() {
        timer = 1200 + RNG.nextInt(2400);
        distance = Math.max(14.0, distance - 4.0);
    }
}
// END OF FILE
