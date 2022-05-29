package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class HallowedDogma extends SpellText {
    public static final String NAME = "Hallowed Dogma";
    public static final String DESCRIPTION = "Choose a card. If it has <b>Countdown</b>, subtract 2 from it and draw a card. Otherwise, give it <b>Countdown(2)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/halloweddogma.png",
            CRAFT, TRAITS, RARITY, 2, HallowedDogma.class,
            () -> List.of(Tooltip.COUNTDOWN));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    public boolean canTarget(Card c) {
                        return c.status.equals(CardStatus.BOARD);
                    }
                });
            }
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        // lmao
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
                                Effect countdownAdd = new Effect();
                                countdownAdd.effectStats.change.setStat(EffectStats.COUNTDOWN, -2);
                                this.resolve(b, rq, el, new AddEffectResolver(c, countdownAdd));
                                this.resolve(b, rq, el, new DrawResolver(owner.board.getPlayer(owner.team), 1));
                            } else {
                                Effect countdownSet = new Effect();
                                countdownSet.effectStats.set.setStat(EffectStats.COUNTDOWN, 2);
                                this.resolve(b, rq, el, new AddEffectResolver(c, countdownSet));
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
