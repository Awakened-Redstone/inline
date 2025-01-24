package com.samsthenerd.inline.platform;

import com.samsthenerd.inline.registry.InlineTooltips;

//? if fabric {
import com.samsthenerd.inline.InlineClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public class InlineClientWrapper implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InlineClient.initClient();

        InlineTooltips.init();
        TooltipComponentCallback.EVENT.register(InlineTooltips::getTooltipComponent);
    }
}
//?} else {
/*import com.samsthenerd.inline.Inline;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(modid = Inline.MOD_ID, value = Dist.CLIENT, bus= EventBusSubscriber.Bus.MOD)
public class InlineClientWrapper {
    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        Inline.logPrint("registering tooltip components");
        // evt.register(MirrorTooltipData.class, MirrorTooltipComponent::new);
        InlineTooltips.init();
        for(Map.Entry<Class<? extends TooltipData>, Function<TooltipData, TooltipComponent>> entry : InlineTooltips.tooltipDataToComponent.entrySet()){
            evt.register(entry.getKey(), entry.getValue());
        }
    }
}
*///?}