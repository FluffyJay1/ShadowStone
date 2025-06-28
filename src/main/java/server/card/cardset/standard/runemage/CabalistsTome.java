package server.card.cardset.standard.runemage;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.CardSet;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;

public class CabalistsTome extends SpellText {
    public static final String NAME = "Cabalist's Tome";
    public static final String DESCRIPTION = "Put 3 random Runemage spells into your hand.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/cabaliststome.png"),
            CRAFT, TRAITS, RARITY, 4, CabalistsTome.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> randomCards = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct.getTooltip().craft.equals(ClassCraft.RUNEMAGE) && ct instanceof SpellText && !(ct instanceof CabalistsTome)).toList(), 3);
                        if (!randomCards.isEmpty()) {
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCards(randomCards)
                                    .withTeam(owner.team)
                                    .withStatus(CardStatus.HAND)
                                    .withPos(Collections.nCopies(randomCards.size(), -1))
                                    .withVisibility(CardVisibility.ALLIES)
                                    .build());
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
