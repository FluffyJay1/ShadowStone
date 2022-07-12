package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class TheCandle extends SpellText {
    public static final String NAME = "THE CANDLE";
    public static final String DESCRIPTION = "Deal 4 damage to all enemy minions. Put <b>THE CANDLE</b> into your deck.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/special/thecandle.png",
            CRAFT, TRAITS, RARITY, 1, TheCandle.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 4, true,
                                new EventAnimationDamageAOEFire(owner.team * -1, false).toString()));
                        int shufflePos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 1).get(0);
                        this.resolve(b, rq, el, new CreateCardResolver(new TheCandle(), owner.team, CardStatus.DECK, shufflePos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new TheCandle().constructInstance(this.owner.board));
                }
                return AI.valueOfMinionDamage(4) * 3 + AI.valueForAddingToDeck(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
