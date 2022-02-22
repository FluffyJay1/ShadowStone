package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

import java.util.List;

public class BellringerAngel extends Minion {
    public static final String NAME = "Bellringer Angel";
    public static final String DESCRIPTION = "<b>Ward</b>.\n<b>Last Words</b>: draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/bellringerangel.png",
            CRAFT, RARITY,2, 0, 0, 2, false, BellringerAngel.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.WARD, Tooltip.LASTWORDS));

    public BellringerAngel(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver lastWords() {
                return new DrawResolver(owner.board.getPlayer(owner.team), 1);
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1;
            }
        };
        e.effectStats.set.setStat(EffectStats.WARD, 1);
        this.addEffect(true, e);
    }
}
