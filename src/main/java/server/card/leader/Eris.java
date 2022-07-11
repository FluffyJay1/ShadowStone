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

public class Eris extends LeaderText {
    public static final String NAME = "Eris";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Hello.")
            .setLine(Emote.THANKS, "I am truly grateful.")
            .setLine(Emote.SORRY, "You must forgive me.")
            .setLine(Emote.WELLPLAYED, "Most impressive.")
            .setLine(Emote.SHOCKED, "Unbelievable...")
            .setLine(Emote.THINKING, "The possibilities are infinite...")
            .setLine(Emote.THREATEN, "I will liberate you!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/eris.png",
            CRAFT, TRAITS, RARITY, Eris.class,
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
