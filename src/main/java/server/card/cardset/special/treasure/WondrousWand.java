package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class WondrousWand extends SpellText {
    public static final String NAME = "Wondrous Wand";
    public static final String DESCRIPTION = "Draw 3 cards. Set their costs to 0.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/special/wondrouswand.png",
            CRAFT, TRAITS, RARITY, 3, WondrousWand.class,
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
                        DrawResolver dr = this.resolve(b, rq, el, new DrawResolver(owner.player, 3));
                        Effect buff = new Effect("Cost set to 0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.COST, 0)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(dr.drawn, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3 + 12; // assume avg cost of cards drawn is 4
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
