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

public class Urias extends LeaderText {
    public static final String NAME = "Urias";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Greetings.")
            .setLine(Emote.THANKS, "I am most grateful.")
            .setLine(Emote.SORRY, "My apologies.")
            .setLine(Emote.WELLPLAYED, "A worthy performance!")
            .setLine(Emote.SHOCKED, "Hm?")
            .setLine(Emote.THINKING, "What is happening here?")
            .setLine(Emote.THREATEN, "Accept your fate!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/urias.png",
            CRAFT, TRAITS, RARITY, Urias.class,
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
