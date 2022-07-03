package server.card.cardset.anime.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventMinionAttack;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class YareYareDaze extends SpellText {
    public static final String NAME = "Yare Yare Daze";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied minion attacks while this is in your hand, subtract 1 from the cost of this card.";
    private static final String BATTLECRY_DESCRIPTION = "Give your minions <b>Storm</b> and remove their \"Can't attack the enemy leader\" restrictions.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/anime/yareyaredaze.png",
            CRAFT, TRAITS, RARITY, 13, YareYareDaze.class,
            () -> List.of(Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        Effect buff = new Effect("<b>Storm</b> and can attack the enemy leader (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.STORM, 1)
                                .set(Stat.CANT_ATTACK_LEADER, 0)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(targets, buff));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                if (event instanceof EventMinionAttack && this.owner.status.equals(CardStatus.HAND)
                        && ((EventMinionAttack) event).m1.team == this.owner.team) {
                    Effect buff = new Effect("-1 cost (from allied minions attacking).", EffectStats.builder()
                            .change(Stat.COST, -1)
                            .build());
                    return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new AddEffectResolver(this.owner, buff));
                }
                return null;
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfStorm(3) * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
