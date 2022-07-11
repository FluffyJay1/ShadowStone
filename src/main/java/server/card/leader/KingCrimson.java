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

public class KingCrimson extends LeaderText {
    public static final String NAME = "King Crimson";
    public static final String DESCRIPTION = "It just works.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "You should just go home now.")
            .setLine(Emote.THANKS, "I respect all the work you've done.")
            .setLine(Emote.SORRY, "You must be troubled.")
            .setLine(Emote.WELLPLAYED, "I see you've learned.")
            .setLine(Emote.SHOCKED, "What?!")
            .setLine(Emote.THINKING, "What's the meaning of this?")
            .setLine(Emote.THREATEN, "King Crimson has already seen through it.")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, DESCRIPTION, "res/leader/kingcrimson.png",
            CRAFT, TRAITS, RARITY, KingCrimson.class,
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
