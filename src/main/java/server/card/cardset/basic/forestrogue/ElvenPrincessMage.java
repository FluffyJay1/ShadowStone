package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class ElvenPrincessMage extends MinionText {
    public static final String NAME = "Elven Princess Mage";
    public static final String DESCRIPTION = "<b>Unleash</b>: Add 2 <b>Fairies</b> to your hand and set their cost to 0, " +
            "and gain +M/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/elvenprincessmage.png",
            CRAFT, TRAITS, RARITY, 4, 3, 1, 5, false, ElvenPrincessMage.class,
            new Vector2f(151, 130), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, Fairy.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> c = Collections.nCopies(2, new Fairy());
                        List<Integer> pos = Collections.nCopies(2, -1);
                        CreateCardResolver ccr = new CreateCardResolver(c, owner.team, CardStatus.HAND, pos);
                        this.resolve(b, rq, el, ccr);
                        Effect costBuff = new Effect("Cost set to 0 (from <b>Elven Princess Mage</b>).", EffectStats.builder()
                                .set(Stat.COST, 0)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, costBuff));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Fairy().constructInstance(this.owner.board));
                }
                return (AI.valueForAddingToHand(this.cachedInstances, refs) + 2) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
