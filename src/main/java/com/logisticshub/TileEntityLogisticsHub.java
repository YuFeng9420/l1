package com.logisticshub;

import com.minecolonies.core.tileentities.TileEntityWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntity for the logistics hub. Extends the core warehouse tile entity so that
 * BuildingWareHouse.getTileEntity() recognizes it as a warehouse tile entity.
 * getType() reports the logisticshub block entity type so that chunk serialization
 * recreates a TileEntityLogisticsHub instead of a plain warehouse.
 */
public class TileEntityLogisticsHub extends TileEntityWareHouse
{
    public TileEntityLogisticsHub(final BlockPos pos, final BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType()
    {
        return ModBlockEntities.LOGISTICS_HUB_TILE.get();
    }
}
