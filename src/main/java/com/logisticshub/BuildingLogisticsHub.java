package com.logisticshub;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.logisticshub.client.gui.WindowLogisticsHub;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingLogisticsHub extends BuildingWareHouse
{
    private static final String SCHEMATIC_NAME = "logisticshub";
    private static final int MAX_BUILDING_LEVEL = 5;

    private final java.util.Map<com.minecolonies.api.colony.requestsystem.token.IToken<?>, LogisticsTaskRecord> tasks
        = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Set<com.minecolonies.api.colony.requestsystem.token.IToken<?>> assignedRequestIds
        = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public BuildingLogisticsHub(final IColony colony, final BlockPos pos)
    {
        super(colony, pos);
    }

    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    public void addTask(LogisticsTaskRecord record)
    {
        if (record != null && record.requestToken != null)
        {
            tasks.put(record.requestToken, record);
            assignedRequestIds.add(record.requestToken);
        }
    }

    public LogisticsTaskRecord getTask(com.minecolonies.api.colony.requestsystem.token.IToken<?> token)
    {
        return tasks.get(token);
    }

    public void updateTaskPhase(com.minecolonies.api.colony.requestsystem.token.IToken<?> token, String phase)
    {
        LogisticsTaskRecord r = tasks.get(token);
        if (r != null) r.phase = phase;
    }

    public void markDeliveredToHub(com.minecolonies.api.colony.requestsystem.token.IToken<?> token, int count)
    {
        LogisticsTaskRecord r = tasks.get(token);
        if (r != null) r.deliveredToHub = count;
    }

    public void markDeliveredToRequester(com.minecolonies.api.colony.requestsystem.token.IToken<?> token, int count)
    {
        LogisticsTaskRecord r = tasks.get(token);
        if (r != null)
        {
            r.deliveredToRequester = count;
            r.phase = "COMPLETED";
        }
    }

    public void removeTask(com.minecolonies.api.colony.requestsystem.token.IToken<?> token)
    {
        tasks.remove(token);
        assignedRequestIds.remove(token);
    }

    public java.util.Collection<LogisticsTaskRecord> getTasks()
    {
        return tasks.values();
    }

    public boolean isAssigned(com.minecolonies.api.colony.requestsystem.token.IToken<?> token)
    {
        return assignedRequestIds.contains(token);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();
        final IFactoryController factory = getColony().getRequestManager().getFactoryController();
        builder.add(new BuildingRequestResolver(getLocation(), factory.getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new LogisticsHubRequestResolver(getLocation(), factory.getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new LogisticsHubDeliveryResolver(getLocation(), factory.getNewInstance(TypeConstants.ITOKEN)));
        return builder.build();
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        if (!colony.getWorld().isClientSide && getCustomName().isEmpty())
        {
            setCustomBuildingName("\u7269\u6D41\u7AD9\u70B9 #" + colony.getID());
        }
    }

    public static class View extends AbstractBuildingView
    {
        public View(final IColonyView colony, final BlockPos pos)
        {
            super(colony, pos);
        }

        @Override
        public BOWindow getWindow()
        {
            return new WindowLogisticsHub(this);
        }
    }
}
