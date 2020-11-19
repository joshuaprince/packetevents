/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.utils.entityfinder;

import io.github.retrooper.packetevents.annotations.Nullable;
import io.github.retrooper.packetevents.exceptions.PacketEventsEntityByIDNotFoundException;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class EntityFinderUtils {
    @Nullable
    public static ServerVersion version;
    private static Class<?> worldServerClass;
    private static Class<?> craftWorldClass;
    private static Method getEntityByIdMethod;
    private static Method craftWorldGetHandle;

    private static boolean isServerVersion_v_1_8_x;

    public static void load() {
        isServerVersion_v_1_8_x = version.isHigherThan(ServerVersion.v_1_7_10) && version.isLowerThan(ServerVersion.v_1_9);
        try {
            worldServerClass = NMSUtils.getNMSClass("WorldServer");
            craftWorldClass = NMSUtils.getOBCClass("CraftWorld");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            getEntityByIdMethod = worldServerClass.getMethod(isServerVersion_v_1_8_x ? "a" : "getEntity", int.class);
            craftWorldGetHandle = craftWorldClass.getMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object getNMSEntityById(final int id) {
        for (final World world : Bukkit.getWorlds()) {
            final Object entity = getNMSEntityByIdWithWorld(world, id);
            if (entity != null) {
                return entity;
            }
        }
        //Entity has not been found in the NMS entity cache map...
        for(World world : Bukkit.getWorlds()) {
            for(Entity entity : world.getEntities()) {
                if(entity.getEntityId() == id) {
                    return NMSUtils.getNMSEntity(entity);
                }
            }
        }
        throw new PacketEventsEntityByIDNotFoundException(id);
    }

    /**
     * Get an entity by their ID.
     *
     * @param id
     * @return Entity
     */
    public static Entity getEntityById(final int id) {
        for (final World world : Bukkit.getWorlds()) {
            final Object entity = getNMSEntityByIdWithWorld(world, id);
            if (entity != null) {
                return NMSUtils.getBukkitEntity(entity);
            }
        }
        //Entity has not been found in the NMS entity cache map...
        for(World world : Bukkit.getWorlds()) {
            for(Entity entity : world.getEntities()) {
                if(entity.getEntityId() == id) {
                    return entity;
                }
            }
        }
        throw new PacketEventsEntityByIDNotFoundException(id);
    }

    public static Object getNMSEntityByIdWithWorld(final World world, final int id) {
        if (world == null) {
            return null;
        } else if (craftWorldClass == null) {
            throw new IllegalStateException("PacketEvents failed to locate the CraftWorld class.");
        }
        Object worldServer = null;
        try {
            worldServer = craftWorldGetHandle.invoke(world);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            return getEntityByIdMethod.invoke(worldServer, id);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
       return null;
    }

    /**
     * Get an entity by their ID, guaranteed to be in the specified world.
     *
     * @param world
     * @param id
     * @return Entity
     */
    @Nullable
    public static Entity getEntityByIdWithWorld(final World world, final int id) {
        Object nmsEntity = getNMSEntityByIdWithWorld(world, id);
        return NMSUtils.getBukkitEntity(nmsEntity);
    }
}
