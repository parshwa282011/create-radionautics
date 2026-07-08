package com.parshwa.create.radionautics.radio;

import com.parshwa.create.radionautics.config.RadioConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

public final class SablePositionHelper {
    private SablePositionHelper() {
    }

    public static Vec3 radioPosition(Level level, BlockPos pos) {
        if (level == null) {
            return Vec3.atCenterOf(pos);
        }
        return JOMLConversion.toMojang(Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.atCenterOf(pos)));
    }

    public static Vec3 shipCenterPosition(BlockEntity blockEntity) {
        if (blockEntity.getLevel() == null) {
            return Vec3.atCenterOf(blockEntity.getBlockPos());
        }

        SubLevelAccess subLevel = Sable.HELPER.getContaining(blockEntity);
        if (subLevel == null) {
            return radioPosition(blockEntity.getLevel(), blockEntity.getBlockPos());
        }

        return JOMLConversion.toMojang(subLevel.boundingBox().center());
    }

    public static Quaterniondc shipRotation(BlockEntity blockEntity) {
        if (blockEntity.getLevel() == null) {
            return new Quaterniond();
        }

        SubLevelAccess subLevel = Sable.HELPER.getContaining(blockEntity);
        if (subLevel == null) {
            return new Quaterniond();
        }

        return subLevel.logicalPose().orientation();
    }

    public static boolean forceLoadContainingSubLevel(BlockEntity blockEntity) {
        if (!RadioConfig.ENABLE_SABLE_CONTINOUS_LOADING.get()) {
            return false;
        }
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        SubLevelAccess subLevel = Sable.HELPER.getContaining(blockEntity);
        if (!(subLevel instanceof ServerSubLevel serverSubLevel)) {
            return false;
        }

        ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(serverLevel);
        if (container == null) {
            return false;
        }

        container.addForceLoadTicket(serverSubLevel, SubLevelLoadingTicketType.COMMAND_FORCED, Unit.INSTANCE);
        return true;
    }
}
