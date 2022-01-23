package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.Board;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.CardTargetList;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;

import java.util.LinkedList;
import java.util.List;

public class HallowedDogma extends Spell {
    public static final String NAME = "Hallowed Dogma";
    public static final String DESCRIPTION = "Choose a card. If it has <b>Countdown</b>, subtract 2 from it and draw a card. Otherwise, give it <b>Countdown(2)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/halloweddogma.png",
            CRAFT, 2, HallowedDogma.class,
            Tooltip.COUNTDOWN);
    final Effect e;
    public HallowedDogma(Board b) {
        super(b, TOOLTIP);
        this.e = new Effect(DESCRIPTION) {
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
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        // lmao
                        getStillTargetableBattlecryCardTargets(0).findFirst().ifPresent(c -> {
                            if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
                                Effect countdownAdd = new Effect();
                                countdownAdd.effectStats.change.setStat(EffectStats.COUNTDOWN, -2);
                                this.resolve(b, rl, el, new AddEffectResolver(c, countdownAdd));
                                this.resolve(b, rl, el, new DrawResolver(owner.board.getPlayer(owner.team), 1));
                            } else {
                                Effect countdownSet = new Effect();
                                countdownSet.effectStats.set.setStat(EffectStats.COUNTDOWN, 2);
                                this.resolve(b, rl, el, new AddEffectResolver(c, countdownSet));
                            }
                        });
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        };
        this.addEffect(true, e);
    }
}
