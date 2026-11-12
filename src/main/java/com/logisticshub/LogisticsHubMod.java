package com.logisticshub;

import com.logisticshub.network.LogisticsHubNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LogisticsHubMod.MOD_ID)
public class LogisticsHubMod
{
    public static final String MOD_ID = "logisticshub";
    public static final Logger LOGGER = LogManager.getLogger("LogisticsHub");

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> LOGISTICS_TAB =
        CREATIVE_MODE_TABS.register("logisticshub", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.logisticshub"))
            .icon(() -> new ItemStack(ModItems.LOGISTICS_HUB_ITEM.get()))
            .displayItems((parameters, output) -> output.accept(ModItems.LOGISTICS_HUB_ITEM.get()))
            .build());

    public LogisticsHubMod()
    {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBuildings.BUILDINGS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        LogisticsHubNetwork.register();
    }
}
