package com.logisticshub;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems
{
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, LogisticsHubMod.MOD_ID);

    public static final RegistryObject<Item> LOGISTICS_HUB_ITEM =
        ITEMS.register("logisticshub", () -> new BlockItem(ModBlocks.HUT_LOGISTICS.get(), new Item.Properties()));

    private ModItems()
    {
    }
}
