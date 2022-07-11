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

public class Luna extends LeaderText {
    public static final String NAME = "Luna";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Nice to see you!")
            .setLine(Emote.THANKS, "Thanks!")
            .setLine(Emote.SORRY, "Sorry...")
            .setLine(Emote.WELLPLAYED, "Amazing!")
            .setLine(Emote.SHOCKED, "What?!")
            .setLine(Emote.THINKING, "What should I do?")
            .setLine(Emote.THREATEN, "You should come on my adventure!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/luna.png",
            CRAFT, TRAITS, RARITY, Luna.class,
            new Vector2f(), -1, null,
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
