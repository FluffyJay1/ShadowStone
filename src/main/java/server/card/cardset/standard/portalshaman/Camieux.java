package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class Camieux extends MinionText {
    public static final String NAME = "Camieux, Gunpowder Gal";
    public static final String DESCRIPTION = "<b>Last Words</b>: Deal 1 damage to a random enemy. Do this 4 times.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/camieux.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 1, true, Camieux.class,
            new Vector2f(143, 150), 1.4, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        for (int i = 0; i < 4; i++) {
                            List<Minion> relevant = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                            if (!relevant.isEmpty()) {
                                this.resolve(b, rq, el, new DamageResolver(effect, SelectRandom.from(relevant), 1, true, new EventAnimationDamageShoot()));
                            }
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 4;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
