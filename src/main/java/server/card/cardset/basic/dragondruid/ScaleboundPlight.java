package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DiscardResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ScaleboundPlight extends SpellText {
    public static final String NAME = "Scalebound Plight";
    public static final String DESCRIPTION = "Discard a card. Draw a card. If <b>Overflow</b> is active for you, draw 2 cards instead.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/scaleboundplight.png",
            CRAFT, TRAITS, RARITY, 1, ScaleboundPlight.class,
            () -> List.of(Tooltip.OVERFLOW),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Discard a card.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.HAND) && c.team == this.getCreator().owner.team && c != this.getCreator().owner;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DiscardResolver(c));
                            if (owner.player.overflow()) {
                                this.resolve(b, rq, el, new DrawResolver(owner.player, 2));
                            } else {
                                this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DISCARD + AI.VALUE_PER_CARD_IN_HAND * 3 / 2.;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.overflow();
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
