package com.logisticshub;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import net.minecraft.core.BlockPos;

public class LogisticsHubRangeModule extends AbstractBuildingModule
{
    public int getRadius()
    {
        return Math.max(1, building.getBuildingLevel());
    }

    public boolean isInRange(final BlockPos pos)
    {
        final BlockPos center = building.getPosition();
        final int radius = getRadius() * 16;
        return Math.abs(center.getX() - pos.getX()) <= radius
            && Math.abs(center.getZ() - pos.getZ()) <= radius;
    }

    public int getChebyshevDistance(final BlockPos pos)
    {
        final BlockPos center = building.getPosition();
        return Math.max(Math.abs(center.getX() - pos.getX()), Math.abs(center.getZ() - pos.getZ()));
    }
}
