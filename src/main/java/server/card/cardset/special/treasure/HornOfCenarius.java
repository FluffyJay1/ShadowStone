package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HornOfCenarius extends SpellText {
    public static final String NAME = "Horn of Cenarius";
    public static final String DESCRIPTION = "Put 3 random minions from your deck into play.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/special/hornofcenarius.png"),
            CRAFT, TRAITS, RARITY, 2, HornOfCenarius.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> eligible = owner.player.getDeck().stream()
                                .filter(c -> c instanceof Minion)
                                .collect(Collectors.toList());
                        List<Card> choices = SelectRandom.from(eligible, 3);
                        List<Integer> pos = Collections.nCopies(choices.size(), -1);
                        this.resolve(b, rq, el, new PutCardResolver(choices, CardStatus.BOARD, owner.team, pos, true));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 12; // assume average stat line of minions summoned is 4/4
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
