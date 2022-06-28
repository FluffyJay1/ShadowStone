package server.card.cardset.standard.forestrogue;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDestroy;
import server.resolver.DestroyResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Vanish extends SpellText {
    public static final String NAME = "Vanish";
    public static final String DESCRIPTION = "Return all minions to their owner's hands.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/standard/vanish.png",
            CRAFT, TRAITS, RARITY, 6, Vanish.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> destroyed = new ArrayList<>();
                        for (int teamMultiplier = 1; teamMultiplier >= -1; teamMultiplier -= 2) {
                            List<Minion> minions = owner.board.getMinions(owner.team * teamMultiplier, false, true).collect(Collectors.toList());
                            if (!minions.isEmpty()) {
                                List<Integer> positions = Collections.nCopies(minions.size(), -1);
                                PutCardResolver r = this.resolve(b, rq, el, new PutCardResolver(minions, CardStatus.HAND, owner.team * teamMultiplier, positions, false));
                                destroyed.addAll(r.destroyed);
                            }
                        }
                        this.resolve(b, rq, el, new DestroyResolver(destroyed, EventDestroy.Cause.NATURAL));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // tbh i have no idea
                return 6;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
