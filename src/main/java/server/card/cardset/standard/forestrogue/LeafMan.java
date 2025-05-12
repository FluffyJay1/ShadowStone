package server.card.cardset.standard.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
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
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class LeafMan extends MinionText {
    public static final String NAME = "Leaf Man";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Spend(3)</b> to give all allied minions +1/+0/+0 and <b>Repel</b> until the end of the opponent's turn.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/leafman.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 2, true, LeafMan.class,
            new Vector2f(160, 200), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND, Tooltip.REPEL),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new SpendResolver(this, 3, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team, false, true).toList();
                        Effect buff = new Effect("+1/+0/+0 and <b>Repel</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, 1)
                                .set(Stat.REPEL, 1)
                                .build(),
                                e -> e.setUntilTurnEnd(-1, 1));
                        this.resolve(b, rq, el, new AddEffectResolver(targets, buff));
                    }
                }));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return (AI.VALUE_OF_REPEL + AI.valueForBuff(1, 0, 0)) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.canSpendAfterPlayed(3);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
