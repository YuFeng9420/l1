package com.logisticshub.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.logisticshub.BuildingLogisticsHub;
import com.minecolonies.core.client.gui.AbstractBuildingMainWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WindowLogisticsHub extends AbstractBuildingMainWindow<BuildingLogisticsHub.View>
{
    private static final ResourceLocation LOGISTICS_HUB_WINDOW =
        new ResourceLocation("logisticshub", "gui/layouthuts/layouthutlogisticshub.xml");

    public WindowLogisticsHub(final BuildingLogisticsHub.View building)
    {
        super(building, LOGISTICS_HUB_WINDOW);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final Text name = findPaneOfTypeByID("siteNameLabel", Text.class);
        if (name != null)
        {
            final String n = buildingView.getBuildingDisplayName();
            name.setText(Component.literal(n == null || n.isEmpty() ? "物流站点" : n));
        }
    }
}
