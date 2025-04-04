package server.card.leader;

import client.tooltip.TooltipLeader;
import client.ui.Animation;
import network.Emote;
import network.EmoteSet;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Omori extends LeaderText {
    public static final String NAME = "OMORI";
    public static final String DESCRIPTION = "He's been living in WHITE SPACE for as long as he can remember.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final EmoteSet EMOTESET = EmoteSet.builder()
            .setLine(Emote.GREETINGS, "...")
            .setLine(Emote.THANKS, "...")
            .setLine(Emote.SORRY, "...")
            .setLine(Emote.WELLPLAYED, "...")
            .setLine(Emote.SHOCKED, "...")
            .setLine(Emote.THINKING, "...")
            .setLine(Emote.THREATEN, "...")
            .build();
    public static final TooltipLeader TOOLTIP = new TooltipLeader(NAME, DESCRIPTION,
            () -> new Animation("leader/omori.png", new Vector2f(3, 1), 0, 0, Image.FILTER_NEAREST,
                    anim -> {
                        anim.play = true;
                        anim.loop = true;
                        anim.setFrameInterval(0.2);
                    }),
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
