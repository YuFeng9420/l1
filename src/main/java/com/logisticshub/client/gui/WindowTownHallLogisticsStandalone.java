package com.logisticshub.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.logisticshub.network.TownHallLogisticsResponseMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WindowTownHallLogisticsStandalone extends BOWindow
{
    private final TownHallLogisticsResponseMessage data;

    public WindowTownHallLogisticsStandalone(TownHallLogisticsResponseMessage data)
    {
        super(new ResourceLocation("logisticshub", "gui/townhall/layouttownhalllogistics.xml"));
        this.data = data;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        setText("sitesCount", "com.logisticshub.gui.townhall.summary.sites.value", data.getSiteCount());
        setText("warehousesCount", "com.logisticshub.gui.townhall.summary.warehouses.value", data.getWarehouseCount());
        setText("couriersTotal", "com.logisticshub.gui.townhall.summary.couriers.value", data.getTotalCouriers());
        setText("requestsTotal", "com.logisticshub.gui.townhall.summary.requests.value", data.getTotalOpenRequests());
        setText("unassignedTotal", "com.logisticshub.gui.townhall.summary.unassigned.value", data.getUnassigned());

        Text levels = findPaneOfTypeByID("levelsValue", Text.class);
        if (levels != null)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 5; i++)
            {
                if (i > 1) sb.append("  ");
                sb.append("Lv").append(i).append(':').append(data.getLevelCount(i));
            }
            levels.setText(Component.literal(sb.toString()));
        }

        ScrollingList list = findPaneOfTypeByID("sites", ScrollingList.class);
        if (list != null)
        {
            list.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override public int getElementCount() { return data.getSites().size(); }

                @Override
                public void updateElement(int index, Pane row)
                {
                    TownHallLogisticsResponseMessage.SiteRow r = data.getSites().get(index);
                    Text name = row.findPaneOfTypeByID("siteName", Text.class);
                    if (name != null)
                    {
                        name.setText(Component.literal(r.name() == null || r.name().isEmpty()
                            ? "站点 (" + r.pos().getX() + ", " + r.pos().getZ() + ")"
                            : r.name()));
                    }
                    Text detail = row.findPaneOfTypeByID("siteDetail", Text.class);
                    if (detail != null)
                    {
                        detail.setText(Component.translatable(
                            "com.logisticshub.gui.townhall.site.detail",
                            r.level(), r.couriers(), r.openRequests()));
                    }
                }
            });
        }

        ScrollingList unList = findPaneOfTypeByID("unassignedList", ScrollingList.class);
        if (unList != null)
        {
            final java.util.List<TownHallLogisticsResponseMessage.UnassignedRow> rows = data.getUnassignedRows();
            unList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override public int getElementCount() { return rows.size(); }

                @Override
                public void updateElement(int index, Pane row)
                {
                    TownHallLogisticsResponseMessage.UnassignedRow r = rows.get(index);
                    Text req = row.findPaneOfTypeByID("unRequester", Text.class);
                    if (req != null) req.setText(Component.literal(
                        r.requesterName() == null || r.requesterName().isEmpty() ? "?" : r.requesterName()));
                    Text item = row.findPaneOfTypeByID("unItem", Text.class);
                    if (item != null) item.setText(Component.literal(
                        (r.itemName() == null ? "?" : r.itemName()) + " x" + r.amount()));
                }
            });
        }
    }

    private void setText(String id, String key, int value)
    {
        Text t = findPaneOfTypeByID(id, Text.class);
        if (t != null) t.setText(Component.translatable(key, value));
    }
}
