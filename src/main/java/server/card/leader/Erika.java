package server.card.leader;

import client.tooltip.TooltipLeader;
import client.ui.Animation;
import network.Emote;
import network.EmoteSet;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Erika extends LeaderText {
    public static final String NAME = "Erika";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "Charmed.")
            .setLine(Emote.THANKS, "Thank you.")
            .setLine(Emote.SORRY, "Apologies.")
            .setLine(Emote.WELLPLAYED, "Excellent work!")
            .setLine(Emote.SHOCKED, "So that's how it's done!")
            .setLine(Emote.THINKING, "Now what to do?")
            .setLine(Emote.THREATEN, "You have breathed your last!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", () -> new Animation("leader/erika.png"),
            CRAFT, TRAITS, RARITY, Erika.class,
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
