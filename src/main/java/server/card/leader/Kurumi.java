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

public class Kurumi extends LeaderText {
    public static final String NAME = "Kurumi";
    public static final String DESCRIPTION = "The worst spirit.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Pleased to meet you.")
            .setLine(Emote.THANKS, "Should I say, thanks?")
            .setLine(Emote.SORRY, "Sorry~")
            .setLine(Emote.WELLPLAYED, "I expected nothing less!")
            .setLine(Emote.SHOCKED, "Is that how it is?")
            .setLine(Emote.THINKING, "Hmm...?")
            .setLine(Emote.THREATEN, "It's hopeless, no matter what you try.")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, DESCRIPTION, "leader/kurumi.png",
            CRAFT, TRAITS, RARITY, Kurumi.class,
            new Vector2f(), -1, null,
            EMOTESET);

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of();
    }

    @Override
    public TooltipLeader getTooltip() {
        return TOOLTIP;
    }
}
