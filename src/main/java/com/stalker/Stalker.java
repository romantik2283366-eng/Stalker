package com.stalker;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Одна тёмная фигура (клиентский эндермен), которая следит за игроком. */
public class Stalker {
    private static final double CREEP = 0.05;
    private static final int MAX_LOOK_TICKS = 160;
    private static final int MAX_LIFE_TICKS = 2400;
    private static final double RAD_TO_DEG = 57.29577951308232;

    private static int nextId = -900000;

    private final ClientWorld world;
    private final EndermanEntity entity;
    private double x, y, z;
    private int lookTicks = 0;
    private int life = 0;
    private boolean seen = false;

    private Stalker(ClientWorld world, EndermanEntity entity, double x, double y, double z) {
        this.world = world;
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Создаёт фигуру на заданном расстоянии от игрока или null, если места нет. */
    public static Stalker spawn(ClientWorld world, ClientPlayerEntity player, double distance) {
        for (int i = 0; i < 12; i++) {
            double ang = Math.random() * Math.PI * 2;
            double x = player.getX() + Math.cos(ang) * distance;
            double z = player.getZ() + Math.sin(ang) * distance;
            Double gy = StalkerHelper.groundY(world, x, player.getY(), z);
            if (gy == null) continue;

            EndermanEntity e = new EndermanEntity(EntityType.ENDERMAN, world);
            e.setId(nextId--);
            if (nextId < -2_000_000_000) nextId = -900000;
            e.setPosition(x, gy, z);
            e.setNoGravity(true);
            e.setInvulnerable(true);
            e.setSilent(true);
            world.addEntity(e);

            Stalker s = new Stalker(world, e, x, gy, z);
            s.face(player);
            return s;
        }
        return null;
    }

    /** Возвращает false, когда фигура исчезла и её нужно забыть. */
    public boolean tick(ClientPlayerEntity player) {
        if (entity.isRemoved()) return false;
        life++;

        if (isLookedAt(player)) {
            if (!seen) {
                seen = true;
                StalkerHelper.playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 0.7f, 0.5f);
            }
            lookTicks++;
            creep(player);
            if (lookTicks > MAX_LOOK_TICKS || distanceTo(player) < 4.0) {
                StalkerHelper.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.5f);
                remove();
                return false;
            }
        }

        if (life > MAX_LIFE_TICKS) {
            remove();
            return false;
        }

        face(player);
        entity.setPosition(x, y, z);
        return true;
    }

    private double distanceTo(ClientPlayerEntity player) {
        double dx = player.getX() - x;
        double dz = player.getZ() - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private void creep(ClientPlayerEntity player) {
        double dist = distanceTo(player);
        if (dist < 0.1) return;
        double nx = x + (player.getX() - x) / dist * CREEP;
        double nz = z + (player.getZ() - z) / dist * CREEP;
        Double gy = StalkerHelper.groundY(world, nx, y, nz);
        if (gy != null && Math.abs(gy - y) <= 1.1) {
            x = nx;
            z = nz;
            y = gy;
        }
    }

    private boolean isLookedAt(ClientPlayerEntity player) {
        Vec3d eye = player.getEyePos();
        Vec3d to = new Vec3d(x - eye.x, y + 1.5 - eye.y, z - eye.z).normalize();
        return player.getRotationVec(1.0f).dotProduct(to) > 0.85 && player.canSee(entity);
    }

    private void face(ClientPlayerEntity player) {
        double dx = player.getX() - x;
        double dz = player.getZ() - z;
        double dy = player.getEyeY() - (y + 2.5);
        float yaw = (float) (MathHelper.atan2(dz, dx) * RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * RAD_TO_DEG);
        entity.setYaw(yaw);
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);
        entity.setPitch(pitch);
    }

    public void remove() {
        world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
    }
}
// END OF FILE
