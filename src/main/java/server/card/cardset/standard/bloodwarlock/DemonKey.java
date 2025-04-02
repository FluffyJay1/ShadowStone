package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventDamage;
import server.resolver.AddEffectResolver;
import server.resolver.PutCardResolver;
import server.resolver.RemoveEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class DemonKey extends AmuletText {
    public static final String NAME = "Demon Key";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: At the start of your next turn, randomly put 1 of the highest-cost minions " +
            "from your hand into play.";
    private static final String ONLEADERDAMAGE_DESCRIPTION = "Whenever your leader takes damage during your turn, subtract 1 from this amulet's <b>Countdown</b>.";
    public static final String DESCRIPTION = "<b>Countdown(5)</b>.\n" + ONLEADERDAMAGE_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/demonkey.png"),
            CRAFT, TRAITS, RARITY, 3, DemonKey.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 5)
                .build()) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDamage && this.owner.board.getCurrentPlayerTurn() == this.owner.team) {
                    EventDamage ed = (EventDamage) event;
                    if(this.owner.player.getLeader().isPresent()) {
                        if (ed.m.contains(this.owner.player.getLeader().get())) {
                            Effect subtract = new Effect("", EffectStats.builder()
                                    .change(Stat.COUNTDOWN, -1)
                                    .build());
                            return new ResolverWithDescription(ONLEADERDAMAGE_DESCRIPTION, new AddEffectResolver(this.owner, subtract));
                        }
                    }
                }
                return null;
            }

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new EffectTurnStartPutHighest();
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, buff));
                        });
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                // highest cost card in hand, but not rly
                // tbh i have no fucking clue
                int maxCost = this.owner.player.getHand().stream()
                        .filter(c -> c instanceof Minion)
                        .map(c -> c.finalStats.get(Stat.COST))
                        .reduce(0, Integer::max, Integer::max);
                return maxCost / 2.;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }

    public static class EffectTurnStartPutHighest extends Effect {
        public static final String BUFF_DESCRIPTION = "At the start of your turn, randomly put 1 of the highest-cost minions from your hand into play (from <b>Demon Key</b>).";
        public EffectTurnStartPutHighest() {
            super(BUFF_DESCRIPTION);
        }
        @Override
        public ResolverWithDescription onTurnStartAllied() {
            Effect effect = this;
            return new ResolverWithDescription(BUFF_DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    List<Minion> minionsInHand = owner.player.getHand().stream()
                            .filter(c -> c instanceof Minion)
                            .map(c -> (Minion) c)
                            .collect(Collectors.toList());
                    Minion target = SelectRandom.oneOfWith(minionsInHand, m -> m.finalStats.get(Stat.COST), Integer::max);
                    if (target != null) {
                        this.resolve(b, rq, el, new PutCardResolver(target, CardStatus.BOARD, owner.team, -1, true));
                    }
                    this.resolve(b, rq, el, new RemoveEffectResolver(List.of(effect)));
                }
            });
        }
    }
}
