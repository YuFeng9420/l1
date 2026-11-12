package com.logisticshub;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 服务端模块，挂在市政厅建筑上，收集全局物流网络信息（所有物流站点与总仓的运行状态）。
 */
public class TownHallLogisticsModule extends AbstractBuildingModule implements ITickingModule
{
    private static final int SYNC_INTERVAL = 40;

    private static final int MAX_LEVEL = 5;

    private String lastSnapshot = "";

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (colony.getWorld().isClientSide)
        {
            return;
        }
        if (colony.getWorld().getGameTime() % SYNC_INTERVAL != 0)
        {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (final IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b instanceof BuildingLogisticsHub)
            {
                final LogisticsHubCourierModule cm = b.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
                final int c = cm != null ? cm.getAssignedCitizen().size() : 0;
                int o = 0;
                for (final Collection<IToken<?>> tokens : b.getOpenRequestsByRequestableType().values())
                {
                    o += tokens.size();
                }
                sb.append(b.getPosition()).append(':').append(b.getBuildingLevel()).append(':').append(c).append(':').append(o).append(';');
            }
        }
        final String snapshot = sb.toString();
        if (!snapshot.equals(lastSnapshot))
        {
            lastSnapshot = snapshot;
            markDirty();
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        final IColony colony = building.getColony();
        final List<IBuilding> sites = new ArrayList<>();
        int warehouseCount = 0;
        int totalCouriers = 0;
        int totalOpenRequests = 0;
        final int[] levelCounts = new int[MAX_LEVEL + 1];

        for (final IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b instanceof BuildingLogisticsHub)
            {
                sites.add(b);

                final LogisticsHubCourierModule courierModule = b.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
                final int couriers = courierModule != null ? courierModule.getAssignedCitizen().size() : 0;
                totalCouriers += couriers;

                int openRequests = 0;
                for (final Collection<IToken<?>> tokens : b.getOpenRequestsByRequestableType().values())
                {
                    openRequests += tokens.size();
                }
                totalOpenRequests += openRequests;

                final int level = Math.min(MAX_LEVEL, Math.max(1, b.getBuildingLevel()));
                levelCounts[level]++;
            }
            else if (b instanceof BuildingWareHouse)
            {
                warehouseCount++;
            }
        }

        buf.writeInt(sites.size());
        for (final IBuilding site : sites)
        {
            buf.writeBlockPos(site.getPosition());
            buf.writeInt(Math.max(1, site.getBuildingLevel()));
            buf.writeUtf(site.getCustomName() == null ? "" : site.getCustomName());
            final LogisticsHubCourierModule cm = site.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
            buf.writeInt(cm != null ? cm.getAssignedCitizen().size() : 0);
            int openRequests = 0;
            for (final Collection<IToken<?>> tokens : site.getOpenRequestsByRequestableType().values())
            {
                openRequests += tokens.size();
            }
            buf.writeInt(openRequests);
        }

        buf.writeInt(warehouseCount);
        buf.writeInt(totalCouriers);
        buf.writeInt(totalOpenRequests);
        for (int i = 1; i <= MAX_LEVEL; i++)
        {
            buf.writeInt(levelCounts[i]);
        }
    }
}
