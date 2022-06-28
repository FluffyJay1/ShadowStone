package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.event.EventDestroy;
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
            () -> List.of(Tooltip.SPEND),
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
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> markedForDeath = new LinkedList<>();
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            DamageResolver r = this.resolve(b, rq, el,
                                    new DamageResolver(effect, (Minion) c, 3, false, new EventAnimationDamageFire().toString()));
                            markedForDeath.addAll(r.destroyed);
                        });
                        this.resolve(b, rq, el, new SpendResolver(effect, 4, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Minion> relevant = b.getMinions(owner.team * -1, false, false).collect(Collectors.toList());
                                DamageResolver r = this.resolve(b, rq, el,
                                        new DamageResolver(effect, relevant, 2, false, new EventAnimationDamageAOEFire(owner.team * -1, false).toString()));
                                markedForDeath.addAll(r.destroyed);
                            }
                        }));
                        this.resolve(b, rq, el, new DestroyResolver(markedForDeath, EventDestroy.Cause.NATURAL));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(3) + (AI.valueOfMinionDamage(2) * 3) / 4;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(4);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
