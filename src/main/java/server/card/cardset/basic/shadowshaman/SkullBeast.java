package server.card.cardset.basic.shadowshaman;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.resolver.GainShadowResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class SkullBeast extends MinionText {
    public static final String NAME = "Skull Beast";
    public static final String DESCRIPTION = "<b>Last Words</b>: Gain 1 shadow.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/skullbeast.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 0, 3, true, SkullBeast.class,
            new Vector2f(136, 219), 1.5, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new GainShadowResolver(this.owner.player, 1));
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_SHADOW;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
