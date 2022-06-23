package server.card.cardset.moba.shadowshaman;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Baneling extends MinionText {
    public static final String NAME = "Baneling";
    public static final String DESCRIPTION = "<b>Last Words</b>: <b>Blast(5)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/moba/baneling.png",
            CRAFT, TRAITS, RARITY, 3, 1, 0, 1, false, Baneling.class,
            new Vector2f(253, 271), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new BlastResolver(this, 5, new EventAnimationDamageMagicHit().toString()));
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
