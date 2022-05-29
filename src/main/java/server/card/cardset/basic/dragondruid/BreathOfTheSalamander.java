package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BreathOfTheSalamander extends SpellText {
    public static final String NAME = "Breath of the Salamander";
    public static final String DESCRIPTION = "Deal 3 damage to an enemy minion. Then <b>Spend(4)</b> to deal 2 damage to all enemy minions.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/breathofthesalamander.png",
            CRAFT, TRAITS, RARITY, 2, BreathOfTheSalamander.class,
            () -> List.of(Tooltip.SPEND));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    public boolean canTarget(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> markedForDeath = new LinkedList<>();
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            DamageResolver r = this.resolve(b, rq, el,
                                    new DamageResolver(effect, (Minion) c, 3, false, EventAnimationDamageFire.class));
                            markedForDeath.addAll(r.destroyed);
                        });
                        this.resolve(b, rq, el, new SpendResolver(effect, 4, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Minion> relevant = b.getMinions(owner.team * -1, false, false).collect(Collectors.toList());
                                DamageResolver r = this.resolve(b, rq, el,
                                        new DamageResolver(effect, relevant, 2, false, EventAnimationDamageFire.class));
                                markedForDeath.addAll(r.destroyed);
                            }
                        }));
                        this.resolve(b, rq, el, new DestroyResolver(markedForDeath));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // don't factor in the spend effect
                return AI.VALUE_PER_DAMAGE * 3;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.mana >= this.owner.finalStatEffects.getStat(EffectStats.COST) + 4;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
