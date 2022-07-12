package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Elsen extends MinionText {
    public static final String NAME = "Elsen";
    public static final String DESCRIPTION = "At the start of your turn, <b>Transform</b> into a <b>Spectre</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/indie/elsen.png",
            CRAFT, TRAITS, RARITY, 0, 0, 1, 4, true, Elsen.class,
            new Vector2f(156, 188), 1.25, new EventAnimationDamageOff(),
            () -> List.of(Tooltip.TRANSFORM, Spectre.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                return new ResolverWithDescription(DESCRIPTION, new TransformResolver(this.owner, new Spectre()));
            }

            // presence value is hard to compute for this one
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
