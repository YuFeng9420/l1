package com.logisticshub.mixin;

import com.ldtteam.blockui.controls.ButtonImage;
import com.logisticshub.network.LogisticsHubNetwork;
import com.logisticshub.network.RequestTownHallLogisticsMessage;
import com.minecolonies.core.client.gui.townhall.AbstractWindowTownHall;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWindowTownHall.class)
public abstract class AbstractWindowTownHallMixin
{
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void logisticshub$addTab(CallbackInfo ci)
    {
        AbstractWindowTownHall self = (AbstractWindowTownHall)(Object)this;
        if (self.findPaneByID("logisticshub_tab") != null) return;

        ButtonImage tab = new ButtonImage();
        tab.setID("logisticshub_tab");
        tab.setSize(17, 17);
        tab.setPosition(445, 50);
        tab.setImage(new ResourceLocation("minecolonies", "textures/gui/red_wax_arrow_round.png"), false);
        self.addChild(tab);
        self.registerButton("logisticshub_tab", () ->
            LogisticsHubNetwork.CHANNEL.sendToServer(new RequestTownHallLogisticsMessage()));
    }
}
