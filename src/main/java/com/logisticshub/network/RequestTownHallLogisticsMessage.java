package com.logisticshub.network;

import com.logisticshub.BuildingLogisticsHub;
import com.logisticshub.LogisticsHubCourierModule;
import com.logisticshub.ModBuildings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.function.Supplier;

public class RequestTownHallLogisticsMessage
{
    public RequestTownHallLogisticsMessage() {}

    public static void encode(RequestTownHallLogisticsMessage msg, FriendlyByteBuf buf) {}
    public static RequestTownHallLogisticsMessage decode(FriendlyByteBuf buf) { return new RequestTownHallLogisticsMessage(); }

    public static void handle(RequestTownHallLogisticsMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(player.level(), player.blockPosition());
            if (colony == null) return;

            int siteCount = 0, warehouseCount = 0, totalCouriers = 0, totalOpenRequests = 0;
            int[] levelCounts = new int[6];
            java.util.List<BuildingLogisticsHub> sites = new java.util.ArrayList<>();

            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (b instanceof BuildingLogisticsHub hub)
                {
                    siteCount++;
                    sites.add(hub);
                    LogisticsHubCourierModule cm = b.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
                    totalCouriers += cm != null ? cm.getAssignedCitizen().size() : 0;
                    int open = 0;
                    for (Collection<IToken<?>> tokens : b.getOpenRequestsByRequestableType().values())
                        open += tokens.size();
                    totalOpenRequests += open;
                    levelCounts[Math.min(5, Math.max(1, b.getBuildingLevel()))]++;
                }
                else if (b instanceof IWareHouse)
                {
                    warehouseCount++;
                }
            }

            java.util.Set<IToken<?>> assigned = new java.util.HashSet<>();
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (b instanceof BuildingLogisticsHub hub)
                {
                    for (var t : hub.getTasks())
                    {
                        if (t.requestToken != null) assigned.add(t.requestToken);
                    }
                }
            }

            java.util.List<TownHallLogisticsResponseMessage.UnassignedRow> unassignedRows = new java.util.ArrayList<>();
            try
            {
                var rm = colony.getRequestManager();
                var identities = ((com.minecolonies.core.colony.requestsystem.management.IStandardRequestManager) rm)
                    .getRequestIdentitiesDataStore().getIdentities();
                for (IToken<?> t : identities.keySet())
                {
                    if (assigned.contains(t)) continue;
                    var req = rm.getRequestForToken(t);
                    if (req == null) continue;
                    var state = req.getState();
                    if (state == com.minecolonies.api.colony.requestsystem.request.RequestState.COMPLETED
                        || state == com.minecolonies.api.colony.requestsystem.request.RequestState.CANCELLED)
                        continue;

                    String requesterName = "";
                    try
                    {
                        var rpos = req.getRequester().getLocation().getInDimensionLocation();
                        var rb = colony.getServerBuildingManager().getBuilding(rpos);
                        if (rb != null) requesterName = rb.getBuildingDisplayName();
                    }
                    catch (Throwable ignored) {}

                    String itemName = "";
                    int amount = 0;
                    try
                    {
                        if (req.getRequest() instanceof com.minecolonies.api.colony.requestsystem.requestable.IDeliverable d)
                        {
                            itemName = d.getResult().getHoverName().getString();
                            amount = d.getCount();
                        }
                    }
                    catch (Throwable ignored) {}

                    unassignedRows.add(new TownHallLogisticsResponseMessage.UnassignedRow(requesterName, itemName, amount));
                    if (unassignedRows.size() >= 30) break;
                }
            }
            catch (Throwable ignored) {}

            int unassigned = unassignedRows.size();

            TownHallLogisticsResponseMessage response = new TownHallLogisticsResponseMessage(
                siteCount, warehouseCount, totalCouriers, totalOpenRequests, unassigned,
                levelCounts, sites, unassignedRows);
            LogisticsHubNetwork.CHANNEL.sendTo(response, player.connection.connection,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}
