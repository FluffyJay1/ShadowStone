package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class Mercy extends SpellText {
    public static final String NAME = "Mercy";
    public static final String DESCRIPTION = "<b>Banish</b> an enemy minion. Return all allied minions to your hand.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/indie/mercy.png"),
            CRAFT, TRAITS, RARITY, 4, Mercy.class,
            () -> List.of(Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new BanishResolver(c));
                        });
                        List<Minion> relevant = b.getMinions(owner.team, false, true).toList();
                        if (!relevant.isEmpty()) {
                            List<Integer> positions = Collections.nCopies(relevant.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(relevant, CardStatus.HAND, owner.team, positions, true));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_BANISH + AI.VALUE_OF_BOUNCE_ALLIED; // yeah sounds about roight
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
