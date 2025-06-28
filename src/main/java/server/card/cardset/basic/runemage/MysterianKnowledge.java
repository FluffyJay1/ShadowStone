package server.card.cardset.basic.runemage;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardTrait;
import server.card.CardVisibility;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class MysterianKnowledge extends SpellText {
    public static final String NAME = "Mysterian Knowledge";
    public static final String DESCRIPTION = "Randomly put 1 of the following cards into your hand: <b>Mysterian Missile</b> or <b>Mysterian Circle</b>.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/mysterianknowledge.png"),
            CRAFT, TRAITS, RARITY, 1, MysterianKnowledge.class,
            () -> List.of(MysterianMissile.TOOLTIP, MysterianCircle.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CardText choice = SelectRandom.from(List.of(new MysterianMissile(), new MysterianCircle()));
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(choice)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withPos(-1)
                                .withVisibility(CardVisibility.ALLIES)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
