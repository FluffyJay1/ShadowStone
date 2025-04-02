package server.card.cardset.anime.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDamage;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.MinionAttackResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GutsBerserk extends MinionText {
    public static final String NAME = "Guts, Berserk";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever this minion or your leader takes damage, gain +1/+1/+1.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of each player's turn, attack a random character, allies and leaders included.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Deal M damage to all characters.";
    public static final String DESCRIPTION = "<b>Disarmed</b>. <b>Stalwart</b>.\n" + UNLEASH_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION + "\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/gutsberserk.png"),
            CRAFT, TRAITS, RARITY, 5, 6, 5, 9, false, GutsBerserk.class,
            new Vector2f(133, 207), 1.4, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.DISARMED, Tooltip.STALWART, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.DISARMED, 1)
                .set(Stat.STALWART, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<Minion> targets = b.getMinions(0, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, x, true,
                                new EventAnimationDamageAOEFire(0, true).toString()));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDamage) {
                    EventDamage ed = (EventDamage) event;
                    if(this.owner.player.getLeader().isPresent()) {
                        int count = (int) IntStream.range(0, ed.m.size())
                                .filter(i -> ed.actualDamage.get(i) > 0 && (ed.m.get(i) == this.owner.player.getLeader().get() || ed.m.get(i) == this.owner))
                                .count();
                        if (count > 0) {
                            return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                                @Override
                                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                    Effect buff = new EffectStatChange("+1/+1/+1 (from taking damage).", 1, 1, 1);
                                    for (int i = 0; i < count; i++) {
                                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                                    }
                                }
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(0, true, true)
                                .filter(m -> m != owner)
                                .collect(Collectors.toList());
                        if (!targets.isEmpty()) {
                            Minion selected = SelectRandom.from(targets);
                            this.resolve(b, rq, el, new MinionAttackResolver((Minion) owner, selected, false));
                        }
                    }
                });
            }

            @Override
            public ResolverWithDescription onTurnEndEnemy() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(0, true, true)
                                .filter(m -> m != owner)
                                .collect(Collectors.toList());
                        if (!targets.isEmpty()) {
                            Minion selected = SelectRandom.from(targets);
                            this.resolve(b, rq, el, new MinionAttackResolver((Minion) owner, selected, false));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 3; // uh
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
