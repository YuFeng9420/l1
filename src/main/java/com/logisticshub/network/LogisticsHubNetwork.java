package com.logisticshub.network;

import com.logisticshub.LogisticsHubMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class LogisticsHubNetwork
{
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(LogisticsHubMod.MOD_ID, "main"),
        () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register()
    {
        int id = 0;
        CHANNEL.registerMessage(id++, RequestTownHallLogisticsMessage.class,
            RequestTownHallLogisticsMessage::encode,
            RequestTownHallLogisticsMessage::decode,
            RequestTownHallLogisticsMessage::handle);
        CHANNEL.registerMessage(id++, TownHallLogisticsResponseMessage.class,
            TownHallLogisticsResponseMessage::encode,
            TownHallLogisticsResponseMessage::decode,
            TownHallLogisticsResponseMessage::handle);
    }

    private LogisticsHubNetwork() {}
}
