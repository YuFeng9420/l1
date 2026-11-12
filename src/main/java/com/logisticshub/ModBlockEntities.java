package com.logisticshub;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LogisticsHubMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<TileEntityLogisticsHub>> LOGISTICS_HUB_TILE =
        BLOCK_ENTITIES.register("logisticshub", () ->
            BlockEntityType.Builder.of(TileEntityLogisticsHub::new, ModBlocks.HUT_LOGISTICS.get()).build(null));

    private ModBlockEntities()
    {
    }
}
