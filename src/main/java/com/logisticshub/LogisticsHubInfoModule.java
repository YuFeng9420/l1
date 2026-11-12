package com.logisticshub;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 服务端模块，负责收集物流站点的运行信息（配送范围、库存、总仓关系、配送任务）并同步给客户端视图。
 */
public class LogisticsHubInfoModule extends AbstractBuildingModule implements ITickingModule
{
    private static final int SYNC_INTERVAL = 40;

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

        final int level = building.getBuildingLevel();
        final LogisticsHubCourierModule courierModule = building.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
        final int couriers = courierModule != null ? courierModule.getAssignedCitizen().size() : 0;
        int open = 0;
        for (final Collection<IToken<?>> tokens : building.getOpenRequestsByRequestableType().values())
        {
            open += tokens.size();
        }
        final String snapshot = level + ":" + couriers + ":" + open;
        if (!snapshot.equals(lastSnapshot))
        {
            lastSnapshot = snapshot;
            markDirty();
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        final IBuilding site = building;
        final int level = Math.max(1, site.getBuildingLevel());
        final int radius = level * 16;

        int usedSlots = 0;
        int totalSlots = 0;
        final Level world = site.getColony().getWorld();
        for (final BlockPos pos : site.getContainers())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AbstractTileEntityRack rack)
            {
                final IItemHandlerModifiable inventory = rack.getInventory();
                totalSlots += inventory.getSlots();
                for (int i = 0; i < inventory.getSlots(); i++)
                {
                    if (!inventory.getStackInSlot(i).isEmpty())
                    {
                        usedSlots++;
                    }
                }
            }
        }

        int warehouseDistance = -1;
        final BuildingWareHouse warehouse = findWarehouse();
        if (warehouse != null)
        {
            final BlockPos a = site.getPosition();
            final BlockPos b = warehouse.getPosition();
            warehouseDistance = Math.abs(a.getX() - b.getX()) + Math.abs(a.getZ() - b.getZ());
        }

        final LogisticsHubCourierModule courierModule = building.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
        final int couriers = courierModule != null ? courierModule.getAssignedCitizen().size() : 0;

        int openRequests = 0;
        for (final Collection<IToken<?>> tokens : site.getOpenRequestsByRequestableType().values())
        {
            openRequests += tokens.size();
        }

        buf.writeUtf(site.getBuildingDisplayName());
        buf.writeInt(level);
        buf.writeInt(radius);
        buf.writeInt(usedSlots);
        buf.writeInt(totalSlots);
        buf.writeInt(warehouseDistance);
        buf.writeInt(couriers);
        buf.writeInt(openRequests);

        if (building instanceof BuildingLogisticsHub hub)
        {
            final java.util.List<LogisticsTaskRecord> list = new java.util.ArrayList<>(hub.getTasks());
            buf.writeInt(list.size());
            for (final LogisticsTaskRecord r : list)
            {
                r.write(buf);
            }
        }
        else
        {
            buf.writeInt(0);
        }
    }

    private BuildingWareHouse findWarehouse()
    {
        for (final IWareHouse warehouse : building.getColony().getServerBuildingManager().getWareHouses())
        {
            if (warehouse instanceof BuildingWareHouse && !(warehouse instanceof BuildingLogisticsHub))
            {
                return (BuildingWareHouse) warehouse;
            }
        }
        return null;
    }
}
