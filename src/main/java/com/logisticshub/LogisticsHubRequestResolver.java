package com.logisticshub;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getDefaultDeliveryPriority;
import static com.minecolonies.api.util.constant.RSConstants.CONST_WAREHOUSE_RESOLVER_PRIORITY;

public class LogisticsHubRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    private static final Map<IToken<?>, Integer> PHASE = new ConcurrentHashMap<>();

    public LogisticsHubRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeConstants.DELIVERABLE;
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> request)
    {
        if (request.getRequester().getLocation().equals(getLocation()))
        {
            return false;
        }

        if (manager.getColony().getWorld().isClientSide)
        {
            return false;
        }

        final IBuilding site = getSite(manager);
        if (site == null || site.getBuildingLevel() <= 0)
        {
            return false;
        }

        final LogisticsHubRangeModule range = site.getFirstModuleOccurance(LogisticsHubRangeModule.class);
        if (range == null || !range.isInRange(request.getRequester().getLocation().getInDimensionLocation()))
        {
            return false;
        }

        if (getStockCount(site, request.getRequest()) >= request.getRequest().getMinimumCount())
        {
            return true;
        }

        if (!hasSpace(site))
        {
            return false;
        }

        return findWarehouse(manager) != null;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return null;
        }

        final IBuilding site = getSite(manager);
        if (site == null)
        {
            return null;
        }

        final BuildingLogisticsHub hub = site instanceof BuildingLogisticsHub h ? h : null;
        if (hub != null && hub.getTask(request.getId()) == null)
        {
            final LogisticsTaskRecord rec = new LogisticsTaskRecord();
            rec.requestToken = request.getId();
            rec.requesterName = getRequesterName(manager, request);
            rec.item = request.getRequest().getResult().copy();
            rec.item.setCount(request.getRequest().getCount());
            rec.totalAmount = request.getRequest().getCount();
            hub.addTask(rec);
        }

        final int stockCount = getStockCount(site, request.getRequest());
        final ItemStack stack = request.getRequest().getResult().copy();

        if (stockCount >= request.getRequest().getMinimumCount())
        {
            stack.setCount(request.getRequest().getCount());
            final Delivery delivery = new Delivery(
                getLocation(),
                request.getRequester().getLocation(),
                stack,
                getDefaultDeliveryPriority(true));
            final IToken<?> sub = manager.createRequest(this, delivery);
            PHASE.put(request.getId(), 2);
            if (hub != null) hub.updateTaskPhase(request.getId(), "SITE_TO_REQUESTER");
            return Lists.newArrayList(sub);
        }

        final BuildingWareHouse warehouse = findWarehouse(manager);
        if (warehouse == null)
        {
            return null;
        }

        stack.setCount(request.getRequest().getCount() - stockCount);
        final Delivery delivery = new Delivery(
            warehouse.getLocation(),
            getLocation(),
            stack,
            getDefaultDeliveryPriority(true));
        final IToken<?> sub = manager.createRequest(warehouse.getRequester(), delivery);
        PHASE.put(request.getId(), 1);
        if (hub != null) hub.updateTaskPhase(request.getId(), "HUB_TO_SITE");
        return Lists.newArrayList(sub);
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        // 留空，由子请求完成 + followup 返回 null 时自动 resolved
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return null;
        }

        final Integer phase = PHASE.get(completedRequest.getId());
        if (phase == null)
        {
            return null;
        }

        final IBuilding siteBuilding = getSite(manager);
        final BuildingLogisticsHub hub = siteBuilding instanceof BuildingLogisticsHub h ? h : null;

        if (phase == 1)
        {
            final IBuilding site = getSite(manager);
            if (site == null)
            {
                PHASE.remove(completedRequest.getId());
                return null;
            }

            if (getStockCount(site, completedRequest.getRequest()) < completedRequest.getRequest().getMinimumCount())
            {
                PHASE.remove(completedRequest.getId());
                if (hub != null) hub.removeTask(completedRequest.getId());
                manager.updateRequestState(completedRequest.getId(), RequestState.REPORTED);
                return null;
            }

            final ItemStack stack = completedRequest.getRequest().getResult().copy();
            stack.setCount(completedRequest.getRequest().getCount());
            final Delivery delivery = new Delivery(
                getLocation(),
                completedRequest.getRequester().getLocation(),
                stack,
                getDefaultDeliveryPriority(true));
            final IToken<?> token = manager.createRequest(this, delivery);
            PHASE.put(completedRequest.getId(), 2);
            if (hub != null)
            {
                hub.markDeliveredToHub(completedRequest.getId(), completedRequest.getRequest().getCount());
                hub.updateTaskPhase(completedRequest.getId(), "SITE_TO_REQUESTER");
            }
            return Lists.newArrayList(manager.getRequestForToken(token));
        }

        if (phase == 2)
        {
            if (hub != null)
            {
                hub.markDeliveredToRequester(completedRequest.getId(), completedRequest.getRequest().getCount());
                hub.removeTask(completedRequest.getId());
            }
            PHASE.remove(completedRequest.getId());
            return null;
        }

        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final IBuilding sb = getSite(manager);
        if (sb instanceof BuildingLogisticsHub h) h.removeTask(request.getId());
        PHASE.remove(request.getId());
    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final IBuilding sb = getSite(manager);
        if (sb instanceof BuildingLogisticsHub h) h.removeTask(request.getId());
        PHASE.remove(request.getId());
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public int getPriority()
    {
        return CONST_WAREHOUSE_RESOLVER_PRIORITY + 1;
    }

    private IBuilding getSite(final IRequestManager manager)
    {
        return manager.getColony().getServerBuildingManager().getBuilding(getLocation().getInDimensionLocation());
    }

    private String getRequesterName(IRequestManager manager, IRequest<?> request)
    {
        try
        {
            final BlockPos pos = request.getRequester().getLocation().getInDimensionLocation();
            final IBuilding b = manager.getColony().getServerBuildingManager().getBuilding(pos);
            return b != null ? b.getBuildingDisplayName() : "";
        }
        catch (Throwable t)
        {
            return "";
        }
    }

    private BuildingWareHouse findWarehouse(final IRequestManager manager)
    {
        for (final IWareHouse warehouse : manager.getColony().getServerBuildingManager().getWareHouses())
        {
            if (warehouse instanceof BuildingWareHouse && !(warehouse instanceof BuildingLogisticsHub))
            {
                return (BuildingWareHouse) warehouse;
            }
        }
        return null;
    }

    private int getStockCount(final IBuilding site, final IDeliverable request)
    {
        int count = 0;
        final Level world = site.getColony().getWorld();
        for (final BlockPos pos : site.getContainers())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AbstractTileEntityRack rack)
            {
                count += rack.getItemCount(request::matches);
            }
        }
        return count;
    }

    private boolean hasSpace(final IBuilding site)
    {
        final Level world = site.getColony().getWorld();
        for (final BlockPos pos : site.getContainers())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AbstractTileEntityRack rack)
            {
                for (int i = 0; i < rack.getInventory().getSlots(); i++)
                {
                    if (rack.getInventory().getStackInSlot(i).isEmpty())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
