package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DiscardLowestResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DragonewtScholar extends MinionText {
    public static final String NAME = "Dragonewt Scholar";
    public static final String STRIKE_DESCRIPTION = "<b>Strike</b>: Randomly discard 1 of the lowest-cost cards in your hand and then draw a card.";
    public static final String DESCRIPTION = "<b>Intimidate</b>.\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/dragonewtscholar.png"),
            CRAFT, TRAITS, RARITY, 2, 1, 1, 3, true, DragonewtScholar.class,
            new Vector2f(151, 165), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.INTIMIDATE, Tooltip.STRIKE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.INTIMIDATE, 1)
                .build()) {
            @Override
            public ResolverWithDescription strike(Minion target) {
                return new ResolverWithDescription(STRIKE_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DiscardLowestResolver(owner.player, 1));
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk how to evaluate this
                return AI.VALUE_PER_CARD_IN_HAND + AI.VALUE_OF_DISCARD;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
