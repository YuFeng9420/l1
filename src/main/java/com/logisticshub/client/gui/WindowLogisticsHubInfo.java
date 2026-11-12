package com.logisticshub.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.logisticshub.LogisticsHubInfoModuleView;
import com.logisticshub.LogisticsTaskRecord;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 物流站点信息页窗口，展示配送范围、库存概况、总仓关系与配送任务状态。
 */
public class WindowLogisticsHubInfo extends AbstractModuleWindow<LogisticsHubInfoModuleView>
{
    private static final String LABEL_RANGE = "rangeValue";
    private static final String LABEL_STORAGE = "storageValue";
    private static final String LABEL_WAREHOUSE = "warehouseValue";
    private static final String LABEL_COURIERS = "couriersValue";
    private static final String LABEL_REQUESTS = "requestsValue";
    private static final String LABEL_NAME = "nameValue";

    public WindowLogisticsHubInfo(final LogisticsHubInfoModuleView moduleView)
    {
        super(moduleView, new ResourceLocation("logisticshub", "gui/layouthuts/layouthutlogisticshubinfo.xml"));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        refresh();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        refresh();
    }

    private void refresh()
    {
        if (moduleView.getLevel() == 0)
        {
            final Text range = findPaneOfTypeByID(LABEL_RANGE, Text.class);
            if (range != null)
            {
                range.setText(Component.literal("加载中..."));
            }
            return;
        }

        final Text name = findPaneOfTypeByID(LABEL_NAME, Text.class);
        if (name != null)
        {
            final String n = moduleView.getDisplayName();
            name.setText(Component.literal(n == null || n.isEmpty() ? "物流站点" : n));
        }

        final Text range = findPaneOfTypeByID(LABEL_RANGE, Text.class);
        if (range != null)
        {
            range.setText(Component.translatable(
                "com.logisticshub.gui.logisticshub.info.range.value",
                moduleView.getLevel(),
                moduleView.getRadius()));
        }

        final Text storage = findPaneOfTypeByID(LABEL_STORAGE, Text.class);
        if (storage != null)
        {
            storage.setText(Component.translatable(
                "com.logisticshub.gui.logisticshub.info.storage.value",
                moduleView.getUsedSlots(),
                moduleView.getTotalSlots()));
        }

        final Text warehouse = findPaneOfTypeByID(LABEL_WAREHOUSE, Text.class);
        if (warehouse != null)
        {
            if (moduleView.getWarehouseDistance() >= 0)
            {
                warehouse.setText(Component.translatable(
                    "com.logisticshub.gui.logisticshub.info.warehouse.value",
                    moduleView.getWarehouseDistance()));
            }
            else
            {
                warehouse.setText(Component.translatable(
                    "com.logisticshub.gui.logisticshub.info.warehouse.none"));
            }
        }

        final Text couriers = findPaneOfTypeByID(LABEL_COURIERS, Text.class);
        if (couriers != null)
        {
            couriers.setText(Component.translatable(
                "com.logisticshub.gui.logisticshub.info.couriers.value",
                moduleView.getCouriers()));
        }

        final Text requests = findPaneOfTypeByID(LABEL_REQUESTS, Text.class);
        if (requests != null)
        {
            requests.setText(Component.translatable(
                "com.logisticshub.gui.logisticshub.info.requests.value",
                moduleView.getOpenRequests()));
        }

        final com.ldtteam.blockui.views.ScrollingList list =
            findPaneOfTypeByID("taskList", com.ldtteam.blockui.views.ScrollingList.class);
        if (list != null)
        {
            final java.util.List<LogisticsTaskRecord> tasks = moduleView.getTasks();
            list.setDataProvider(new com.ldtteam.blockui.views.ScrollingList.DataProvider()
            {
                @Override public int getElementCount() { return tasks.size(); }

                @Override
                public void updateElement(int index, com.ldtteam.blockui.Pane row)
                {
                    final LogisticsTaskRecord r = tasks.get(index);

                    Text requester = row.findPaneOfTypeByID("taskRequester", Text.class);
                    if (requester != null)
                    {
                        requester.setText(Component.literal(
                            (r.requesterName == null || r.requesterName.isEmpty() ? "?" : r.requesterName)));
                    }

                    Text item = row.findPaneOfTypeByID("taskItem", Text.class);
                    if (item != null)
                    {
                        String name = r.item.isEmpty() ? "?" : r.item.getHoverName().getString();
                        item.setText(Component.literal(name + " x" + r.totalAmount));
                    }

                    Text phase = row.findPaneOfTypeByID("taskPhase", Text.class);
                    if (phase != null)
                    {
                        String phaseText = switch (r.phase)
                        {
                            case "HUB_TO_SITE" -> "总仓→站点";
                            case "SITE_TO_REQUESTER" -> "站点→建筑";
                            case "COMPLETED" -> "已完成";
                            default -> r.phase;
                        };
                        phase.setText(Component.literal(phaseText + "  " + r.progressPercent() + "%"));
                    }
                }
            });
        }
    }
}
