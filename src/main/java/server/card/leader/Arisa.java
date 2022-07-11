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

public class Arisa extends LeaderText {
    public static final String NAME = "Arisa";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Hi there!")
            .setLine(Emote.THANKS, "Thanks!")
            .setLine(Emote.SORRY, "Sorry!")
            .setLine(Emote.WELLPLAYED, "Whoa!")
            .setLine(Emote.SHOCKED, "(Gasp)")
            .setLine(Emote.THINKING, "What should I do?")
            .setLine(Emote.THREATEN, "Time for target practice!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/arisa.png",
            CRAFT, TRAITS, RARITY, Arisa.class,
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
