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

public class Isabelle extends LeaderText {
    public static final String NAME = "Isabelle";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "How do you do.")
            .setLine(Emote.THANKS, "Thank you.")
            .setLine(Emote.SORRY, "I'm sorry...")
            .setLine(Emote.WELLPLAYED, "Impressive!")
            .setLine(Emote.SHOCKED, "I'm shocked!")
            .setLine(Emote.THINKING, "Hmm... Quite the enigma...")
            .setLine(Emote.THREATEN, "You don't stand a chance!")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, "", "res/leader/isabelle.png",
            CRAFT, TRAITS, RARITY, Isabelle.class,
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
