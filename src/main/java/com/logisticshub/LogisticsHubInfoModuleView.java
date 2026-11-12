package com.logisticshub;

import com.ldtteam.blockui.views.BOWindow;
import com.logisticshub.client.gui.WindowLogisticsHubInfo;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 物流站点信息页的客户端视图，接收服务端同步的物流数据并提供对应的窗口。
 */
public class LogisticsHubInfoModuleView extends AbstractBuildingModuleView
{
    private String displayName = "";
    private int level;
    private int radius;
    private int usedSlots;
    private int totalSlots;
    private int warehouseDistance;
    private int couriers;
    private int openRequests;
    private final java.util.List<LogisticsTaskRecord> tasks = new java.util.ArrayList<>();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        displayName = buf.readUtf();
        level = buf.readInt();
        radius = buf.readInt();
        usedSlots = buf.readInt();
        totalSlots = buf.readInt();
        warehouseDistance = buf.readInt();
        couriers = buf.readInt();
        openRequests = buf.readInt();

        tasks.clear();
        final int n = buf.readInt();
        for (int i = 0; i < n; i++)
        {
            tasks.add(LogisticsTaskRecord.read(buf));
        }
    }

    @Override
    public boolean isPageVisible()
    {
        return true;
    }

    @Override
    public String getIcon()
    {
        return "info";
    }

    @Override
    public Component getDesc()
    {
        return Component.translatable("com.logisticshub.gui.logisticshub.info");
    }

    @NotNull
    @Override
    public BOWindow getWindow()
    {
        return new WindowLogisticsHubInfo(this);
    }

    public int getLevel()
    {
        return level;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public java.util.List<LogisticsTaskRecord> getTasks()
    {
        return tasks;
    }

    public int getRadius()
    {
        return radius;
    }

    public int getUsedSlots()
    {
        return usedSlots;
    }

    public int getTotalSlots()
    {
        return totalSlots;
    }

    public int getWarehouseDistance()
    {
        return warehouseDistance;
    }

    public int getCouriers()
    {
        return couriers;
    }

    public int getOpenRequests()
    {
        return openRequests;
    }
}
