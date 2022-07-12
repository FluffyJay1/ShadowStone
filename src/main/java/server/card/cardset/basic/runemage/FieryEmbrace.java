package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class FieryEmbrace extends SpellText {
    public static final String NAME = "Fiery Embrace";
    public static final String DESCRIPTION = "Destroy an enemy minion. " + EffectSpellboostDiscount.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/basic/fieryembrace.png",
            CRAFT, TRAITS, RARITY, 7, FieryEmbrace.class,
            () -> List.of(Tooltip.SPELLBOOST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect("Destroy an enemy minion.") {
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
                return new ResolverWithDescription("Destroy an enemy minion.", new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DestroyResolver(c));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DESTROY;
            }
        }, new EffectSpellboostDiscount());
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
