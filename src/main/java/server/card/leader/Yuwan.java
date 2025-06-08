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

public class Yuwan extends LeaderText {
    public static final String NAME = "Yuwan";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "The pleasure's mine.")
            .setLine(Emote.THANKS, "Appreciate it.")
            .setLine(Emote.SORRY, "Apologies.")
            .setLine(Emote.WELLPLAYED, "Formidable.")
            .setLine(Emote.SHOCKED, "But how!")
            .setLine(Emote.THINKING, "Stay on your toes...")
            .setLine(Emote.THREATEN, "I will have revenge!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", () -> new Animation("leader/yuwan.png"),
            CRAFT, TRAITS, RARITY, Yuwan.class,
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
