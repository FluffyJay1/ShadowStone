package server.card.cardset.basic.dragondruid;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.common.EffectStatChange;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DiscardLowestResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class VenomousPucewyrm extends MinionText {
    public static final String NAME = "Venomous Pucewyrm";
    public static final String DESCRIPTION = "At the end of your turn, randomly discard 1 of the lowest-cost cards in your hand and gain +2/+0/+2.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/venomouspucewyrm.png",
            CRAFT, TRAITS, RARITY, 5, 4, 2, 5, true, VenomousPucewyrm.class,
            new Vector2f(143, 151), 1.3, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DiscardLowestResolver(owner.player, 1));
                        Effect buff = new EffectStatChange("+2/+0/+2 (from end of turn effect).", 2, 0, 2);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_OF_DISCARD + AI.valueForBuff(2, 0, 2);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
