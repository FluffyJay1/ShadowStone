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

public class Lich extends LeaderText {
    public static final String NAME = "Lich";
    public static final String DESCRIPTION = "Gonna have your mana.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "I am the dead of winter!")
            .setLine(Emote.THANKS, "Why thank you.")
            .setLine(Emote.SORRY, "Does it tingle?")
            .setLine(Emote.WELLPLAYED, "Oh, that's cool.")
            .setLine(Emote.SHOCKED, "What a drip!")
            .setLine(Emote.THINKING, "A chill wind...")
            .setLine(Emote.THREATEN, "Feel my cold embrace!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, DESCRIPTION, () -> new Animation("leader/lich.png"),
            CRAFT, TRAITS, RARITY, Lich.class,
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
