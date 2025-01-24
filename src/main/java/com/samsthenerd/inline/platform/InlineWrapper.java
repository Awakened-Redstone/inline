package com.samsthenerd.inline.platform;

import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.impl.InlineImpl;

//? if fabric {
import com.samsthenerd.inline.Inline;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;

public class InlineWrapper implements ModInitializer {
    @Override
    public void onInitialize() {
        Inline.onInitialize();

        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
            MatchContext.ChatMatchContext ctx = MatchContext.ChatMatchContext.of(sender, message);
            for(InlineMatcher matcher : InlineImpl.SERVER_CHAT_MATCHERS){
                matcher.match(ctx);
            }
            return ctx.getFinalStyledText();
        });
    }
}
//?} elif neoforge {

/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.InlineClient;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

@Mod(Inline.MOD_ID)
public final class InlineWrapper {
    public InlineWrapper() {
        // so that we can register properly with architectury
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        IEventBus modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        modBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerChatDecoration);

        Inline.onInitialize();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(InlineClient::initClient);
    }

    private void onServerChatDecoration(ServerChatEvent event){
        MatchContext.ChatMatchContext ctx = MatchContext.ChatMatchContext.of(event.getPlayer(), event.getMessage());
        for(InlineMatcher matcher : InlineImpl.SERVER_CHAT_MATCHERS){
            matcher.match(ctx);
        }
        event.setMessage(ctx.getFinalStyledText());
    }
}
*///?}
