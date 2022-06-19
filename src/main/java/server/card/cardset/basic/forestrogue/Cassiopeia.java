package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
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

public class Cassiopeia extends MinionText {
    public static final String NAME = "Cassiopeia";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Deal 1 damage to a random enemy minion. Do this X times. X equals the number of cards in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/cassiopeia.png",
            CRAFT, TRAITS, RARITY, 6, 3, 2, 3, true, Cassiopeia.class,
            new Vector2f(150, 145), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        for (int i = 0; i < owner.player.getHand().size(); i++) {
                            List<Minion> choices = owner.board.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            if (!choices.isEmpty()) {
                                Minion choice = SelectRandom.from(choices);
                                this.resolve(b, rq, el, new DamageResolver(effect, choice, 1, true, new EventAnimationDamageOrbFall().toString()));
                            }
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(1) * this.owner.player.getHand().size() / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
