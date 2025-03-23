package server.card.cardset.indie.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
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
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Ralsei extends MinionText {
    public static final String NAME = "Ralsei";
    public static final String DESCRIPTION = "<b>Unleash</b>: Restore M health to all allies. Give all enemy minions -M/-0/-0.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/indie/ralsei.png",
            CRAFT, TRAITS, RARITY, 4, 1, 2, 5, false, Ralsei.class,
            new Vector2f(154, 174), 1.25, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<Minion> alliedTargets = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new RestoreResolver(effect, alliedTargets, x));
                        List<Minion> enemyTargets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        Effect debuff = new Effect("-" + x + "/-0/-0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, -x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(enemyTargets, debuff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int x = this.owner.finalStats.get(Stat.MAGIC);
                return (AI.VALUE_PER_HEAL * x * 3 - AI.valueForBuff(-x, 0, 0) * 3) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
