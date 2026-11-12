package com.logisticshub;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, LogisticsHubMod.MOD_ID);

    public static final RegistryObject<Block> HUT_LOGISTICS =
        BLOCKS.register("logisticshub", BlockHutLogisticsHub::new);

    private ModBlocks()
    {
    }
}
