package com.logisticshub;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.WarehouseRequestQueueModule;
import com.minecolonies.core.colony.requestsystem.resolvers.DeliverymenRequestResolver;
import org.jetbrains.annotations.NotNull;

public class LogisticsHubDeliveryResolver extends DeliverymenRequestResolver<Delivery>
{
    public LogisticsHubDeliveryResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends Delivery> getRequestType()
    {
        return TypeConstants.DELIVERY;
    }

    @Override
    public boolean hasCouriers(@NotNull final IRequestManager manager)
    {
        final IWareHouse site = getSite(manager);
        if (site == null)
        {
            return false;
        }
        final LogisticsHubCourierModule couriers = site.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
        return couriers != null && !couriers.getAssignedCitizen().isEmpty();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
    {
        final Delivery delivery = request.getRequest();
        final ILocation startLoc = delivery.getStart();
        if (startLoc != null
            && manager.getColony().getServerBuildingManager()
                .getBuilding(startLoc.getInDimensionLocation()) instanceof IWareHouse
            && !startLoc.equals(getLocation()))
        {
            return;
        }

        final IWareHouse site = getSite(manager);
        if (site == null)
        {
            return;
        }
        final LogisticsHubCourierModule couriers = site.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
        if (couriers == null || couriers.getAssignedCitizen().isEmpty())
        {
            return;
        }
        final WarehouseRequestQueueModule queue = site.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
        if (queue == null)
        {
            return;
        }
        queue.addRequest(request.getId());
    }

    private IWareHouse getSite(final IRequestManager manager)
    {
        return manager.getColony().getServerBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
    }
}
