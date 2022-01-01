package server.card.cardpack.basic;

import java.util.*;
import java.util.stream.Collectors;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WoodOfBrambles extends Amulet {
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Wood of Brambles",
            "<b> Countdown(2). </b> \n <b> Battlecry: </b> add two <b> Faries </b> to your hand. Give all allied minions the following effect until this amulet leaves play: <b> Clash: </b> deal 1 damage to the enemy minion. \n Whenever an allied minion comes into play, give them that effect until this amulet leaves play.",
            "res/card/basic/woodofbrambles.png", CRAFT, 2, WoodOfBrambles.class, Tooltip.COUNTDOWN, Tooltip.BATTLECRY,
            Fairy.TOOLTIP, Tooltip.CLASH);

    public WoodOfBrambles(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Card> cards = List.of(new Fairy(effect.owner.board), new Fairy(effect.owner.board));
                        List<Integer> pos = List.of(-1, -1);
                        this.resolve(b, rl, el, new CreateCardResolver(cards, effect.owner.team, CardStatus.HAND, pos));
                        EffectBrambles e = new EffectBrambles(effect);
                        AddEffectResolver aer = new AddEffectResolver(
                                effect.owner.board.getBoardObjects(effect.owner.team, false, true, false), e);
                        this.resolve(b, rl, el, aer);
                    }

                };
            }

            @Override
            public double getBattlecryValue() {
                return AI.VALUE_PER_CARD_IN_HAND * 2 / 2.;
            }

            @Override
            public Resolver onListenEvent(Event event) {
                Effect effect = this;
                return new Resolver(false) {

                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<BoardObject> cardsEnteringPlay = event.cardsEnteringPlay();
                        if (cardsEnteringPlay != null && effect.owner.status.equals(CardStatus.BOARD)) {
                            List<BoardObject> relevant = new LinkedList<>();
                            for (BoardObject c : cardsEnteringPlay) {
                                if (c instanceof Minion && c.team == effect.owner.team) {
                                    relevant.add(c);
                                }
                            }
                            if (!relevant.isEmpty()) {
                                AddEffectResolver aer = new AddEffectResolver(relevant, new EffectBrambles(effect));
                                this.resolve(b, rl, el, aer);
                            }
                        }
                    }

                };
            }

            @Override
            public Resolver onLeavePlay() {
                // find the effects that this is responsible for
                List<Effect> distributedEffects = this.owner.board.getAdditionalEffects().stream()
                        .filter(e -> e instanceof EffectBrambles && ((EffectBrambles) e).creator == this)
                        .collect(Collectors.toList());
                return new RemoveEffectResolver(distributedEffects);
            }

            @Override
            public double getPresenceValue() {
                return this.owner.finalStatEffects.getStat(EffectStats.COUNTDOWN);
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 2);
        this.addEffect(true, e);
    }

    public static class EffectBrambles extends Effect {
        private Effect creator;

        public EffectBrambles(String description) {
            super(description);
        }

        public EffectBrambles(Effect creator) {
            this("Has <b> Clash: </b> deal 1 damage to the enemy minion until the corresponding Wood of Brambles leaves play.");
            this.creator = creator;
        }

        @Override
        public Resolver clash(Minion target) {
            return new EffectDamageResolver(this, target, 1, true, null);
        }

        @Override
        public double getPresenceValue() {
            return AI.VALUE_PER_DAMAGE * 2 / 2.;
        }

        @Override
        public String extraStateString() {
            return this.creator.toReference();
        }

        @Override
        public void loadExtraState(Board b, StringTokenizer st) {
            this.creator = Effect.fromReference(b, st);
        }

    }
}
