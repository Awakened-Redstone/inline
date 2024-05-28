package com.samsthenerd.inline.forge;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.InlineClient;
import com.samsthenerd.inline.config.InlineConfig;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("inline")
public class InlineForge {
    public InlineForge(){
        // so that we can register properly with architectury
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Inline.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        
        // note, technically double nested lambdas, so should be fine ?
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, 
            () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> InlineConfig.getConfigScreen(parent)));


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.register(InlineForgeClient.class));

        Inline.onInitialize();
    }

    private void onClientSetup(FMLClientSetupEvent event) { 
        event.enqueueWork(() -> {
            InlineClient.initClient();
        });
    }
}
