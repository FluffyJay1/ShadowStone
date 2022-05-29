package server.card.cardset.basic.forestrogue;

import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SylvanJustice extends SpellText {
    public static final String NAME = "Sylvan Justice";
    public static final String DESCRIPTION = "Deal 2 damage to an enemy minion. Add a <b>Fairy</b> to your hand.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/sylvanjustice.png",
            CRAFT, RARITY, 2, SylvanJustice.class,
            () -> List.of(Fairy.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Deal 2 damage to an enemy minion.") {
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
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 2, true, EventAnimationDamageOrbFall.class));
                        });
                        this.resolve(b, rq, el, new CreateCardResolver(new Fairy(), owner.team, CardStatus.HAND, -1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Fairy().constructInstance(this.owner.board));
                }
                return AI.VALUE_PER_DAMAGE * 2 + AI.valueForAddingToHand(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}