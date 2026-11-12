package com.logisticshub.network;

import com.logisticshub.BuildingLogisticsHub;
import com.logisticshub.LogisticsHubCourierModule;
import com.logisticshub.ModBuildings;
import com.logisticshub.client.gui.WindowTownHallLogisticsStandalone;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class TownHallLogisticsResponseMessage
{
    private final int siteCount, warehouseCount, totalCouriers, totalOpenRequests, unassigned;
    private final int[] levelCounts;
    private final List<SiteRow> sites;
    private final List<UnassignedRow> unassignedRows = new ArrayList<>();

    public record SiteRow(BlockPos pos, int level, String name, int couriers, int openRequests) {}
    public record UnassignedRow(String requesterName, String itemName, int amount) {}

    public TownHallLogisticsResponseMessage(int siteCount, int warehouseCount, int totalCouriers, int totalOpenRequests,
                                            int unassigned, int[] levelCounts, List<BuildingLogisticsHub> hubs,
                                            List<UnassignedRow> rows)
    {
        this.siteCount = siteCount;
        this.warehouseCount = warehouseCount;
        this.totalCouriers = totalCouriers;
        this.totalOpenRequests = totalOpenRequests;
        this.unassigned = unassigned;
        this.levelCounts = levelCounts.clone();
        this.sites = new ArrayList<>();
        for (BuildingLogisticsHub hub : hubs)
        {
            LogisticsHubCourierModule cm = hub.getModule(ModBuildings.LOGISTICS_HUB_COURIER);
            int couriers = cm != null ? cm.getAssignedCitizen().size() : 0;
            int open = 0;
            for (Collection<IToken<?>> tokens : hub.getOpenRequestsByRequestableType().values())
                open += tokens.size();
            sites.add(new SiteRow(hub.getPosition(), hub.getBuildingLevel(),
                hub.getBuildingDisplayName(), couriers, open));
        }
        if (rows != null) unassignedRows.addAll(rows);
    }

    public static void encode(TownHallLogisticsResponseMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.siteCount);
        buf.writeInt(msg.warehouseCount);
        buf.writeInt(msg.totalCouriers);
        buf.writeInt(msg.totalOpenRequests);
        buf.writeInt(msg.unassigned);
        for (int i = 1; i <= 5; i++) buf.writeInt(msg.levelCounts[i]);
        buf.writeInt(msg.sites.size());
        for (SiteRow r : msg.sites)
        {
            buf.writeBlockPos(r.pos());
            buf.writeInt(r.level());
            buf.writeUtf(r.name() == null ? "" : r.name());
            buf.writeInt(r.couriers());
            buf.writeInt(r.openRequests());
        }
        buf.writeInt(msg.unassignedRows.size());
        for (UnassignedRow r : msg.unassignedRows)
        {
            buf.writeUtf(r.requesterName() == null ? "" : r.requesterName());
            buf.writeUtf(r.itemName() == null ? "" : r.itemName());
            buf.writeInt(r.amount());
        }
    }

    public static TownHallLogisticsResponseMessage decode(FriendlyByteBuf buf)
    {
        int sc = buf.readInt(), wc = buf.readInt(), tc = buf.readInt(), to = buf.readInt(), un = buf.readInt();
        int[] lc = new int[6];
        for (int i = 1; i <= 5; i++) lc[i] = buf.readInt();
        int n = buf.readInt();
        List<SiteRow> rows = new ArrayList<>();
        for (int i = 0; i < n; i++)
        {
            BlockPos p = buf.readBlockPos();
            int l = buf.readInt();
            String name = buf.readUtf();
            int c = buf.readInt();
            int o = buf.readInt();
            rows.add(new SiteRow(p, l, name, c, o));
        }
        int un2 = buf.readInt();
        List<UnassignedRow> ur = new ArrayList<>();
        for (int i = 0; i < un2; i++)
        {
            String rn = buf.readUtf();
            String in = buf.readUtf();
            int am = buf.readInt();
            ur.add(new UnassignedRow(rn, in, am));
        }
        TownHallLogisticsResponseMessage msg = new TownHallLogisticsResponseMessage(sc, wc, tc, to, un, lc, List.of(), List.of());
        msg.sites.clear();
        msg.sites.addAll(rows);
        msg.unassignedRows.clear();
        msg.unassignedRows.addAll(ur);
        return msg;
    }

    public static void handle(TownHallLogisticsResponseMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> Minecraft.getInstance().execute(() -> {
            try { new WindowTownHallLogisticsStandalone(msg).open(); }
            catch (Throwable ignored) {}
        }));
        ctx.get().setPacketHandled(true);
    }

    public int getSiteCount() { return siteCount; }
    public int getWarehouseCount() { return warehouseCount; }
    public int getTotalCouriers() { return totalCouriers; }
    public int getTotalOpenRequests() { return totalOpenRequests; }
    public int getUnassigned() { return unassigned; }
    public int getLevelCount(int i) { return (i >= 1 && i <= 5) ? levelCounts[i] : 0; }
    public List<SiteRow> getSites() { return sites; }
    public List<UnassignedRow> getUnassignedRows() { return unassignedRows; }
}
