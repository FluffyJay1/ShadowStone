package server.card.cardset.basic.shadowshaman;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

import java.util.List;

public class Baneling extends MinionText {
    public static final String NAME = "Baneling";
    public static final String DESCRIPTION = "<b>Last Words</b>: <b>Blast(5)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/baneling.png",
            CRAFT, RARITY, 3, 1, 0, 1, false, Baneling.class,
            new Vector2f(253, 271), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LASTWORDS, Tooltip.BLAST));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver lastWords() {
                return new BlastResolver(this, 5, null);
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 5 / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
