package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageArrow;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventRestore;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class KelHolyMarksman extends MinionText {
    public static final String NAME = "Kel, Holy Marksman";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever your leader's health is restored, deal X damage to all enemy minions. X equals this minion's magic.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain +0/+1/+0. Restore 2 health to your leader.";
    public static final String DESCRIPTION = ONLISTENEVENT_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/kelholymarksman.png",
            CRAFT, TRAITS, RARITY, 5, 4, 1, 3, false, KelHolyMarksman.class,
            new Vector2f(154, 154), 1.4, new EventAnimationDamageArrow(),
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new EffectStatChange("+0/+1/+0 (from <b>Unleash</b>).", 0, 1, 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new RestoreResolver(effect, l, 2));
                        });
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                Effect effect = this;
                if (event instanceof EventRestore && this.owner.player.getLeader().isPresent()
                        && ((EventRestore) event).m.contains(this.owner.player.getLeader().get())) {
                    return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            int damage = owner.finalStats.get(Stat.MAGIC);
                            List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            this.resolve(b, rq, el, new DamageResolver(effect, targets, damage, true, new EventAnimationDamageArrow().toString()));
                        }
                    });
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(this.owner.finalStats.get(Stat.MAGIC)) * 3 + AI.VALUE_PER_HEAL;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
