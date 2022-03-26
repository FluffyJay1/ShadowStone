package server.card.cardset.basic.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Cucouroux extends MinionText {
    public static final String NAME = "Cucouroux, Green Gunsmith";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Spend(2)</b> to summon a <b>Camieux, Gunpowder Gal</b>.\n" +
            "When this attacks a minion, deal 2 damage to it.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/cucouroux.png",
            CRAFT, RARITY, 3, 3, 1, 2, true, Cucouroux.class,
            new Vector2f(160, 145), 1.4, null,
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND, Camieux.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                String resolverDescription = "<b>Battlecry</b>: <b>Spend(2)</b> to summon a <b>Camieux, Gunpowder Gal</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new SpendResolver(effect, 2, new CreateCardResolver(new Camieux(), owner.team, CardStatus.BOARD, owner.getIndex() + 1)));
                    }
                });
            }

            @Override
            public ResolverWithDescription onAttack(Minion target) {
                Effect effect = this;
                String resolverDescription = "When this attacks a minion, deal 2 damage to it.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (target.status.equals(CardStatus.BOARD)) {
                            this.resolve(b, rq, el, new DamageResolver(effect, target, 2, true, null));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // some bullshit
                return AI.VALUE_PER_DAMAGE * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
