package com.stalker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

public final class StalkerHelper {
    private StalkerHelper() {}

    /** Высота пола под точкой или null, если пола нет или он под водой. */
    public static Double groundY(ClientWorld world, double x, double refY, double z) {
        int bx = MathHelper.floor(x);
        int bz = MathHelper.floor(z);
        int startY = MathHelper.floor(refY) + 2;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int y = startY; y >= startY - 8; y--) {
            pos.set(bx, y, bz);
            VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (shape.isEmpty()) continue;

            double top = y + shape.getMax(Direction.Axis.Y);

            // Над полом должно быть свободно минимум 2 блока и не должно быть воды
            pos.set(bx, y + 1, bz);
            boolean free1 = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                    && world.getFluidState(pos).isEmpty();
            pos.set(bx, y + 2, bz);
            boolean free2 = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();

            if (free1 && free2) return top;
        }
        return null;
    }

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        MinecraftClient.getInstance().getSoundManager()
                .play(PositionedSoundInstance.master(sound, volume, pitch));
    }
}
