package server.card.cardset.basic.forestrogue;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class GlimmeringWings extends SpellText {
    public static final String NAME = "Glimmering Wings";
    public static final String DESCRIPTION = "Draw a card. If at least 2 other cards were played this turn, draw 2 instead.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/glimmeringwings.png",
            CRAFT, TRAITS, RARITY, 2, GlimmeringWings.class,
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
                        if (owner.player.cardsPlayedThisTurn > 2) {
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 2));
                        } else {
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3 / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.cardsPlayedThisTurn >= 2;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
