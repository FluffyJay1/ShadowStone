package server.card.cardset.special.treasure;

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

public class BagOfStuffing extends SpellText {
    public static final String NAME = "Bag of Stuffing";
    public static final String DESCRIPTION = "Draw cards until your hand is full.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/special/bagofstuffing.png",
            CRAFT, TRAITS, RARITY, 1, BagOfStuffing.class,
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
                        int missing = owner.player.maxHandSize - owner.player.getHand().size();
                        this.resolve(b, rq, el, new DrawResolver(owner.player, missing));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 6;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
