package com.logisticshub;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockHutLogisticsHub extends AbstractBlockHut<BlockHutLogisticsHub>
{
    public BlockHutLogisticsHub()
    {
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "logisticshub";
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos pos, @NotNull final BlockState state)
    {
        return new TileEntityLogisticsHub(pos, state);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.LOGISTICS_HUB.get();
    }
}
