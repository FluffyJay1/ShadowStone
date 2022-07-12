package server.card.cardset.special.lich;

import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageChainFrost;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class ChainFrost extends SpellText {
    public static final String NAME = "Chain Frost";
    public static final String DESCRIPTION = "Deal 3 damage to an enemy minion. If there's another enemy minion, randomly choose 1 of them to repeat this effect on.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/special/chainfrost.png",
            CRAFT, TRAITS, RARITY, 7, ChainFrost.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Deal 3 damage to an enemy minion.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            Minion target = (Minion) c;
                            for (int i = 0; i < 14; i++) {
                                ResolverQueue subQueue = new ResolverQueue();
                                this.resolve(b, subQueue, el, new DamageResolver(effect, target, 3, true, new EventAnimationDamageChainFrost().toString()));
                                this.resolveQueue(b, subQueue, el, subQueue);
                                Minion finalTarget = target;
                                List<Minion> nextTargets = b.getMinions(owner.team * -1, false, true)
                                        .filter(m -> m != finalTarget)
                                        .collect(Collectors.toList());
                                if (nextTargets.isEmpty()) {
                                    break;
                                }
                                target = SelectRandom.from(nextTargets);
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(3) * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
