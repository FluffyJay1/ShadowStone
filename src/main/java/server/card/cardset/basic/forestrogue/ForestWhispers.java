package server.card.cardset.basic.forestrogue;

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
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ForestWhispers extends SpellText {
    public static final String NAME = "Forest Whispers";
    public static final String DESCRIPTION = "<b>Transform</b> an enemy minion into a <b>Fairy</b>. Put a <b>Fairy</b> into your hand";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/basic/forestwhispers.png",
            CRAFT, TRAITS, RARITY, 4, ForestWhispers.class,
            () -> List.of(Tooltip.TRANSFORM, Fairy.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Transform an enemy minion into a <b>Fairy</b>.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new TransformResolver(c, new Fairy()));
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
                return AI.valueForAddingToHand(this.cachedInstances, refs) + 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
