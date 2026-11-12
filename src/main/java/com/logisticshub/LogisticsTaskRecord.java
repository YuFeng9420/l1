package com.logisticshub;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class LogisticsTaskRecord
{
    public IToken<?> requestToken;
    public String requesterName = "";
    public ItemStack item = ItemStack.EMPTY;
    public int totalAmount;
    public int deliveredToHub;
    public int deliveredToRequester;
    public String phase = "HUB_TO_SITE";

    public int progressPercent()
    {
        if ("HUB_TO_SITE".equals(phase)) return 0;
        if ("SITE_TO_REQUESTER".equals(phase)) return 50;
        if ("COMPLETED".equals(phase)) return 100;
        return 0;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeUtf(requesterName == null ? "" : requesterName);
        buf.writeItem(item);
        buf.writeInt(totalAmount);
        buf.writeInt(deliveredToHub);
        buf.writeInt(deliveredToRequester);
        buf.writeUtf(phase == null ? "" : phase);
    }

    public static LogisticsTaskRecord read(FriendlyByteBuf buf)
    {
        LogisticsTaskRecord r = new LogisticsTaskRecord();
        r.requesterName = buf.readUtf();
        r.item = buf.readItem();
        r.totalAmount = buf.readInt();
        r.deliveredToHub = buf.readInt();
        r.deliveredToRequester = buf.readInt();
        r.phase = buf.readUtf();
        return r;
    }
}
