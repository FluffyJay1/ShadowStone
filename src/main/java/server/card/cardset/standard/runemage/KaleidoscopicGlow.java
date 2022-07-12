package server.card.cardset.standard.runemage;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class KaleidoscopicGlow extends SpellText {
    public static final String NAME = "Kaleidoscopic Glow";
    public static final String DESCRIPTION = "Return a card that costs 2 or less to the player's hand. Draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/standard/kaleidoscopicglow.png",
            CRAFT, TRAITS, RARITY, 2, KaleidoscopicGlow.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Return a card that costs 2 or less to the player's hand.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c.finalBasicStats.get(Stat.COST) <= 2;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new PutCardResolver(c, CardStatus.HAND, c.team, -1, true));
                        });
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 1 + AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
