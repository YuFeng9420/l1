package com.logisticshub.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.logisticshub.TownHallLogisticsModuleView;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 市政厅物流信息页窗口，展示全局物流网络概览、站点汇总与配送统计。
 */
public class WindowTownHallLogistics extends AbstractModuleWindow<TownHallLogisticsModuleView>
{
    private static final String LIST_SITES = "sites";
    private static final String LABEL_SITES_COUNT = "sitesCount";
    private static final String LABEL_WAREHOUSES_COUNT = "warehousesCount";
    private static final String LABEL_COURIERS_TOTAL = "couriersTotal";
    private static final String LABEL_REQUESTS_TOTAL = "requestsTotal";
    private static final String LABEL_LEVELS = "levelsValue";

    public WindowTownHallLogistics(final TownHallLogisticsModuleView moduleView)
    {
        super(moduleView, new ResourceLocation("logisticshub", "gui/townhall/layouttownhalllogistics.xml"));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        setLabel(LABEL_SITES_COUNT, "com.logisticshub.gui.townhall.summary.sites.value", moduleView.getSites().size());
        setLabel(LABEL_WAREHOUSES_COUNT, "com.logisticshub.gui.townhall.summary.warehouses.value", moduleView.getWarehouseCount());
        setLabel(LABEL_COURIERS_TOTAL, "com.logisticshub.gui.townhall.summary.couriers.value", moduleView.getTotalCouriers());
        setLabel(LABEL_REQUESTS_TOTAL, "com.logisticshub.gui.townhall.summary.requests.value", moduleView.getTotalOpenRequests());

        final Text levels = findPaneOfTypeByID(LABEL_LEVELS, Text.class);
        if (levels != null)
        {
            final StringBuilder builder = new StringBuilder();
            for (int i = 1; i <= TownHallLogisticsModuleView.MAX_LEVEL; i++)
            {
                if (i > 1)
                {
                    builder.append("  ");
                }
                builder.append("Lv").append(i).append(':').append(moduleView.getLevelCount(i));
            }
            levels.setText(Component.literal(builder.toString()));
        }

        final ScrollingList sites = findPaneOfTypeByID(LIST_SITES, ScrollingList.class);
        if (sites != null)
        {
            sites.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return moduleView.getSites().size();
                }

                @Override
                public void updateElement(final int index, final Pane rowPane)
                {
                    final TownHallLogisticsModuleView.SiteInfo site = moduleView.getSites().get(index);

                    final Text name = rowPane.findPaneOfTypeByID("siteName", Text.class);
                    if (name != null)
                    {
                        if (site.getName() != null && !site.getName().isEmpty())
                        {
                            name.setText(Component.literal(site.getName()));
                        }
                        else
                        {
                            name.setText(Component.translatable(
                                "com.logisticshub.gui.townhall.site.unnamed",
                                site.getPosition().getX(),
                                site.getPosition().getZ()));
                        }
                    }

                    final Text detail = rowPane.findPaneOfTypeByID("siteDetail", Text.class);
                    if (detail != null)
                    {
                        detail.setText(Component.translatable(
                            "com.logisticshub.gui.townhall.site.detail",
                            site.getLevel(),
                            site.getCouriers(),
                            site.getOpenRequests()));
                    }
                }
            });
        }
    }

    private void setLabel(final String id, final String key, final int value)
    {
        final Text text = findPaneOfTypeByID(id, Text.class);
        if (text != null)
        {
            text.setText(Component.translatable(key, value));
        }
    }
}
