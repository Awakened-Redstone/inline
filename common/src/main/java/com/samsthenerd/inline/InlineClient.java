package com.samsthenerd.inline;

import java.util.UUID;
import java.util.regex.MatchResult;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.InlineMatchResult.DataMatch;
import com.samsthenerd.inline.api.InlineMatchResult.TextMatch;
import com.samsthenerd.inline.api.MatcherInfo;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.matchers.RegexMatcher;
import com.samsthenerd.inline.api.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.renderers.SpriteInlineRenderer;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class InlineClient {
    public static void initClient(){

        // InlineAutoConfig.init();

        addDefaultRenderers();
        addDefaultMatchers();
    }

    private static void addDefaultRenderers(){
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(SpriteInlineRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    private static void addDefaultMatchers(){
        Identifier itemMatcherID = new Identifier(Inline.MOD_ID, "item");
        InlineClientAPI.INSTANCE.addMatcher(itemMatcherID, new RegexMatcher.Simple("<item:([a-z:\\/_]+)>", (MatchResult mr) ->{
            Item item = Registries.ITEM.get(new Identifier(mr.group(1)));
            if(item == null) return null;
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }, MatcherInfo.fromId(itemMatcherID)));

        Identifier entityMatcherID = new Identifier(Inline.MOD_ID, "entity");
        InlineClientAPI.INSTANCE.addMatcher(entityMatcherID, new RegexMatcher.Simple("<entity:([a-z:\\/_]+)>", (MatchResult mr) ->{
            EntityType entType = Registries.ENTITY_TYPE.get(new Identifier(mr.group(1)));
            if(entType == null) return null;
            return new DataMatch(EntityInlineData.fromType(entType));
        }, MatcherInfo.fromId(entityMatcherID)));

        Identifier linkMatcherId = new Identifier(Inline.MOD_ID, "link");
        InlineClientAPI.INSTANCE.addMatcher(linkMatcherId, new RegexMatcher.Simple("\\[(.*)\\]\\((.*)\\)", (MatchResult mr) ->{
            String text = mr.group(1);
            String link = mr.group(2);
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(link));
            MutableText linkText = Text.literal(text + " 🔗");
            linkText.setStyle(Style.EMPTY.withClickEvent(ce).withHoverEvent(he).withUnderline(true).withColor(Formatting.BLUE));
            return new TextMatch(linkText);
        }, MatcherInfo.fromId(linkMatcherId)));


        // InlineClientAPI.INSTANCE.addMatcher(new Identifier(Inline.MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
        //     String text = mr.group(2);
        //     int astCount = mr.group(1).length();
        //     MutableText linkText = Text.literal(text);
        //     linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
        //     return new TextMatch(linkText);
        // }));

        Identifier modMatcherId = new Identifier(Inline.MOD_ID, "modicon");
        InlineClientAPI.INSTANCE.addMatcher(modMatcherId, new RegexMatcher.Simple("<mod:([a-z:\\/_-]+)>", (MatchResult mr) -> {
            String modid = mr.group(1);
            try{
                Mod mod = Platform.getMod(modid);
                return new DataMatch(new ModIconData(modid), ModIconData.getTooltipStyle(modid));
            } catch (Exception e){
                Inline.LOGGER.error("error parsing modicon: " + modid);
                return null;
            }
        }, MatcherInfo.fromId(modMatcherId)));

        Identifier faceMatcherId = new Identifier(Inline.MOD_ID, "playerface");
        InlineClientAPI.INSTANCE.addMatcher(faceMatcherId, new RegexMatcher.Simple("<face:([a-z:A-Z0-9\\/_-]+)>", (MatchResult mr) -> {
            String playerNameOrUUID = mr.group(1);
            GameProfile profile;
            try{
                profile = new GameProfile(UUID.fromString(playerNameOrUUID), null);
            } catch (IllegalArgumentException e){
                profile = new GameProfile(null, playerNameOrUUID);
            }
            return new DataMatch(new PlayerHeadData(profile));
        }, MatcherInfo.fromId(faceMatcherId)));
    }
}
