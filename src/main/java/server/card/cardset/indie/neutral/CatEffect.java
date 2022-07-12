package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class CatEffect extends SpellText {
    public static final String NAME = "Cat Effect";
    public static final String DESCRIPTION = "Give an allied minion <b>Ward</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/indie/cateffect.png",
            CRAFT, TRAITS, RARITY, 0, CatEffect.class,
            () -> List.of(Tooltip.WARD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            Effect buff = new Effect("<b>Ward</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.WARD, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(c, buff));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_WARD;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
