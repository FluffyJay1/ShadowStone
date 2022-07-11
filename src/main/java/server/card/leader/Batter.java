package server.card.leader;

import client.tooltip.TooltipLeader;
import network.Emote;
import network.EmoteSet;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Batter extends LeaderText {
    public static final String NAME = "The Batter";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "I'm here.")
            .setLine(Emote.THANKS, "Thanks.")
            .setLine(Emote.SORRY, "I apologize.")
            .setLine(Emote.WELLPLAYED, "You are a formidable adversary.")
            .setLine(Emote.SHOCKED, "Whatever.")
            .setLine(Emote.THINKING, "Damn, this lardass is beefier than I expected.")
            .setLine(Emote.THREATEN, "Purification in progress.")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "The purifier.", "res/card/indie/batter.png",
            CRAFT, TRAITS, RARITY, Batter.class,
            new Vector2f(112, 120), 2, null,
            EMOTESET);

    @Override
    protected List<Effect> getSpecialEffects() {
        return null;
    }

    @Override
    public TooltipLeader getTooltip() {
        return TOOLTIP;
    }
}
