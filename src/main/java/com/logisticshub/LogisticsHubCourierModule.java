package com.logisticshub;

import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;

public class LogisticsHubCourierModule extends CourierAssignmentModule
{
    @Override
    public int getModuleMax()
    {
        return this.building.getBuildingLevel();
    }

    @Override
    protected String getModuleSerializationIdentifier()
    {
        return "logisticshub_courier";
    }
}
