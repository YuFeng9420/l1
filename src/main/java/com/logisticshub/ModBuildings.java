package com.logisticshub;

import com.minecolonies.api.blocks.AbstractColonyBlock;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.core.colony.buildings.moduleviews.CourierAssignmentModuleView;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBuildings
{
    public static final DeferredRegister<BuildingEntry> BUILDINGS =
        DeferredRegister.create(new ResourceLocation("minecolonies", "buildings"), LogisticsHubMod.MOD_ID);

    public static final BuildingEntry.ModuleProducer<LogisticsHubCourierModule, CourierAssignmentModuleView> LOGISTICS_HUB_COURIER =
        new BuildingEntry.ModuleProducer<>("logisticshub_courier", LogisticsHubCourierModule::new, () -> CourierAssignmentModuleView::new);

    public static final BuildingEntry.ModuleProducer<LogisticsHubInfoModule, LogisticsHubInfoModuleView> LOGISTICS_HUB_INFO =
        new BuildingEntry.ModuleProducer<>("logisticshub_info", LogisticsHubInfoModule::new, () -> LogisticsHubInfoModuleView::new);

    public static final BuildingEntry.ModuleProducer<TownHallLogisticsModule, TownHallLogisticsModuleView> TOWN_HALL_LOGISTICS =
        new BuildingEntry.ModuleProducer<>("logisticshub_townhall_logistics", TownHallLogisticsModule::new, () -> TownHallLogisticsModuleView::new);

    public static final RegistryObject<BuildingEntry> LOGISTICS_HUB =
        BUILDINGS.register("logisticshub", () -> new BuildingEntry.Builder()
            .setBuildingBlock((AbstractColonyBlock<?>) ModBlocks.HUT_LOGISTICS.get())
            .setBuildingProducer(BuildingLogisticsHub::new)
            .setBuildingViewProducer(() -> BuildingLogisticsHub.View::new)
            .setRegistryName(new ResourceLocation(LogisticsHubMod.MOD_ID, "logisticshub"))
            .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
              "logisticshub_range", LogisticsHubRangeModule::new, null))
            .addBuildingModuleProducer(LOGISTICS_HUB_COURIER)
            .addBuildingModuleProducer(LOGISTICS_HUB_INFO)
            .addBuildingModuleProducer(BuildingModules.WAREHOUSE_REQUEST_QUEUE)
            .addBuildingModuleProducer(BuildingModules.WAREHOUSE_OPTIONS)
            .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
            .createBuildingEntry());

    private ModBuildings()
    {
    }
}
