package com.logisticshub;

import com.ldtteam.blockui.views.BOWindow;
import com.logisticshub.client.gui.WindowTownHallLogistics;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 市政厅物流页面的客户端视图，接收全局物流网络数据并提供对应的窗口。
 */
public class TownHallLogisticsModuleView extends AbstractBuildingModuleView
{
    public static final int MAX_LEVEL = 5;

    private final List<SiteInfo> sites = new ArrayList<>();
    private int warehouseCount;
    private int totalCouriers;
    private int totalOpenRequests;
    private final int[] levelCounts = new int[MAX_LEVEL + 1];

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        sites.clear();
        final int siteCount = buf.readInt();
        for (int i = 0; i < siteCount; i++)
        {
            final BlockPos pos = buf.readBlockPos();
            final int level = buf.readInt();
            final String name = buf.readUtf();
            final int couriers = buf.readInt();
            final int openRequests = buf.readInt();
            sites.add(new SiteInfo(pos, level, name, couriers, openRequests));
        }

        warehouseCount = buf.readInt();
        totalCouriers = buf.readInt();
        totalOpenRequests = buf.readInt();
        for (int i = 1; i <= MAX_LEVEL; i++)
        {
            levelCounts[i] = buf.readInt();
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
        return "requests";
    }

    @Override
    public Component getDesc()
    {
        return Component.translatable("com.logisticshub.gui.townhall.logistics");
    }

    @NotNull
    @Override
    public BOWindow getWindow()
    {
        return new WindowTownHallLogistics(this);
    }

    public List<SiteInfo> getSites()
    {
        return sites;
    }

    public int getWarehouseCount()
    {
        return warehouseCount;
    }

    public int getTotalCouriers()
    {
        return totalCouriers;
    }

    public int getTotalOpenRequests()
    {
        return totalOpenRequests;
    }

    public int getLevelCount(final int level)
    {
        if (level < 1 || level > MAX_LEVEL)
        {
            return 0;
        }
        return levelCounts[level];
    }

    public static class SiteInfo
    {
        private final BlockPos position;
        private final int level;
        private final String name;
        private final int couriers;
        private final int openRequests;

        public SiteInfo(final BlockPos position, final int level, final String name, final int couriers, final int openRequests)
        {
            this.position = position;
            this.level = level;
            this.name = name;
            this.couriers = couriers;
            this.openRequests = openRequests;
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public int getLevel()
        {
            return level;
        }

        public String getName()
        {
            return name;
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
}
