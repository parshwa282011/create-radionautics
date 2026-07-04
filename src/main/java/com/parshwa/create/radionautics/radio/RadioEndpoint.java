package com.parshwa.create.radionautics.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface RadioEndpoint {
    ResourceKey<Level> dimension();

    BlockPos blockPos();

    Vec3 radioPosition();

    boolean isRemoved();
}
