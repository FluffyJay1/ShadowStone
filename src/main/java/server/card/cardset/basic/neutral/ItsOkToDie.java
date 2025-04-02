package server.card.cardset.basic.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventUnleash;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class ItsOkToDie extends AmuletText {
    public static final String NAME = "It's Ok to Die";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 1 damage to all minions. Give all allied minions <b>Rush</b>.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever you unleash an allied minion, give it <b>Rush</b>.";
    public static final String DESCRIPTION = "<b>Countdown(4)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/basic/itsoktodie.png"),
            CRAFT, TRAITS, RARITY, 3, ItsOkToDie.class,
            new Vector2f(172, 210), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 4)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(0, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 1, true,
                                new EventAnimationDamageAOEFire(0, false).toString()));
                        List<Minion> rushTargets = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        Effect buff = new Effect("<b>Rush</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.RUSH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(rushTargets, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(1) * 3;
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventUnleash) {
                    EventUnleash eu = (EventUnleash) event;
                    if (eu.source.team == this.owner.team) {
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                if (eu.m.isInPlay()) {
                                    Effect buff = new Effect("<b>Rush</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                            .set(Stat.RUSH, 1)
                                            .build());
                                    this.resolve(b, rq, el, new AddEffectResolver(eu.m, buff));
                                }
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfRush(3) * 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
