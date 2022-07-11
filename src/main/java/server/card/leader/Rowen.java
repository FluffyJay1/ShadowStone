package server.card.leader;

import client.tooltip.TooltipLeader;
import network.Emote;
import network.EmoteSet;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Rowen extends LeaderText {
    public static final String NAME = "Rowen";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Reporting for duty.")
            .setLine(Emote.THANKS, "Thanks.")
            .setLine(Emote.SORRY, "Don't let it get to you!")
            .setLine(Emote.WELLPLAYED, "Incredible!")
            .setLine(Emote.SHOCKED, "What?!")
            .setLine(Emote.THINKING, "Think, Rowen...")
            .setLine(Emote.THREATEN, "Hear the dragon's roar!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/smile.png",
            CRAFT, TRAITS, RARITY, Rowen.class,
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
